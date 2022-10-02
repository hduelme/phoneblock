/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.phoneblock.carddav.resource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.haumacher.phoneblock.carddav.schema.CardDavSchema;
import de.haumacher.phoneblock.db.BlockList;
import de.haumacher.phoneblock.db.DBService;
import de.haumacher.phoneblock.db.SpamReports;
import de.haumacher.phoneblock.db.Users;
import de.haumacher.phoneblock.db.settings.UserSettings;

/**
 * {@link Resource} representing a collection of {@link AddressBookResource}s.
 */
public class AddressBookResource extends Resource {

	private static final Logger LOG = LoggerFactory.getLogger(AddressBookResource.class);

	private final String _principal;
	private String _serverRoot;

	/** 
	 * Creates a {@link AddressBookResource}.
	 * 
	 * @param rootUrl The full URl (including protocol) of the CardDAV servlet.
	 * @param serverRoot The absolute path of the CardDAV servlet relative to the server.
	 */
	public AddressBookResource(String rootUrl, String serverRoot, String resourcePath, String principal) {
		super(rootUrl, resourcePath);
		_serverRoot = serverRoot;
		_principal = principal;
	}
	
	@Override
	protected boolean isCollection() {
		return true;
	}
	
	@Override
	protected QName getResourceType() {
		return CardDavSchema.CARDDAV_ADDRESSBOOK;
	}
	
	@Override
	public Collection<Resource> list() {
		return allPhoneNumbers()
			.stream()
			.sorted()
			.map(r -> new AddressResource(getRootUrl(), getResourcePath() + r, _principal))
			.collect(Collectors.toList());
	}

	private Set<String> allPhoneNumbers() {
		try (SqlSession session = DBService.getInstance().openSession()) {
			SpamReports reports = session.getMapper(SpamReports.class);
			BlockList blocklist = session.getMapper(BlockList.class);
			Users users = session.getMapper(Users.class);
			UserSettings settings = users.getSettings(_principal);
			
			long currentUser = users.getUserId(_principal);
			
			List<String> personalizations = blocklist.getPersonalizations(currentUser);
			
			Set<String> result = reports.getSpamList(settings.getMinVotes(), settings.getMaxLength() - personalizations.size());
			result.removeAll(blocklist.getExcluded(currentUser));
			result.addAll(personalizations);
			
			return result;
		}
	}
	
	@Override
	protected String getDisplayName() {
		return "BLOCKLIST";
	}
	
	@Override
	public Resource get(String url) {
		String rootUrl = getRootUrl();
		
		int prefixLength;
		if (url.startsWith(rootUrl)) {
			prefixLength = rootUrl.length();
		} else {
			if (url.startsWith(_serverRoot)) {
				prefixLength = _serverRoot.length();
			} else {
				// Invalid URL.
				LOG.warn("Received invalid contact URL outside server '" + rootUrl + "': " + url);
				return null;
			}
		}
		if (!url.startsWith(getResourcePath(), prefixLength)) {
			LOG.warn("Received invalid contact URL outside address book '" + getResourcePath() + "': " + url);
			return null;
		}
		
		return new AddressResource(rootUrl, url.substring(prefixLength + getResourcePath().length()), _principal);
	}

	@Override
	public String getEtag() {
		return Integer.toString(allPhoneNumbers().stream().map(r -> r.hashCode()).reduce(0, (x, y) -> x + y));
	}
}
