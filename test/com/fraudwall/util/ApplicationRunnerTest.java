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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


public class ApplicationRunnerTest extends AbstractPropsTest {

	//----------------------------------------------- getHostNameAlias

	public void testGetHostNameAliasThrowsIfAppCodePropertyIsUndefined() {
		try {
			makeRunner("UnknownApp", TEST_CUSTOMER).getHostNameAlias();
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("No such property: \"application.code.UnknownApp\".", ex.getMessage());
		}
	}

	public void testGetHostNameAliasThrowsIfCustomerCodePropertyIsUndefined() {
		try {
			makeRunner(DEFAULT, "UnknownCust").getHostNameAlias();
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("No such property: \"customer.code.UnknownCust\".", ex.getMessage());
		}
	}

	public void testGetHostNameAliasReturnsCorrectHostName() {
		checkGetHostNameAliasReturnsCorrectHostName("def.ag.prod", DEFAULT, TEST_CUSTOMER, null);
		checkGetHostNameAliasReturnsCorrectHostName("db.ag.prod", "database", TEST_CUSTOMER, null);
		checkGetHostNameAliasReturnsCorrectHostName("db.def.ag.prod", DEFAULT, TEST_CUSTOMER, "db");
	}

	private void checkGetHostNameAliasReturnsCorrectHostName(String host, String appName, String custName, String dbHost) {
		assertEquals(host, makeRunner(appName, custName, dbHost).getHostNameAlias());
	}

	public void testGetHostNameAliasReturnsCorrectHostNameForNonProdEnvironment() {
		assertEquals("def.ag.qa", new ApplicationRunner(DEFAULT, TEST_CUSTOMER, null, "qa").getHostNameAlias());
	}

	//----------------------------------------------- isEnabled

	private final static String TEST_CUST2 = TEST_CUSTOMER + "2", TEST_CUST3 = TEST_CUSTOMER + "3";

	public void testIsEnabledReturnsValueCorrectly() throws UnknownHostException {
		FWPropsTest.setProperty("customer.code." + TEST_CUST2, "ag2");
		FWPropsTest.setProperty("customer.code." + TEST_CUST3, "ag3");
		checkIsEnabledOnlyReturnsTrueForTheCorrectPair(DEFAULT, TEST_CUSTOMER);
		checkIsEnabledOnlyReturnsTrueForTheCorrectPair(DEFAULT, TEST_CUST2);
		checkIsEnabledOnlyReturnsTrueForTheCorrectPair(DEFAULT, TEST_CUST3);
	}

	private void checkIsEnabledOnlyReturnsTrueForTheCorrectPair(String appName, String custName) throws UnknownHostException {
		String host = makeRunner(appName, custName).getHostNameAlias();
		for (String app : new String[] {DEFAULT, DEFAULT, DEFAULT}) {
			for (String cust : new String[] {TEST_CUSTOMER, TEST_CUST2, TEST_CUST3}) {
				assertEquals("Invalid value for: " + appName + "," + custName,
					app.equals(appName) && cust.equals(custName),
					new ApplicationRunnerMock(app, cust, host).isEnabled());
			}
		}

	}

	//----------------------------------------------- private helper

	private ApplicationRunner makeRunner(String appName, String custName) {
		return makeRunner(appName, custName, null);
	}

	private ApplicationRunner makeRunner(String appName, String custName, String prefix) {
		return new ApplicationRunner(appName, custName, prefix, ApplicationRunner.DEFAULT_ENVIRONMENT);
	}

	private static class ApplicationRunnerMock extends ApplicationRunner {
		static byte[] bytes = new byte[] {(byte)255, (byte)255, (byte)255, (byte)255};
		String matchingHost;

		public ApplicationRunnerMock(String appName, String custName, String matchingHost) {
			super(appName, custName, null, ApplicationRunner.DEFAULT_ENVIRONMENT);
			this.matchingHost = matchingHost;
		}

		@Override
		InetAddress[] getAllByName(String host) throws UnknownHostException {
			if (host.equals(matchingHost)) {
				List<InetAddress> addrs = Utilities.getInetAddresses();
				return addrs.toArray(new InetAddress[addrs.size()]);
			} else {
				return new InetAddress[] { InetAddress.getByAddress(bytes) };
			}
		}

	}

}
