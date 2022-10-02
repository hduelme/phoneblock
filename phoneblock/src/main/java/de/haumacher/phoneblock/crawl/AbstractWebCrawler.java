/*
 * Copyright (c) 2022 Bernhard Haumacher et al. All Rights Reserved.
 */
package de.haumacher.phoneblock.crawl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for crawler implementations that search the web for spam reports.
 */
public abstract class AbstractWebCrawler implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractWebCrawler.class);

	/**
	 * Agent strings to hide in the mass.
	 */
	private static final String[] AGENTS = {
		"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)",
		"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
		"Mozilla/5.0 (iPad; CPU OS 7_1_2 like Mac OS X; en-US) AppleWebKit/531.5.2 (KHTML, like Gecko) Version/4.0.5 Mobile/8B116 Safari/6531.5.2",
		"Mozilla/5.0 (Linux; U; Android 4.4.2; en-US; HM NOTE 1W Build/KOT49H) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 UCBrowser/11.0.5.850 U3/0.8.0 Mobile Safari/534.30",
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/601.7.7 (KHTML, like Gecko) Version/9.1.2 Safari/601.7.7",
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:47.0) Gecko/20100101 Firefox/47.0",
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36",
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 11_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15",
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36",
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36",
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36",
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36",
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:55.0) Gecko/20100101 Firefox/55.0",
		"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36",
		"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36",
		"Mozilla/5.0 (Windows NT 5.1; rv:9.0.1) Gecko/20100101 Firefox/9.0.1",
		"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0",
		"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36",
		"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36",
		"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE",
		"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0",
		"Mozilla/5.0 (X11; Linux i686; rv:93.0) Gecko/20100101 Firefox/93.0",
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.129 Safari/537.36",
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4 240.111 Safari/537.36",
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36",
		"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:76.0) Gecko/20100101 Firefox/76.0",
		"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:77.0) Gecko/20100101 Firefox/77.0",
		"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:96.0) Gecko/20100101 Firefox/96.0",
	};

	protected static final int MINUTES = 1000 * 60;
	
	private URL _url;

	protected final Random _rnd = new Random();

	private boolean _stopped;

	private SpamReporter _reporter;
	
	/**
	 * Creates a {@link AbstractWebCrawler}.
	 *
	 * @param url The URL to fetch.
	 * @param reporter The sink of generated spam reports. 
	 */
	public AbstractWebCrawler(String url, SpamReporter reporter) throws MalformedURLException {
		_reporter = reporter;
		_url = new URL(url);
	}

	@Override
	public void run() {
		while (true) {
			if (isStopped()) {
				LOG.info("Stopping crawler.");
				return;
			}
			
			long delay = process();
			
			LOG.info("Crawler sleeping for " + (delay / MINUTES) + " minutes.");
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ex) {
				LOG.info("Stopping crawler.");
				return;
			}
		}
	}
	
	/**
	 * Fetches web page and analyzes it.
	 *
	 * @return The number of milliseconds to wait before the next fetch should be performed.
	 */
	public long process() {
		try {
			return tryProcess();
		} catch (Throwable ex) {
			LOG.error("Failed to crawl.", ex);

			return 20 * MINUTES;
		}
	}
	
	/** 
	 * Implementation of {@link #process()}.
	 */
	protected abstract long tryProcess() throws Throwable;

	/** 
	 * Fetches contents from the configured URL.
	 */
	protected Document fetch() throws IOException {
		Document document ;
		HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
		try {
			connection.setRequestProperty("User-Agent", chooseAgent());
			
			try (InputStream in = _url.openStream()) {
				document = Jsoup.parse(in, connection.getContentEncoding(), _url.toExternalForm());
			}
		} finally {
			connection.disconnect();
		}
		return document;
	}

	/** 
	 * Selects a random user agent string.
	 */
	protected String chooseAgent() {
		return AGENTS[_rnd.nextInt(AGENTS.length)];
	}

	/** 
	 * Requests to stop.
	 */
	public synchronized void stop() {
		_stopped = true;
		notifyAll();
	}

	/** 
	 * Whether this instance has been {@link #isStopped() stopped}.
	 */
	public synchronized boolean isStopped() {
		return _stopped;
	}

	/** 
	 * Forwards the spam report to the configured {@link SpamReporter}.
	 */
	protected void reportCaller(String caller, int rating, long time) {
		_reporter.reportCaller(caller, rating, time);
	}

}
