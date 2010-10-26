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

import org.apache.commons.httpclient.URIException;


public class CanonicalUrlTest extends AbstractAnchorTest {

	// =================================================== canonicalizeUrl

	public void testCanonicalizeUrlWhenNullOrEmptyUrl() throws Exception {
		checkNullOrEmptyUrl(null);
		checkNullOrEmptyUrl("   ");
	}

	private void checkNullOrEmptyUrl(String url) {
		try {
			new CanonicalUrl(url);
			fail();
		} catch (URIException e) {
			// expected case
			assertEquals("String argument 'url' is null, empty, or all whitespace characters.",
				e.getMessage());
		}
	}

	public void testCanonicalizeUrlWhenUrlHasSurroundingWhiteSpace() throws Exception {
		String url = "http://www.anchorintelligence.com/";
		String actual = new CanonicalUrl("   " + url + "       ").toString();
		assertEquals(url, actual);
	}

	public void testCanonicalizeUrlWhenUrlHasNoPath() throws Exception {
		String url = "http://www.anchorintelligence.com/";
		String actual = new CanonicalUrl(url.substring(0, url.length()-1)).toString();
		assertEquals(url, actual);
	}

	public void testCanonicalizeUrlWhenUrlHasPathNoChange() throws Exception {
		String url = "http://www.anchorintelligence.com/foo.bar";
		String actual = new CanonicalUrl(url).toString();
		assertEquals(url, actual);
	}

	// For resiliency, programs interpreting URI should treat upper case letters as equivalent to lower case in scheme names
	public void testCanonicalizeUrlLowersSchemeCaseWhenSchemeMixedCase() throws Exception {
		String actual = new CanonicalUrl("hTtP://www.anchorintelligence.com/").toString();
		assertEquals("http://www.anchorintelligence.com/", actual);
	}

	public void testCanonicalizeUrlLowersDomainCaseWhenDomainMixedCase() throws Exception {
		String actual = new CanonicalUrl("hTtP://www.anchoRintelLigence.com/").toString();
		assertEquals("http://www.anchorintelligence.com/", actual);
	}

	public void testCanonicalizeUrlPreservesPortWhenSpecified() throws Exception {
		String url = "http://www.anchorintelligence.com:8001/";
		String actual = new CanonicalUrl(url).toString();
		assertEquals(url, actual);
	}

	public void testCanonicalizeUrlStripsPortWhenDefaultForHttp() throws Exception {
		String actual = new CanonicalUrl("http://www.anchorintelligence.com:80/").toString();
		assertEquals("http://www.anchorintelligence.com/", actual);
	}

	public void testCanonicalizeUrlStripsPortWhenDefaultForHttps() throws Exception {
		String actual = new CanonicalUrl("https://www.anchorintelligence.com:443/").toString();
		assertEquals("https://www.anchorintelligence.com/", actual);
	}

	public void testCanonicalizeUrlWhenUnsupportedSchemeThrowsException() throws Exception {
		try {
			new CanonicalUrl("ftp://www.anchorintelligence.com/");
			fail();
		} catch (URIException e) {
			// expected case
			assertNotNull(e);
			assertEquals("Unsupported URI Scheme: ftp", e.getMessage());
		}
	}

	public void testCanonicalizeUrlWhenQueryExistsReturnsCorrectUrl() throws Exception {
		String url = "http://www.anchorintelligence.com/foo.bar?foo&bar";
		String actual = new CanonicalUrl(url).toString();
		assertEquals(url, actual);
	}

	public void testCanonicalizeUrlWhenQueryContainsSpaceSucceeds() throws Exception {
		String url = "http://www.anchorintelligence.com/foo.bar?foo=two words";
		String actual = new CanonicalUrl(url).toString();
		assertEquals("http://www.anchorintelligence.com/foo.bar?foo=two%20words", actual);
	}

	public void testCanonicalizeUrlWhenQueryContainsUrlEncodedSucceeds() throws Exception {
		String url = "http://www.anchorintelligence.com/foo.bar?foo=two%20words";
		String actual = new CanonicalUrl(url).toString();
		assertEquals(url, actual);
	}

	public void testCanonicalizeUrlWhenQueryContainsUrlEncodedPlusSucceeds() throws Exception {
		String url = "http://www.anchorintelligence.com/foo.bar?foo=two+words";
		String actual = new CanonicalUrl(url).toString();
		assertEquals(url, actual);
	}

	public void testCanonicalizeUrlWhenFragmentExistsReturnsCorrectUrl() throws Exception {
		String url = "http://www.anchorintelligence.com/foo.bar#foobar";
		String actual = new CanonicalUrl(url).toString();
		assertEquals(url, actual);
	}

	public void testCanonicalizeUrlWhenFragmentExistsAndResetReturnsCorrectUrl() throws Exception {
		String url = "http://www.anchorintelligence.com/foo.bar";
		CanonicalUrl canonicalUrl = new CanonicalUrl(url + "#foobar");
		canonicalUrl.setFragment(null);
		assertEquals(url, canonicalUrl.toString());
	}

	public void testRandomUrl() throws Exception {
		String url = "http://r.webring.com/hub?ring=proana&id=12&ac=YLK6w{tY?????????????????voa";
		String actual = new CanonicalUrl(url).toString();
		assertEquals("http://r.webring.com/hub?ring=proana&id=12&ac=YLK6w%7BtY?????????????????voa", actual);
	}
}
