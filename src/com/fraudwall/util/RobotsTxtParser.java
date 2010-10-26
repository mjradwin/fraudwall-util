/**
 * Copyright (c) 2010, Anchor Intelligence. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither the name of Anchor Intelligence nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.fraudwall.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Fetches and parses robots.txt files per the Robots Exclusion Standard.
 * @see <a href="http://www.robotstxt.org/">Robots Exclusion Standard</a>
 * @author mradwin
 */
public class RobotsTxtParser {
	private final Log log = LogFactory.getLog(RobotsTxtParser.class);

	/** time to wait (in seconds) */
	public static final int DEFAULT_TIMEOUT = 10;
	/** number of hostnames for the cache */
	public static final int DEFAULT_CACHESIZE = 1000;
	private static final String ROBOTS_TXT_DISALLOW = "Disallow:";
	private static final String ROBOTS_TXT_USER_AGENT = "User-agent:";
	private static final List<String> EMPTY_LIST = Collections.emptyList();

	private final ExpiringLRUMap<String,List<String>> robotsTxtCache;
	private final HttpConnectionManager hcm;
	private final HttpClient client;

	public RobotsTxtParser() {
		this(DEFAULT_TIMEOUT, DEFAULT_CACHESIZE);
	}

	public RobotsTxtParser(int timeout, int cacheSize) {
		robotsTxtCache = ExpiringLRUMap.create(cacheSize);

		int timeToWaitForResponse = (int) (timeout * DateUtils.MILLIS_PER_SECOND);
		HttpConnectionManagerParams hmcp = new HttpConnectionManagerParams();
		hmcp.setSoTimeout(timeToWaitForResponse);
		hmcp.setConnectionTimeout(timeToWaitForResponse);
		hcm = new MultiThreadedHttpConnectionManager();
		hcm.setParams(hmcp);
		client = new HttpClient(hcm);

		String proxyHost = FWProps.getStringProperty("http.proxyHost");
		int proxyPort = FWProps.getIntegerProperty("http.proxyPort");
		if (StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
			client.getHostConfiguration().setProxy(proxyHost, proxyPort);
		}
	}

	private boolean isDeniedByRobotsTxtInner(URI uri, List<String> disallowed) throws URIException {
		String path = uri.getPath();
		for (String prefix : disallowed) {
			if (path.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the URI is disallowed by the corresponding robots.txt,
	 * false otherwise. If the robots.txt file cannot be fetched due to timeout
	 * or another HTTP error, throws an exception. If the web server is
	 * responding correctly but robots.txt simply returns a 404 Not Found
	 * or some other HTTP error, this method assumes that the URI is not
	 * disallowed, and returns false.
	 *
	 * @throws IOException
	 * @throws HttpException
	 */
	public boolean isDisallowed(URI uri) throws HttpException, IOException {
		String hostname = uri.getHost();
		List<String> prevResult = robotsTxtCache.get(hostname);
		if (prevResult != null) {
			return isDeniedByRobotsTxtInner(uri, prevResult);
		}

		String robotsTxtUrl = "http://" + hostname + "/robots.txt";
		HttpMethod method = new GetMethod(robotsTxtUrl);

		log.info("Fetching " + robotsTxtUrl);
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				// common case: 404 Not Found
				log.info("Got " + statusCode + " from "+ robotsTxtUrl);
				robotsTxtCache.put(hostname, EMPTY_LIST);
				return false;
			}
			List<String> result = parse(method.getResponseBodyAsStream());
			robotsTxtCache.put(hostname, result);
			return isDeniedByRobotsTxtInner(uri, result);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Returns a list of prefixes disallowed by the User-Agent "*".
	 *
	 * @throws IOException
	 */
	public List<String> parse(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		boolean match = false;
		List<String> disallowed = new ArrayList<String>(10);
		while ((line = reader.readLine()) != null) {
			int commentIdx = line.indexOf('#');
			if (commentIdx != -1) {
				line = line.substring(0, commentIdx);
			}
			line = line.trim();
			if ("".equals(line)) {
				continue;
			}
			if (line.startsWith(ROBOTS_TXT_USER_AGENT)) {
				String value = line.substring(ROBOTS_TXT_USER_AGENT.length()).trim();
				match = "*".equals(value) || "anchor-nautilus".equalsIgnoreCase(value);
			} else if (line.startsWith(ROBOTS_TXT_DISALLOW)) {
				if (match) {
					String value = line.substring(ROBOTS_TXT_DISALLOW.length()).trim();
					if (StringUtils.isNotEmpty(value)) {
						disallowed.add(value);
					}
				}
			}
		}
		reader.close();
		return disallowed;
	}
}
