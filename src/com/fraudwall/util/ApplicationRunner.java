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

import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runs a particular application for a particular customer, but only if
 * invoked on a machine whose configuration properties indicate that the
 * application should run there. So either this program invokes the
 * application or it is a no-op.
 *
 * @author ryan
 */
public class ApplicationRunner {

	private static final Log log = LogFactory.getLog(ApplicationRunner.class);

 	public static final String USAGE =
 		"ApplicationRunner -application <appName> -customer <custName> [-db] [-env <environment>] [-verbose]";
	public static final String APPLICATION_OPT = "application";
	public static final String CUSTOMER_OPT = "customer";
	public static final String ENVIRONMENT_OPT = "env";
	public static final String HELP_OPT = "help";
	public static final String VERBOSE_OPT = "verbose";
	public static final String PREFIX_OPT = "prefix";
	private static final Options OPTIONS = new Options();
	public static final String DEFAULT_ENVIRONMENT = "prod";

	static {
		OPTIONS.addOption(APPLICATION_OPT, true,
			"Name of the application used to select configuration values");
		OPTIONS.addOption(CUSTOMER_OPT, true,
			"Customer string used to select configuration values");
		OPTIONS.addOption(PREFIX_OPT, true,
			"Optional prefix to be added to the DNS name, e.g: wh or db");
		OPTIONS.addOption(ENVIRONMENT_OPT, true,
			"Environment used to select configuration values (default \"" + DEFAULT_ENVIRONMENT + "\")");
		OPTIONS.addOption(VERBOSE_OPT, false,
			"Print info-level debugging information");
	}

	private final String appName;
	private final String custName;
	private final String environment;
	private final String prefix;

	public ApplicationRunner(String appName, String custName, String prefix, String environment) {
		this.appName = appName;
		this.custName = custName;
		this.environment = environment;
		this.prefix = prefix == null ? "" : prefix + ".";
	}

	/*test*/ boolean isEnabled() throws UnknownHostException {
		String host = getHostNameAlias();
		List<InetAddress> inetAddresses = Utilities.getInetAddresses();
		for (InetAddress addr : getAllByName(host)) {
			if (inetAddresses.contains(addr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Template method for looking up the address of a host.  Will be
	 * overidden in unit tests
	 */
	/*test*/ InetAddress[] getAllByName(String host) throws UnknownHostException {
		return InetAddress.getAllByName(host);
	}

	/**
	 * Returns the DNS alias for the application/customer/environment triple on
	 * which this application was invokes. For example, for KingCrab/AdGuys/production,
	 * this would return the DNS alias "kc.ab.prod".
	 */
	/*test*/ String getHostNameAlias() {
		String appCode = FWProps.getStringProperty("application.code."+ appName);
		String custCode = FWProps.getStringProperty("customer.code." + custName);
		return String.format("%s%s.%s.%s", prefix, appCode, custCode, environment);
	}

	public static void main(String[] args) {
		try {
			CommandDefinition line = new CommandDefinition(args, OPTIONS, USAGE, HELP_OPT);
			if (!line.hasOption(APPLICATION_OPT) || !line.hasOption(CUSTOMER_OPT)) {
				throw new IllegalUsageException(line);
			}
			String appName = line.getOptionValue(APPLICATION_OPT);
			String custName = line.getOptionValue(CUSTOMER_OPT);
			String environment = line.getOptionValue(ENVIRONMENT_OPT, DEFAULT_ENVIRONMENT);
			String prefix = line.getOptionValue(PREFIX_OPT);
			boolean verbose = line.hasOption(VERBOSE_OPT);
			FWProps.initialize(appName, custName);

			ApplicationRunner appRunner = new ApplicationRunner(appName, custName, prefix, environment);
			boolean enabled = false;
			try {
				enabled = appRunner.isEnabled();
			} catch (UnknownHostException e) {
				// Ignored because enabled == false gets handled below
			}

			String hostName = Utilities.getCurrentHost();
			String alias = appRunner.getHostNameAlias();
			if (enabled) {
				if (verbose) {
					log.info(hostName + " == " + alias + " ("
						+ appName + " is enabled for " + custName + ")");
				}
			} else {
				if (verbose) {
					log.info(hostName + " != " + alias + " ("
						+ appName + " is NOT enabled for " + custName + ")");
				}
				System.exit(1);
			}

		} catch (IllegalUsageException e) {
			e.printUsageAndExit();
		}
	}

}
