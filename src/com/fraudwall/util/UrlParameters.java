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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Breaks up a URL query string into key/value pairs. Similar in spirit
 * to javax.servlet.ServletRequest, but for offline processing.
 */
public class UrlParameters {
	protected HashMap<String,ArrayList<String>> map;

	/**
	 * Construct the parameter map with decode = true
	 */
	public UrlParameters(String queryString) {
		this(queryString, true);
	}

	/**
	 * Construct the parameter map.
	 * @param decode whether to url-decode the parameters
	 * @see Utilities#decodeURL(String)
	 */
	public UrlParameters(String queryString, boolean decode) {
		map = new HashMap<String,ArrayList<String> >();

		if (queryString != null) {
			StringTokenizer st = new StringTokenizer(queryString, "&");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				int eq = token.indexOf('=');
				if (eq != -1) {
					String key = new String(token.substring(0, eq));
					String val = new String(token.substring(eq+1));
					if (decode) {
						key = Utilities.decodeURL(key);
						val = Utilities.decodeURL(val);
					}
					ArrayList<String> values;
					if ((values = map.get(key)) == null) {
						values = new ArrayList<String>();
						map.put(key, values);
					}
					values.add(val);
				}
			}
		}
	}

	public String getParameter(String name) {
		ArrayList<String> values = map.get(name);
		return (values == null) ? null : values.get(0);
	}

	public List<String> getParameterValues(String name) {
		return map.get(name);
	}

	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(map.keySet());
	}
}

/*
 * Local variables:
 * tab-width: 4
 * c-basic-offset: 4
 * End:
 * vim600: noet sw=4 ts=4 fdm=marker
 * vim<600: noet sw=4 ts=4
 */

