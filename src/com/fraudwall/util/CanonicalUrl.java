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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;

/**
 * Contains utilities for URLs
 *
 * @author kfox
 */
@SuppressWarnings("serial")
public class CanonicalUrl extends URI {

	private static final Map<String, Integer> DEFAULT_PORT = new HashMap<String, Integer>();
	static {
		DEFAULT_PORT.put("http", 80);
		DEFAULT_PORT.put("https", 443);
	}

	/**
	 * Creates a canonicalized url where the url is of the format [scheme:]//[user-info@][host][port][path][?query][#fragment]
	 * based on the following rules:
	 *
	 * <ul>
	 * <li>If the url contains spaces assumes it's unencoded.  Otherwise assumes the url is encoded.
	 * <li>Scheme is lower cased and the only two supported schemes right now are http and https.</li>
	 * <li>The host is lower cased</li>
	 * <li>If the port is not specified, the port is set to the default port (e.g. 80 for http)</li>
	 * <li>If the path is empty, it gets replaced by a single forward slash ("/")<li>
	 * </ul>
	 * @throws URIException if the scheme is not http or https, or if the URI does not have valid syntax.
	 */
	public CanonicalUrl(String urlStr) throws URIException {
		try {
			ArgCheck.isNotBlank(urlStr, "url");
			urlStr = urlStr.trim();
			try {
				// assume first that the url is properly url encoded
				parseUriReference(urlStr, true);
			} catch (URIException ue) {
				// try the url as unencoded
				parseUriReference(urlStr, false);
			}
			checkScheme(getScheme());
			updateHostAndPort();
			updatePath();
			setURI();
		} catch (URIException ue) {
			throw ue;
		} catch (Exception e) {
			throw new URIException(e.getMessage());
		}
	}

	/**
	 * Checks the url scheme.
	 * RFC2396 3.1 - "For resiliency, programs interpreting URI should treat upper case letters as equivalent to lower
	 *  case in scheme names." -- should be taken care of by the URI class
	 * @throws URIException If the scheme is not supported
	 */
	private void checkScheme(String schemeStr) throws URIException {
		if (!DEFAULT_PORT.containsKey(schemeStr)) {
			throw new URIException("Unsupported URI Scheme: " + schemeStr);
		}

	}

	/**
	 * Sets the host section of the url.  RFC1034 3.1 - By convention, domain names can be stored with arbitrary
	 * case, but domain name comparisons for all present domain functions are done in a case-insensitive manner ...
	 * When you receive a domain name or label, you should preserve its case.
	 * Sets the port.  If port number is the default for the scheme, then the port is reset to be the default.
	 */
	private void updateHostAndPort() {
		String rawHost = new String(getRawHost()).toLowerCase();
		ArgCheck.isNotBlank(rawHost, "hostStr");
		_host = rawHost.toCharArray();
		// derive port using the scheme default if unspecified
		_port = _port == DEFAULT_PORT.get(getScheme()) ? -1 : _port;
		String hostportStr = (_port == -1) ? rawHost : rawHost + ":" + _port;
		this._authority = new String((_userinfo == null ? "" : new String(_userinfo) + "@") + hostportStr).toCharArray();
	}

	/**
	 * Updates the path section of the url.  RFC2616 5.1.2 - Note that the absolute path cannot be empty; if none is
	 * present in the original URI, it MUST be given as "/" (the server root).
	 */
	public void updatePath() throws URIException {
		if (StringUtils.isBlank(getPath())) {
			_path = new char[]{'/'};
		}
	}

	@Override
	public String toString() {
		return getEscapedURIReference();
	}

	public long getHash() {
		return fnvHash.fnv64aHash(toString());
	}
}
