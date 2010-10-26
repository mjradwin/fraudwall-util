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
/**
 *
 */
package com.fraudwall.util.http.mock;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpServletRequestMock implements HttpServletRequest {

	private Log log = LogFactory.getLog(HttpServletRequestMock.class);

	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Map<String, String[]> parameters = new HashMap<String, String[]>();
	private HttpSessionMock session = new HttpSessionMock();
	private String requestUri;

	public String getAuthType() {
		return null;
	}

	public String getContextPath() {
		return null;
	}

	public Cookie[] getCookies() {
		return null;
	}

	public long getDateHeader(String arg0) {
		return 0;
	}

	public String getHeader(String arg0) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getHeaderNames() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getHeaders(String arg0) {
		return null;
	}

	public int getIntHeader(String arg0) {
		return 0;
	}

	public String getMethod() {
		return null;
	}

	public String getPathInfo() {
		return null;
	}

	public String getPathTranslated() {
		return null;
	}

	public String getQueryString() {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> keys = new ArrayList<String>(parameters.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			for (String value : parameters.get(key)) {
				try {
					if (sb.length() != 0) {
						sb.append("&");
					}
					sb.append(URLEncoder.encode(key, "UTF8")).append("=").append(URLEncoder.encode(value, "UTF8"));
				} catch (UnsupportedEncodingException e) {
					log.warn(e);
				}
			}
		}
		return sb.toString();
	}

	public String getRemoteUser() {
		return null;
	}

	public String getRequestURI() {
		return requestUri;
	}

	public void setRequestURI(String requestUri) {
		this.requestUri = requestUri;
	}

	public StringBuffer getRequestURL() {
		return null;
	}

	public String getRequestedSessionId() {
		return null;
	}

	public String getServletPath() {
		return null;
	}

	public HttpSession getSession() {
		return session;
	}

	public HttpSession getSession(boolean arg0) {
		return session;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	public boolean isRequestedSessionIdValid() {
		return false;
	}

	public boolean isUserInRole(String arg0) {
		return false;
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return null;
	}

	public String getCharacterEncoding() {
		return null;
	}

	public int getContentLength() {
		return 0;
	}

	public String getContentType() {
		return null;
	}

	public ServletInputStream getInputStream() {
		return null;
	}

	public Locale getLocale() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getLocales() {
		return null;
	}

	public String getParameter(String key) {
		if (!parameters.containsKey(key)) {
			return null;
		}
		return parameters.get(key)[0];
	}

	public void setParameter(String key, String value) {
		parameters.put(key, new String[]{value});
	}

	@SuppressWarnings("unchecked")
	public Map getParameterMap() {
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public Enumeration getParameterNames() {
		return null;
	}

	public String[] getParameterValues(String arg0) {
		return null;
	}

	public void removeParameter(String key) {
		parameters.remove(key);
	}

	public String getProtocol() {
		return null;
	}

	public BufferedReader getReader() {
		return null;
	}

	public String getRealPath(String arg0) {
		return null;
	}

	public String getRemoteAddr() {
		return null;
	}

	public String getRemoteHost() {
		return null;
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	public String getScheme() {
		return null;
	}

	public String getServerName() {
		return null;
	}

	public int getServerPort() {
		return 0;
	}

	public boolean isSecure() {
		return false;
	}

	public void removeAttribute(String attr) {
		attributes.remove(attr);
	}

	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	public void setCharacterEncoding(String arg0) {
	}

	public String getLocalAddr() {
		return null;
	}

	public String getLocalName() {
		return null;
	}

	public int getLocalPort() {
		return 0;
	}

	public int getRemotePort() {
		return 0;
	}
}
