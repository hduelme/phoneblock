/*
 * Copyright (c) 2023 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.phoneblock.ab;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.ibatis.session.SqlSession;
import org.kohsuke.args4j.ClassParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.mjsip.pool.PortConfig;
import org.mjsip.pool.PortPool;
import org.mjsip.sip.address.NameAddress;
import org.mjsip.sip.address.SipURI;
import org.mjsip.sip.provider.SipConfig;
import org.mjsip.sip.provider.SipProvider;
import org.mjsip.time.Scheduler;
import org.mjsip.ua.registration.RegistrationClient;
import org.mjsip.ua.registration.RegistrationClientListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoolu.net.AddressType;
import org.zoolu.util.ConfigFile;

import de.haumacher.phoneblock.answerbot.AnswerBot;
import de.haumacher.phoneblock.answerbot.AnswerbotConfig;
import de.haumacher.phoneblock.answerbot.CustomerConfig;
import de.haumacher.phoneblock.answerbot.CustomerOptions;
import de.haumacher.phoneblock.db.DB;
import de.haumacher.phoneblock.db.DBAnswerBotSip;
import de.haumacher.phoneblock.db.DBService;
import de.haumacher.phoneblock.db.Users;
import de.haumacher.phoneblock.db.settings.AnswerBotSip;
import de.haumacher.phoneblock.scheduler.SchedulerService;

/**
 * Service managing a SIP stack.
 */
public class SipService implements ServletContextListener, RegistrationClientListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(SipService.class);

	private SchedulerService _scheduler;
	private DBService _dbService;
	
	private SipProvider _sipProvider;
	private PortPool _portPool;
	private AnswerBot _answerBot;
	
	private final ConcurrentHashMap<String, Registration> _clients = new ConcurrentHashMap<>();

	private long _lastRegister;
	
	private static SipService _instance;

	private String _fileName;

	private Collection<String> _jndiOptions;

	private SipServiceConfig _config;

	/** 
	 * Creates a {@link SipService}.
	 */
	public SipService(SchedulerService scheduler, DBService dbService) {
		_scheduler = scheduler;
		_dbService = dbService;
	}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		loadJndiOptions();
		
		start();
	}
	
	private void loadJndiOptions() {
		_fileName = null;
		_jndiOptions = new ArrayList<>();
		try {
			InitialContext initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");

			NamingEnumeration<NameClassPair> options = envCtx.list("answerbot");
			for (; options.hasMore(); ) {
				NameClassPair pair = options.next();
				String name = pair.getName();
				Object lookup = envCtx.lookup("answerbot/" + name);
				
				if ("configfile".equals(name)) {
					_fileName = lookup.toString();
				} else {
					_jndiOptions.add(name);
					if (lookup != null) {
						_jndiOptions.add(lookup.toString());
					}
				}
			}
		} catch (NamingException ex) {
			LOG.error("Error loading JDNI configuration: " + ex.getMessage());
		}
	}
	
	public void start() {
		if (_answerBot != null) {
			LOG.warn("SIP service already active.");
			return;
		}
		LOG.info("Starting SIP service.");
		
		SipConfig sipConfig = new SipConfig();
		PortConfig portConfig = new PortConfig();
		portConfig.setMediaPort(50061);
		portConfig.setPortCount(20);
		_config = new SipServiceConfig();
		AnswerbotConfig botOptions = new AnswerbotConfig();
		
		loadConfig(sipConfig, portConfig, botOptions);
		sipConfig.normalize();
		resolveViaAddress(sipConfig);
		
		Scheduler scheduler = Scheduler.of(_scheduler.executor());
		
		_sipProvider = new SipProvider(sipConfig, scheduler);
		_portPool = portConfig.createPool();
		_answerBot = new AnswerBot(_sipProvider, botOptions, this::getCustomer, _portPool) {
			@Override
			protected void processCallData(String userName, String from, long startTime, long duration) {
				super.processCallData(userName, from, startTime, duration);
				
				DB db = _dbService.db();
				try (SqlSession session = db.openSession()) {
					Users users = session.getMapper(Users.class);
					
					long id = users.getAnswerBotId(userName);
					users.recordCall(id, from, startTime, duration);
					users.recordSummary(id, duration);
					
					session.commit();
				}
			}
		};
		
		_instance = this;
		
		_scheduler.executor().scheduleAtFixedRate(this::registerBots, 10, 5 * 60, TimeUnit.SECONDS);
	}

	private void resolveViaAddress(SipConfig sipConfig) {
		sipConfig.setViaAddrIPv4(resolve(AddressType.IP4, sipConfig.getViaAddrIPv4()));
		sipConfig.setViaAddrIPv6(resolve(AddressType.IP6, sipConfig.getViaAddrIPv6()));
	}

	private String resolve(AddressType type, String hostName) {
		boolean resolveV6 = type == AddressType.IP6;
		
		try {
			for (InetAddress address : InetAddress.getAllByName(hostName)) {
				boolean ipv6 = address instanceof Inet6Address;
				if (resolveV6 == ipv6) {
					return address.getHostAddress();
				}
			}
		} catch (UnknownHostException e) {
			// Ignore.
		}
		return hostName;
	}
	
	/**
	 * Registers all answer bots that have been updated since the last run.
	 */
	private void registerBots() {
		resolveViaAddress((SipConfig) _sipProvider.sipConfig());
		
		long since = _lastRegister;
		_lastRegister = System.currentTimeMillis();
		
		DB db = _dbService.db();
		
		List<? extends AnswerBotSip> bots;
		try (SqlSession session = db.openSession()) {
			Users users = session.getMapper(Users.class);
			
			bots = users.getEnabledAnswerBots(since);
		}
		
		List<AnswerBotSip> failed = new ArrayList<>();
		for (AnswerBotSip bot : bots) {
			String host = getHost(bot);
			if (host == null || host.isEmpty()) {
				failed.add(bot);
			} else {
				register(bot, false);
			}
		}

		if (!failed.isEmpty()) {
			try (SqlSession session = db.openSession()) {
				Users users = session.getMapper(Users.class);
			
				for (AnswerBotSip bot : failed) {
					LOG.warn("Disabling answer bot without host address: " + bot.getUserId() + "/" + bot.getUserName());
					users.enableAnswerBot(bot.getId(), false, _lastRegister);
				}

				session.commit();
			}
		}
	}

	private String getHost(AnswerBotSip bot) {
		String host = bot.getHost();
		if (host == null || host.isEmpty()) {
			host = bot.getIpv6();
			if (host == null || host.isEmpty()) {
				host = bot.getIpv4();
			}
		}
		return host;
	}
	
	private void loadConfig(Object...beans) {
		CmdLineParser parser = new CmdLineParser(null);
		for (Object bean : beans) {
			new ClassParser().parse(bean, parser);
		}
		
		try {
			Collection<String> fileOptions;
			if (_fileName != null) {
				File file = new File(_fileName);
				if (!file.exists()) {
					LOG.error("Answerbot configuration file does not exits: " + file.getAbsolutePath());
				}
				
				LOG.info("Loading configuration from: " + _fileName);
				ConfigFile configFile = new ConfigFile(file);
				fileOptions = configFile.toArguments();
			} else {
				fileOptions = Collections.emptyList();
			}
			
			Collection<String> arguments = new ArrayList<>(fileOptions);
			arguments.addAll(_jndiOptions);
			
			parser.parseArgument(arguments);
		} catch (CmdLineException ex) {
			LOG.error("Invalid answer bot configuration: " + ex.getMessage());
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		stop();
	}
	
	public void stop() {
		LOG.info("Stopping SIP service.");
		for (RegistrationClient client : _clients.values()) {
			client.halt();
		}
		_clients.clear();

		if (_answerBot != null) {
			_answerBot.halt();
		}
		_answerBot = null;
		_portPool = null;
		
		if (_sipProvider != null) {
			_sipProvider.halt();
			_sipProvider = null;
		}
		
		if (_instance == this) {
			_instance = null;
		}
	}
	
	/**
	 * The {@link SipService} singleton.
	 */
	public static SipService getInstance() {
		return _instance;
	}
	
	public void enableAnwserBot(String userName) {
		enableAnwserBot(userName, false);
	}
	
	public void enableAnwserBot(String userName, boolean temporary) {
		try (SqlSession tx = _dbService.db().openSession()) {
			Users users = tx.getMapper(Users.class);
			
			DBAnswerBotSip bot = users.getAnswerBotBySipUser(userName);
			if (bot == null) {
				LOG.warn("User with ID '" + userName + "' not found.");
				return;
			}
			
			register(bot, temporary);
		}
	}

	public void disableAnwserBot(String userName) {
		try (SqlSession tx = _dbService.db().openSession()) {
			Users users = tx.getMapper(Users.class);
			
			DBAnswerBotSip bot = users.getAnswerBotBySipUser(userName);
			if (bot == null) {
				LOG.warn("User with ID '" + userName + "' not found.");
				return;
			}
			
			users.enableAnswerBot(bot.getId(), false, System.currentTimeMillis());
			tx.commit();
			
			stop(userName);
		}
	}

	private void stop(String userName) {
		Registration registration = _clients.remove(userName);
		if (registration == null) {
			LOG.info("No active registration for user '" + userName + "'.");
			return;
		}

		LOG.info("Stopping answer bot '" + userName + "'.");
		registration.halt();
	}

	/**
	 * Dynamically registers a new answer bot.
	 * 
	 * @param temporary Whether the bot is first activated and should only be
	 *                  permanently activated, if the first registration succeeds.
	 */
	public void register(AnswerBotSip bot, boolean temporary) {
		register(bot, toCustomerConfig(bot), temporary);
	}

	private CustomerConfig toCustomerConfig(AnswerBotSip bot) {
		CustomerConfig regConfig = new CustomerConfig();
		regConfig.setUser(bot.getUserName());
		regConfig.setPasswd(bot.getPasswd());
		regConfig.setRealm(bot.getRealm());
		regConfig.setRegistrar(new SipURI(bot.getRegistrar()));
		regConfig.setRoute(new SipURI(getHost(bot)).addLr());
		return regConfig;
	}

	private void register(AnswerBotSip bot, CustomerOptions customerConfig, boolean temporary) {
		try {
			Registration client = new Registration(_sipProvider, bot, customerConfig, this, temporary);
			Registration clash = _clients.put(bot.getUserName(), client);
			
			if (clash != null) {
				clash.halt();
			}
			
			LOG.info("Started registration for " + customerConfig.getUser() + ".");
			client.loopRegister(customerConfig);
		} catch (Exception ex) {
			LOG.error("Registration for " + customerConfig.getUser() + " failed.", ex);
		}
	}
	
	/**
	 * Whether a registration client is active for the given user name.
	 */
	public boolean isActive(String userName) {
		return _clients.get(userName) != null;
	}

	@Override
	public void onRegistrationSuccess(RegistrationClient client, NameAddress target, NameAddress contact, int expires,
			int renewTime, String result) {
		Registration registration = (Registration) client;
		long id = registration.getId();
		
		if (registration.isTemporary()) {
			// Bot was only started temporarily, now enable permanently.
			
			try (SqlSession session = _dbService.db().openSession()) {
				Users users = session.getMapper(Users.class);
				
				users.enableAnswerBot(id, true, System.currentTimeMillis());
				session.commit();
			}
			
			registration.setPermanent();
		}
		registration.resetFailures();

		updateRegistration(id, true, result);

		LOG.info("Sucessfully registered " + client.getUsername() + ": " + result);
	}

	@Override
	public void onRegistrationFailure(RegistrationClient client, NameAddress target, NameAddress contact,
			String result) {
		Registration registration = (Registration) client;
		long id = registration.getId();

		registration.incFailures();
		int failures = registration.getFailures();

		LOG.warn("Failed to register '" + client.getUsername() + "' (" + failures + " failures): " + result);
		updateRegistration(id, false, result);
		
		boolean temporary = registration.isTemporary();
		if (temporary || failures > _config.maxFailures) {
			LOG.warn("Stopping " + (temporary ? "temporary " : "") + "registration '" + client.getUsername() + "'.");
			stop(registration.getUsername());
		}
	}

	private void updateRegistration(long id, boolean registered, String result) {
		try (SqlSession session = _dbService.db().openSession()) {
			Users users = session.getMapper(Users.class);
			
			int cnt = users.updateSipRegistration(id, registered, result);
			if (cnt > 0) {
				session.commit();
			}
		}
	}

	private CustomerOptions getCustomer(String userName) {
		Registration registration = _clients.get(userName);
		if (registration == null) {
			return null;
		}
		return registration.getCustomer();
	}

}
