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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.exc.AnchorFatalError;
import com.fraudwall.util.exc.IllegalUsageException;
import com.fraudwall.util.exc.Require;
import com.fraudwall.util.io.AnchorLineNumberReader;
import com.fraudwall.util.io.IOUtils;

/**
 * Provides support for loading and reading Anchor configuration properties. There are 8
 * levels of properties: defaults, application-specific, customer-specific,
 * application/customer-specific, host-specific, user-specific, user/host-specific,
 * and those given on the command line to the application. See {@link #initialize(String, String)}
 * for details of which property files are loaded.<p>
 *
 * Like Ant properties, FWProps property values may reference other properties using
 * the ${&lt;prop-name&gt;} syntax. For example, with the following property definitions:
 * <pre>
 * adspace=Ad Space
 * adspaces=${adspace}s
 * adspace.report.title=${adspaces} Report
 * </pre>
 * the property value "adspace.report.title" would have the macro-expanded value
 * "Ad Spaces Report". As this example illustrates, a chain of references is supported.
 * Reference cycles are detected and result in an IllegalStateException. Multiple
 * consecutive property references are supported within the same property value
 * (e.g., "${foo} bar ${baz}"), but not nested property references (e.g., "${${foo}}").
 *
 * @author Marc
 * @author Allan Heydon
 */
public class FWProps {

	private static final String DEFAULT_ROOT = "/var/fraudwall";

	private static final Log log = LogFactory.getLog(FWProps.class);

	public static final String ROOT_PROP_NAME = "ROOT";
	private static final String CUSTOMER_CAMEL_PROP_NAME = "Customer";
	private static final String CUSTOMER_CODE_PROP_NAME = "CustomerCode";

	// Property file prefixes
	private static final String UNITTEST = "unittest";
	private static final String USERS_DIR = "users";
	private static final String CUSTS_DIR = "custs";
	private static final String LOCAL = "local";
	private static final String PROD = "prod";
	private static final String L10N_FILE_NAME = "l10n";

	private static final String PROD_HOSTS_PROP_NAME = "prod.hosts";
	private static final String PROPERTIES_SUFFIX = ".properties";

	/**
	 * Name of the boolean valued property that, if true, causes user-specific
	 * property-files not to be read by the {@link #initialize()} method.
	 */
	public static final String IGNORE_USER_PROPS = "ignore.user.properties";

	/**
	 * Name of the boolean valued property that, if true, causes host-specific
	 * property-files not to be read by the {@link #initialize()} method.
	 */
	public static final String IGNORE_HOST_PROPS = "ignore.host.properties";

	/**
	 * Prefix of properties that describe production customers
	 */
	private static final String CUSTOMER_CODE_PREFIX = "customer.code.";

	/**
	 * Suffix of properties that describe applications
	 */
	private static final String DB_HOST_SUFFIX = ".db.host";

	/**
	 * Singleton instance of FWProps to be used by the various static
	 * getter methods.
	 */
	/*test*/ static /*final*/ FWProps INSTANCE;

	/**
	 * The application name, all lower case. Set by
	 * {@link #initialize(String, String)}.
	 */
	/*test*/ final String applicationName;

	/**
	 * The customer name, camel-cased. Set by
	 * {@link #initialize(String, String)}.
	 */
	/*test*/ final String customerName;

	/**
	 * The shortened version of the customer name. Set by
	 * {@link #initialize(String, String)}.
	 */
	/*test*/ final String shortCustomerName;

	/**
	 * The name of the current host.  This should always be null
	 * unless set from {@link #main(String[])} or from a unit test.
	 */
	/*test*/ final String hostName;

	/**
	 * Set to true if this is a production host to enable production properties files.
	 */
	private boolean isProductionHost = false;

	/**
	 * Set to true to ignore host specific properties files.
	 */
	private boolean ignoreHostProperties = false;

	/**
	 * Set to true to ignore user specific properties files.
	 */
	private boolean ignoreUserProperties = false;

	/**
	 * The combination of all read properties files. Set by
	 * {@link #initialize(String, String)}.
	 */
	/*test*/ final Properties allProps;

	public static synchronized void checkForInitialization() {
		if (INSTANCE == null) {
			throw new IllegalStateException("FWProps has not been initialized.");
		}
	}

	/*test*/ FWProps(String applicationName, String customerName, boolean forceProductionHost) {
		this.applicationName = StringUtils.isNotEmpty(applicationName) ? applicationName.toLowerCase() : applicationName;
		this.customerName = customerName;
		hostName = Utilities.getCurrentHost();
		shortCustomerName = StringUtils.isEmpty(customerName) ? customerName :
			com.fraudwall.util.StringUtils.firstComponent(customerName);
		allProps = new Properties();
		ignoreHostProperties = forceProductionHost;
		ignoreUserProperties = forceProductionHost;
		isProductionHost = forceProductionHost;
	}

	/**
	 * Flattens the source Properties into a single new Properties
	 * object that can be serialized out via Properties.store()
	 */
	public static Properties flattenProperties(Properties... sources) {
		Properties dest = new Properties();
		for (Properties src : sources) {
			dest.putAll(src);
		}
		return dest;
	}

	/**
	 * Initializes configuration properties by reading all property files
	 * appropriate for the given application and customer. This method looks for
	 * property files in the "java/config/" directory, denoted below by
	 * &lt;config-dir&gt;.
	 * <p>
	 * Property files are loaded in the following order. Each successive
	 * property file may override properties specified in earlier files.
	 * <ol>
	 * <li>&lt;config-dir&gt;/defaults.properties -- default property file</li>
	 * <li>&lt;config-dir&gt;/&lt;appName&gt;.properties --
	 * application-specific file (e.g., "tigerprawn.properties")</li>
	 * <li>&lt;config-dir&gt;/local.properties -- local property file (should never be checked in,
	 *  generated by internal tools)</li>
	 * <li>&lt;config-dir&gt;/custs/&lt;custName&gt;.properties -- customer-specific
	 * file (e.g., "adguys.properties")</li>
	 * <li>&lt;config-dir&gt;/custs/&lt;custName&gt;.&lt;appName&gt;.properties --
	 * application/customer-specific file (e.g.,
	 * "adguys.tigerprawn.properties")</li>
	 * <li>&lt;config-dir&gt;/&lt;hostName&gt;.properties -- host-specific file
	 * (e.g., "ra.properties")</li>
	 * <li>&lt;config-dir&gt;/custs/&lt;custName&gt;.&lt;hostName&gt;.properties --
	 * host/customer-specific file (e.g.,
	 * "adguys.maine.properties")</li>
	 * <li>&lt;config-dir&gt;/users/&lt;userName&gt;.properties -- user-specific file
	 * (e.g., "marc.properties")</li>
	 * <li>&lt;config-dir&gt;/users/&lt;userName&gt;.&lt;hostName&gt;.properties --
	 * user/host specific file (e.g., "marc.ra.properties")</li>
	 * <li>&lt;config-dir&gt;/unittest.properties -- file loaded only during unit tests</li>
	 * <li>&lt;config-dir&gt;/users/&lt;userName&gt;.unittest.properties -- per-user
	 * file loaded only during unit tests</li>
	 * <li>system properties, as defined on the command line (see
	 * {@link System#getProperties()})</li>
	 * </ol>
	 * In the above, &lt;hostName&gt; denotes the <em>unqualified</em> local host name,
	 * and &lt;userName&gt; denotes the value returned by {@link Utilities#getCurrentUser()}.
	 * <p>
	 * If any of the property files (1) through (5) inclusive defines the
	 * boolean-valued property named by {@link #IGNORE_USER_PROPS} to be true,
	 * then the user specific files (6), (7), and (9) are <em>not</em> loaded.
	 * Files (8) and (9) are loaded only during unit tests, so tests can be configured
	 * to run differently from production configurations.
	 * <p>
	 * At each level, the jar is searched for the appropriate file. If the file
	 * isn't found in the jar, the file system is searched as indicated above.
	 * Note that the jar may contain a local file system reference (e.g., "../conf"),
	 * which can therefore lead to a search in the file system relative to the location
	 * of the jar file.
	 * <p>
	 * <b>Example:</b> When Marc runs the application TigerPrawn for the
	 * AdGuys customer on the host named maine, properties are read from the
	 * following places in the order listed:
	 * <ol>
	 * <li>config/defaults.properties</li>
	 * <li>config/tigerprawn.properties</li>
	 * <li>config/l10n.properties</li>
	 * <li>config/custs/adguys.properties</li>
	 * <li>config/custs/adguys.tigerprawn.properties</li>
	 * <li>config/maine.properties</li>
	 * <li>config/custs/adguys.maine.properties</li>
	 * <li>config/custs/adguys.l10n.properties</li>
	 * <li>config/users/marc.properties</li>
	 * <li>config/users/marc.maine.properties</li>
	 * <li>system properties (specified on the command line)</li>
	 * </ol>
	 *
	 * @param appName
	 *            Case-insensitive name of the application for which property
	 *            files will be read. May be <code>null</code>, in which case
	 *            no application-specific or application/host specific property
	 *            files are read.
	 * @param custName
	 *            Case-sensitive name of the customer for which property files
	 *            will be read. May be <code>null</code>, in which case no
	 *            customer specific property files are read.
	 */
	public static synchronized void initialize(String appName, String custName) {
		initialize(appName, custName, /*forceProductionHost=*/ false);
	}

	/**
	 * Version of initialize that allows the injections o
	 * @param appName
	 *            Case-insensitive name of the application for which property
	 *            files will be read. May be <code>null</code>, in which case
	 *            no application-specific or application/host specific property
	 *            files are read.
	 * @param custName
	 *            Case-sensitive name of the customer for which property files
	 *            will be read. May be <code>null</code>, in which case no
	 *            customer specific property files are read.
	 * @param forceProductionHost
	 *            If true, then production property files will be loaded,
	 *            user properties files and host properties files will be ignored,
	 *            even if the current host is not production.
	 */
	public static synchronized void initialize(String appName, String custName, boolean forceProductionHost) {
		INSTANCE = new FWProps(appName, custName, forceProductionHost);
		INSTANCE.initialize();
	}

	/*test*/ synchronized void initialize() {
		// allow for multiple calls to initialize(),
		// as would happen during multiple JUnit tests

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		String userName = Utilities.getCurrentUser().toLowerCase();
		boolean isUnitTest = Utilities.isCalledFromUnitTest();

		allProps.put(CUSTOMER_CAMEL_PROP_NAME, StringUtils.isEmpty(customerName) ? "UnknownCustomer" : customerName);
		allProps.put(ROOT_PROP_NAME, Utilities.ifNull(System.getenv("ROOT"), DEFAULT_ROOT));

		// pre-load properties specified on the command-line
		// so that prod.hosts, ignore.user.properties and ignore.host.properties
		// can be handled as expected.
		addToAllProps(System.getProperties(), "system");

		// defaults.properties
		addToAllProps(getDefaultProps(), "default");

		isProductionHost = isProductionHost || Boolean.parseBoolean(System.getProperty("isProductionHost")) ||
			ArrayUtils.contains(getProp(PROD_HOSTS_PROP_NAME).split(","), hostName);

		// <appName>.properties
		addToAllProps(getProps(applicationName), "app");

		if (isProductionHost) {
			// <appName>.prod.properties
			addToAllProps(getProps(applicationName, PROD, /*isUser=*/false, /*isHost=*/false), "app-prod");
		}

		// local.properties
		addToAllProps(getProps(LOCAL), LOCAL);

		// l10n.properties
		addToAllProps(getProps(L10N_FILE_NAME), L10N_OPT);

		// <shortCustName>.properties
		Properties customerProperties = getPropsFromDir(CUSTS_DIR, shortCustomerName);
		Require.isTrue(StringUtils.isBlank(customerName) || customerProperties != null, "Unknown customer " + customerName);
		addToAllProps(customerProperties, "shortcust");

		addToAllProps(getPropsFromDir(CUSTS_DIR, customerName), "cust");

		if (isProductionHost) {
			// <shortCustName>.prod.properties
			addToAllProps(getPropsFromDir(CUSTS_DIR, shortCustomerName, PROD), "shortcust-prod");

			// <custName>.prod.properties
			addToAllProps(getPropsFromDir(CUSTS_DIR, customerName, PROD), "cust-prod");
		}

		// <shortCustName>.<appName>.properties
		addToAllProps(getPropsFromDir(CUSTS_DIR, shortCustomerName, applicationName), "shortcust-app");

		// <custName>.<appName>.properties
		addToAllProps(getPropsFromDir(CUSTS_DIR, customerName, applicationName), "cust-app");

		// Set ignoreHostProperties before loading any host specific properties
		setIgnoreHost(ignoreHostProperties || com.fraudwall.util.StringUtils.parseBoolean(getProp(IGNORE_HOST_PROPS)));

		// <hostName>.properties
		addToAllProps(getProps(hostName, /*isUser=*/false, /*isHost=*/true), "host");

		// <shortCustName>.<hostName>.properties
		addToAllProps(
			getPropsFromDir(CUSTS_DIR, shortCustomerName, hostName, /*isUser=*/false, /*isHost=*/true), "shortcust-host");

		// <custName>.<hostName>.properties
		addToAllProps(getPropsFromDir(CUSTS_DIR, customerName, hostName, /*isUser=*/false, /*isHost=*/true), "cust-host");

		// <shortCustName>.l10n.properties
		addToAllProps(getPropsFromDir(CUSTS_DIR, shortCustomerName, L10N_FILE_NAME), "shortcust-l10n");

		// <custName>.l10n.properties
		addToAllProps(getPropsFromDir(CUSTS_DIR, customerName, L10N_FILE_NAME), "cust-l10n");

		// Set ignoreUserProperties before loading any user properties
		setIgnoreUser(ignoreUserProperties || isProductionHost ||  com.fraudwall.util.StringUtils.parseBoolean(getProp(IGNORE_USER_PROPS)));

		// <userName>.properties
		addToAllProps(getPropsFromDir(USERS_DIR, userName, /*isUser=*/true, /*isHost=*/false), "user");

		// <userName>.<hostName>.properties
		addToAllProps(getPropsFromDir(USERS_DIR, userName, hostName, /*isUser=*/true, /*isHost=*/true), "user-host");

		if (isUnitTest) {
			// unittest.properties
			addToAllProps(getProps(UNITTEST), UNITTEST);

			// <host>.unittest.properties
			addToAllProps(getProps(hostName, UNITTEST, /*isUser=*/false, /*isHost=*/true), "host-unittest");

			// <user>.unittest.properties
			addToAllProps(getPropsFromDir(USERS_DIR, userName, UNITTEST, /*isUser=*/true, /*isHost=*/false), "user-unittest");
		}

		// local.properties
		addToAllProps(getProps(LOCAL), LOCAL);

		// let any system properties specified on the command-line
		// override properties that were set in the config files
		addToAllProps(System.getProperties(), "system");

		String custCode = getCustomerCode();
		allProps.put(CUSTOMER_CODE_PROP_NAME, custCode == null ? "" : custCode);

		// expand all ${<prop-name} occurrences in property values
		expandAllMacros(/*INOUT*/ allProps);

		if (log.isDebugEnabled()) {
			log.debug("Final props loaded: " + allProps.toString());
			final String sampleProperty = "geoip.country.filename";
			log.debug("Sample property: " + sampleProperty + "="
					+ allProps.getProperty(sampleProperty));
		}
	}

	/**
	 * Returns true iff the current host is a production host,
	 * that is to say, is in the list of hostnames specified by
	 * the property named {@link #PROD_HOSTS_PROP_NAME}.
	 */
	public static boolean isProductionHost() {
		return INSTANCE.isProductionHost;
	}

	private void addToAllProps(Properties props, String propsType) {
		if (log.isDebugEnabled()) {
			log.debug(propsType + " props: " + props);
		}
		if (props != null) {
			allProps.putAll(props);
		}
	}

	/**
	 * Destructively modifies the values of <code>props</code> by in-line
	 * expanding any occurrence of ${<prop-name>} in a property value with
	 * the (recursively expanded) value of the property named <prop-name>.
	 *
	 * @throws IllegalStateException if a cycle is detected in the expansion,
	 * an opening "${" is not matched by a closing "}", or the string between
	 * the braces does not name an existing property.
	 */
	/*test*/ static void expandAllMacros(/*INOUT*/ Properties props) {
		for (Map.Entry<Object,Object> entry: props.entrySet()) {
			String value = (String) entry.getValue();
			int ix = value.indexOf("${");
			if (ix >= 0) {
				String name = (String) entry.getKey();
				Stack<String> seenProps = new Stack<String>();
				props.setProperty(name, expandAllMacros(name, value, ix, /*INOUT*/ props, /*INOUT*/ seenProps)) ;
			}
		}
	}

	private static String expandAllMacros(
		String name, String value, int startIx,
		/*INOUT*/ Properties props, /*INOUT*/ Stack<String> seenProps)
	{
		Require.isFalse(seenProps.contains(name), "Cycle found among properties: " + seenProps + ".");
		seenProps.push(name);
		StringBuilder sb = new StringBuilder(value.length() * 2);
		int endIx = -1;
		while (startIx >= 0) {
			sb.append(value.substring(endIx + 1, startIx));
			endIx = value.indexOf("}", startIx + 2);
			Require.isFalse(endIx < 0,
				"Opening '${' unmatched by closing '}' in property '" + name + "' with value '" + value + "'.");
			String refName = value.substring(startIx + 2, endIx);
			String refValue = props.getProperty(refName);
			Require.isNotNull(refValue,
				"Unknown property name '" + refName + "' referenced from property '" + name + "' with value '" + value + "'.");
			int refStartIx = refValue.indexOf("${");
			if (refStartIx >= 0) {
				refValue = expandAllMacros(refName, refValue, refStartIx, props, seenProps);
			}
			sb.append(refValue);
			startIx = value.indexOf("${", endIx + 1);
		}
		sb.append(value.substring(endIx + 1));
		seenProps.pop();
		return sb.toString();
	}

	/**
	 * Macro expands all occurrences of ${propname} in the given string. This
	 * method performs only one level of macro expansion. That is, if after all
	 * top-level ${propname} occurrences have been expanded, any ${propname}
	 * occurrences in the resulting string are <em>not</em> expanded.
	 */
	public static String replaceAllProps(String s) {
		int start = s.indexOf("${");
		if (start < 0) {
			return s;
		}
		StringBuilder sb = new StringBuilder(s.length() * 2);
		int end = -1;
		for (; 0 <= start; start = s.indexOf("${", end + 1)) {
			sb.append(s.substring(end + 1, start));
			end = s.indexOf('}', start + 2);
			if (end < 0) {
				throw new IllegalArgumentException("String '" + s + "' contains '${' with no matching '}'.");
			}
			String propName = s.substring(start + 2, end);
			String value = getProperty(propName);
			if (value == null) {
				throw new IllegalArgumentException("String '" + s + "' names an undefined property: " + propName);
			}
			sb.append(value);
		}
		if (end + 1 < s.length()) {
			sb.append(s.substring(end + 1));
		}
		return sb.toString();
	}


	/** Returns the application name (always all lower-case) on which FWProps was initialized. */
	public static String getApplicationName() {
		return INSTANCE.applicationName;
	}

	/**
	 * Returns the customer name passed to {@link #initialize(String, String)}.
	 * By convention, the customer name is CamelCased.
	 */
	public static String getCustomerName() {
		return INSTANCE.customerName;
	}

	/**
	 * Returns this customer code, or <code>null</code> if no customer code is defined
	 * for this customer. The customer code is specified by the property named
	 * "customer.code.&lt;CustomerName&gt;". For example, AdGuys's customer code is
	 * specified by the "customer.code.AdGuys" property to have the value "ab".
	 */
	public static String getCustomerCode() {
		String custCode = INSTANCE.allProps.getProperty("customer.code." + INSTANCE.customerName);
		return StringUtils.isBlank(custCode) ? null : custCode;
	}

	private void setIgnoreHost(boolean ignore) {
		ignoreHostProperties = ignore;
	}

	private void setIgnoreUser(boolean ignore) {
		ignoreUserProperties = ignore;
	}

	//--------------------------------------- helper methods for reading .properties files

	private Properties getPropsFromDir(String subDir, String part1, String part2) {
		return getPropsFromDir(subDir, part1, part2, /*isUser=*/ false, /*isHost=*/false);
	}

	private Properties getPropsFromDir(String subDir, String part1, String part2, boolean isUser, boolean isHost) {
		if (StringUtils.isEmpty(part1) || StringUtils.isEmpty(part2)) {
			return null;
		}
		return getPropsFromDir(subDir, part1 + "." + part2, isUser, isHost);
	}

	private Properties getPropsFromDir(String subDir, String basename) {
		return getPropsFromDir(subDir, basename, /*isUser=*/ false, /*isHost=*/false);
	}

	private Properties getPropsFromDir(String subDir, String basename, boolean isUser, boolean isHost) {
		if (StringUtils.isEmpty(basename)) {
			return null;
		}
		return getProps(getFilename(subDir, basename), isUser, isHost);
	}

	private Properties getProps(String part1, String part2, boolean isUser, boolean isHost) {
		if (StringUtils.isEmpty(part1) || StringUtils.isEmpty(part2)) {
			return null;
		}
		return getProps(part1 + "." + part2, isUser, isHost);
	}

	private Properties getProps(String filename) {
		return getProps(filename, /*isUser=*/ false, /*isHost=*/false);
	}

	private Properties getProps(String filename, boolean isUser, boolean isHost) {
		filename += PROPERTIES_SUFFIX;
		if (StringUtils.isEmpty(filename)) {
			return null;
		}
		if (isUser && ignoreUserProperties) {
			if (log.isDebugEnabled()) {
				log.debug("Property \"ignore.user.properties\" is set; skipping " + filename + ".");
			}
			return null;
		}
		if (isHost && ignoreHostProperties) {
			if (log.isDebugEnabled()) {
				log.debug("Property \"ignore.host.properties\" is set; skipping " + filename + ".");
			}
			return null;
		}
		return loadProperties(filename);
	}

	private static String getFilename(String subDir, String basename) {
		return StringUtils.isEmpty(subDir) ? basename : new File(subDir, basename).toString();
	}

	private Properties getDefaultProps() {
		return loadProperties("defaults" + PROPERTIES_SUFFIX, true);
	}

	private Properties loadProperties(String fileName) {
		return loadProperties(fileName, /*isRequired=*/ false);
	}


	/**
	 * Returns the properties loaded from the specified file. First check in the jar, and if
	 * not found there, look in the file system in the specified directory.
	 *
	 * @param fileName
	 *            Name of file containing properties
	 * @param isRequired
	 *            If true, raise an exception if the fileName cannot be found
	 */
	protected Properties loadProperties(String fileName, boolean isRequired) {
		InputStream in = getPropertyFileAsStream(fileName);
		if (in != null) {
			if (log.isDebugEnabled()) {
				log.debug("found via class.getResourceAsStream: " + fileName);
			}
			try {
				return loadProperties(in);
			} catch (IOException e) {
				if (log.isDebugEnabled()) {
					log.debug("exception loading from class.getResourceAsStream: " + e);
				}
			}
		}
		if (isRequired) {
			log.fatal("Cannot find the property file: " + fileName);
			throw new IllegalArgumentException("cannot find " + fileName);
		}
		return null;
	}

	private InputStream getPropertyFileAsStream(String fileName) {
		// try loading from the class path (jar file or directory)
		if (log.isDebugEnabled()) {
			log.debug("trying to load via getSystemResourceAsStream: " + fileName);
		}
		// Using FWProps().getClass().getResourceAsStream since it's using the class loader that actually loaded
		// the class.  In the case of war files this is a special servlet class loader.
		// Using ClassLoader.getSystemResourceAsStream doesn't work for getting property files from wars.
		return FWProps.class.getResourceAsStream("/" + fileName);
	}

	protected static Properties loadProperties(InputStream in) throws IOException {
		try {
			Properties targetProps = new Properties();
			targetProps.load(in);
			return targetProps; // success!
		} finally {
			in.close();
		}
	}

	private String getProp(String propName) {
		return allProps.getProperty(propName);
	}

	/**
	 * Returns the value of the property named <code>propName</code> as a
	 * String; returns <code>null</code> if the property has not been defined.
	 * All other methods throw {@link IllegalArgumentException} if the property
	 * is not defined.
	 *
	 * @see #getStringProperty(String)
	 * @see #getBooleanProperty(String)
	 * @see #getCharacterProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getTimeProperty(String)
	 */
	public static String getProperty(String propName) {
		checkForInitialization();
		return INSTANCE.getProp(propName);
	}

	/**
	 * Returns the value of the property named {@code optPrefix.propName} if {@code optPrefix}
	 * is non-null and non-blank; otherwise, returns the value of the property named {@code propName}.
	 *
	 * @throws IllegalArgumentException
	 *             if neither of the properties is defined.
	 */
	/*test*/ static String getPropVal(String optPrefix, String propName) {
		checkForInitialization();
		String propValue = null;
		if (StringUtils.isNotBlank(optPrefix)) {
			propValue = getProperty(optPrefix + "." + propName);
		}
		if (propValue == null) {
			propValue = getProperty(propName);
		}
		if (propValue == null) {
			if (StringUtils.isNotBlank(optPrefix)) {
				throw new IllegalArgumentException(
					"No such properties: \"" + optPrefix + "." + propName + "\" or \"" + propName + "\".");
			} else {
				throw new IllegalArgumentException("No such property: \"" + propName + "\".");
			}
		}
		return propValue;
	}

	/**
	 * Returns a representation of the property name {@code optPrefix} concatenated with {@code propName}
	 * for use in error messages.
	 */
	public static String getPropName(String optPrefix, String propName) {
		return StringUtils.isBlank(optPrefix) ? propName : "[" + optPrefix + ".]" + propName;
	}

	/**
	 * Returns true if and only if the property with the given name has a value
	 * of "1" or "true" (case insensitive).
	 *
	 * @throws IllegalArgumentException if the property named <code>propName</code> is undefined.
	 * @see #getBooleanProperty(String, String)
	 */
	public static boolean getBooleanProperty(String propName) {
		return getBooleanProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns true if and only if one of two properties has a value of "1" or
	 * "true" (case insensitive). If {@code optPrefix} is non-null and
	 * non-blank, this method first tries the property named "{@code optPrefix}.{@code propName}",
	 * using the associated value if one is defined. Otherwise, it uses the
	 * value of the property "{@code propName}" with no optional prefix.
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @see #getBooleanProperty(String)
	 */
	public static boolean getBooleanProperty(String optPrefix, String propName) {
		return com.fraudwall.util.StringUtils.parseBoolean(getPropVal(optPrefix, propName));
	}

	/**
	 * Returns the first character of the required property named <code>propName</code>.
	 * Any characters after the first are ignored.
	 *
	 * @throws IllegalArgumentException if the property named <code>propName</code> is undefined or empty.
	 * @see #getCharacterProperty(String, String)
	 */
	public static char getCharacterProperty(String propName) {
		return getCharacterProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns the first character of one of two properties. If {@code optPrefix} is non-null and
	 * non-blank, this method first tries the property named "{@code optPrefix}.{@code propName}",
	 * using the associated value if one is defined. Otherwise, it uses the
	 * value of the property "{@code propName}" with no optional prefix.
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @see #getCharacterProperty(String)
	 */
	public static char getCharacterProperty(String optPrefix, String propName) {
		String s = getStringProperty(optPrefix, propName);
		if (StringUtils.isEmpty(s)) {
			throw new IllegalArgumentException("Character-valued property '" + propName + "' is empty.");
		}
		return s.charAt(0);
	}

	/**
	 * Returns the string value of the property named <code>propName</code>, which
	 * must be defined.
	 *
	 * @throws IllegalArgumentException if the property named <code>propName</code> is undefined.
	 * @see #getProperty(String)
	 * @see #getStringProperty(String, String)
	 */
	public static String getStringProperty(String propName) {
		return getStringProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns the string value of one of two properties. If {@code optPrefix} is non-null and
	 * non-blank, this method first tries the property named "{@code optPrefix}.{@code propName}",
	 * returning the associated value if one is defined. Otherwise, it returns the
	 * value of the property "{@code propName}" with no optional prefix.
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @see #getStringProperty(String)
	 */
	public static String getStringProperty(String optPrefix, String propName) {
		return getPropVal(optPrefix, propName);
	}

	/**
	 * Returns the integer value of the property named <code>propName</code>, which
	 * must be defined.
	 *
	 * @throws IllegalArgumentException if the property named <code>propName</code> is undefined.
	 * @throws NumberFormatException if the value of the named property cannot be parsed as an integer.
	 * @see #getIntegerProperty(String, String)
	 */
	public static int getIntegerProperty(String propName) {
		return getIntegerProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns the integer value of one of two properties. If {@code optPrefix} is non-null and
	 * non-blank, this method first tries the property named "{@code optPrefix}.{@code propName}",
	 * returning the associated value if one is defined. Otherwise, it returns the
	 * value of the property "{@code propName}" with no optional prefix.
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @throws NumberFormatException if the used property value cannot be parsed as an integer.
	 * @see #getIntegerProperty(String)
	 */
	public static int getIntegerProperty(String optPrefix, String propName) {
		return Integer.parseInt(getPropVal(optPrefix, propName));
	}

	/**
	 * Returns the long value of the property named <code>propName</code>, which
	 * must be defined.
	 *
	 * @throws IllegalArgumentException if the property named <code>propName</code> is undefined.
	 * @throws NumberFormatException if the value of the named property cannot be parsed as a long.
	 * @see #getLongProperty(String, String)
	 */
	public static long getLongProperty(String propName) {
		return getLongProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns the long value of one of two properties. If {@code optPrefix} is non-null and
	 * non-blank, this method first tries the property named "{@code optPrefix}.{@code propName}",
	 * returning the associated value if one is defined. Otherwise, it returns the
	 * value of the property "{@code propName}" with no optional prefix.
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @throws NumberFormatException if the used property value cannot be parsed as a long.
	 * @see #getLongProperty(String)
	 */
	public static long getLongProperty(String optPrefix, String propName) {
		return Long.parseLong(getPropVal(optPrefix, propName));
	}

	/**
	 * Returns the double value of the property named <code>propName</code>, which
	 * must be defined.
	 *
	 * @throws IllegalArgumentException if the property named <code>propName</code> is undefined.
	 * @throws NumberFormatException if the value of the named property cannot be parsed as a double.
	 * @see #getDoubleProperty(String, String)
	 */
	public static double getDoubleProperty(String propName) {
		return getDoubleProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns the double value of one of two properties. If {@code optPrefix} is non-null and
	 * non-blank, this method first tries the property named "{@code optPrefix}.{@code propName}",
	 * returning the associated value if one is defined. Otherwise, it returns the
	 * value of the property "{@code propName}" with no optional prefix.
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @throws NumberFormatException if the used property value cannot be parsed as a double.
	 * @see #getDoubleProperty(String)
	 */
	public static double getDoubleProperty(String optPrefix, String propName) {
		return Double.parseDouble(getPropVal(optPrefix, propName));
	}

	/**
	 * Returns the floating-point value of the property named <code>propName</code>, which
	 * must be defined.
	 *
	 * @throws IllegalArgumentException if the property named <code>propName</code> is undefined.
	 * @throws NumberFormatException if the value of the named property cannot be parsed as a float.
	 * @see #getFloatProperty(String, String)
	 */
	public static float getFloatProperty(String propName) {
		return getFloatProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns the floating-point value of one of two properties. If
	 * {@code optPrefix} is non-null and non-blank, this method first tries the
	 * property named "{@code optPrefix}.{@code propName}", returning the
	 * associated value if one is defined. Otherwise, it returns the value of
	 * the property "{@code propName}" with no optional prefix.
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @throws NumberFormatException if the used property value cannot be parsed as a float.
	 * @see #getFloatProperty(String)
	 */
	public static float getFloatProperty(String optPrefix, String propName) {
		return Float.parseFloat(getPropVal(optPrefix, propName));
	}

	/**
	 * Return the number of milliseconds designated in the value of the property
	 * named <code>propName</code>. The property must have the format "#c", where "#"
	 * is a decimal number and "c" is one of "w" (weeks), "d" (days), "h" or "H" (hours),
	 * "m" (minutes) and "s" (seconds). For example the value "25m" is interpreted by this
	 * method as 25 minutes, which is 25 * 60 * 1000 = 1500000 milliseconds.
	 *
	 * @throws IllegalArgumentException if the property named <code>propName</code> is undefined
	 * or does not have the required format.
	 * @see #getTimeProperty(String, String)
	 */
	public static long getTimeProperty(String propName) {
		return getTimeProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns the number of milliseconds designated by of one of two
	 * properties. If {@code optPrefix} is non-null and non-blank, this method
	 * first tries the property named "{@code optPrefix}.{@code propName}",
	 * using the associated value if one is defined. Otherwise, it uses the
	 * value of the property "{@code propName}" with no optional prefix.
	 * <p>
	 * Whichever property value is used must have the format "#c", where "#" is
	 * a decimal number and "c" is one of "w" (weeks), "d" (days), "h" or "H"
	 * (hours), "m" (minutes), "s" (seconds), or "ms" (milliseconds). For example
	 * the value "25m" is interpreted by this method as 25 minutes, which is
	 * 25 * 60 * 1000 = 1500000 milliseconds.
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @see #getTimeProperty(String)
	 */
	public static long getTimeProperty(String optPrefix, String propName) {
		String propValue = getPropVal(optPrefix, propName);
		try {
			return Utilities.parseDuration(propValue);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("property [" + propName + "] invalid duration", e);
		}
	}

	/**
	 * Assuming the value of the property named <code>propName</code> is a
	 * string of values separated by commas, this returns a list of the
	 * strings between the commas. For example, if the property value was
	 * "gg,aj,yy,gc", the result of this method would be a 4-element list
	 * containing the Strings "gg", "aj", "yy", and "gc".
	 *
	 * @see #getCsvProperty(String, String)
	 */
	public static List<String> getCsvProperty(String propName) {
		return getCsvProperty(/*optPrefix=*/ null, propName);
	}

	/**
	 * Returns the list of values associated with of one of two properties. If
	 * {@code optPrefix} is non-null and non-blank, this method first tries the
	 * property named "{@code optPrefix}.{@code propName}", using the
	 * associated value if one is defined. Otherwise, it uses the value of the
	 * property "{@code propName}" with no optional prefix.
	 * <p>
	 * Whichever property is used, this method expects the property value to be
	 * a comma-separated list, and it returns a list of the strings between the
	 * commas. For example, if the property value was "gg,aj,yy,gc", the result
	 * of this method would be a 4-element list containing the Strings "gg",
	 * "aj", "yy", and "gc".
	 *
	 * @throws IllegalArgumentException if neither of the properties is defined.
	 * @see #getCsvProperty(String)
	 */
	public static List<String> getCsvProperty(String optPrefix, String propName) {
		String csvValue = getStringProperty(optPrefix, propName);
		return Arrays.asList(StringUtils.split(csvValue, ','));
	}

	private static final Pattern PROP_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9._-]+)=.*");

	private void printL10nProps(String outputFileName) throws IOException {
		File l10nFile = new File(L10N_FILE_NAME + PROPERTIES_SUFFIX);
		AnchorLineNumberReader rd = IOUtils.getLineNumberReader(getPropertyFileAsStream(l10nFile.getName()), l10nFile);
		try {
			PrintWriter wr = new PrintWriter(IOUtils.getBufferedWriter(new File(outputFileName)));
			try {
				wr.println("# DO NOT EDIT THIS FILE!");
				wr.println("# Automatically generated via " + FWProps.class.getName());
				wr.println("# by " + Utilities.getCurrentUser().toLowerCase() + " on " + new Date());
				if (StringUtils.isNotBlank(customerName)) {
					wr.println("customer=" + customerName);
				}
				String line;
				while ((line = rd.readLine()) != null) {
					Matcher match = PROP_NAME_PATTERN.matcher(line);
					if (match.matches()) {
						String propName = match.group(1);
						String propValue = allProps.getProperty(propName);
						if (propValue == null) {
							throw new AnchorFatalError("Found no value for property: " + propName);
						}
						wr.print(propName);
						wr.print("=");
						wr.println(propValue);
					}
				}
			} finally {
				wr.close();
			}
		} finally {
			rd.close();
		}
	}

	private void printAllProps(List<String> propNames, boolean valuesOnly) {
		// if no properties were specified explicitly, print them all in sorted order
		if (propNames.isEmpty()) {
			Set<Object> allKeys = allProps.keySet();
			propNames = new ArrayList<String>(allKeys.size());
			for (Object key: allKeys) {
				propNames.add(key.toString());
			}
			Collections.sort(propNames);
		}
		for (String propName : propNames) {
			System.out.println((valuesOnly ? "" : (propName + "=")) + getStringProperty(propName));
		}
	}

	/**
	 * Returns a sorted list of all application names based on the presence of
	 * properties named <code>*.db.host</code>.
	 */
	public static List<String> listApplications() {
		return INSTANCE.getAllApplications();
	}

	private void printAllApplications() {
		List<String> names = getAllApplications();
		for (String name : names) {
			System.out.println(name);
		}
	}

	/**
	 * Returns a sorted list of all application names based on the presence of
	 * properties named <code>*.db.host</code>.
	 */
	/*test*/ List<String> getAllApplications() {
		List<String> appNames = new ArrayList<String>();
		Enumeration<?> propertyNames = allProps.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String name = (String) propertyNames.nextElement();
			if (name.endsWith(DB_HOST_SUFFIX)) {
				String app = name.substring(0, name.length() - DB_HOST_SUFFIX.length());
				appNames.add(app);
			}
		}
		Collections.sort(appNames);
		return appNames;
	}

	/**
	 * Returns a sorted list of all customer names based on the presence of
	 * properties named <code>customer.code.*</code>.
	 *
	 * @see #listCustomerCodes()
	 */
	public static List<String> listCustomers() {
		return INSTANCE.getAllCustomers();
	}

	/**
	 * Returns a sorted list of all customer names based on the presence of
	 * properties named <code>customer.code.*</code>.
	 */
	/*test*/ List<String> getAllCustomers() {
		List<String> customerNames = new ArrayList<String>();
		Enumeration<?> propertyNames = allProps.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String name = (String) propertyNames.nextElement();
			if (name.startsWith(CUSTOMER_CODE_PREFIX)) {
				String customer = name.substring(CUSTOMER_CODE_PREFIX.length());
				if (! "nocust".equals(customer) && ! "demo".equals(customer)) {
					customerNames.add(customer);
				}
			}
		}
		Collections.sort(customerNames);
		return customerNames;
	}

	/**
	 * Returns a list of all customer codes based on the presence of properties
	 * named <code>customer.code.*</code>. The codes are returned in sorted
	 * order by the corresponding full customer names.
	 *
	 * @see #listCustomers()
	 */
	public static List<String> listCustomerCodes() {
		List<String> customerCodes = new ArrayList<String>();
		for (String customerName : listCustomers()) {
			customerCodes.add(getStringProperty(CUSTOMER_CODE_PREFIX + customerName));
		}
		return customerCodes;
	}

	/**
	 * Returns a sorted list of customer names that have the property
	 * <code><i>appName</i>.enabled=true</code>. If <code>appName</code>
	 * is <code>null</code>, returns all customer names.
	 */
	/*test*/ List<String> getApplicationEnabledCustomers(String appName) {
		List<String> customerNames = getAllCustomers();
		if (appName == null) {
			return customerNames;
		}
		List<String> enabled = new ArrayList<String>(customerNames.size());
		for (String customer : customerNames) {
			FWProps customerFwprops = newInstance(appName, customer);
			customerFwprops.initialize();
			if (com.fraudwall.util.StringUtils.parseBoolean(customerFwprops.getProp(appName + ".enabled"))) {
				enabled.add(customer);
			}
		}
		return enabled;
	}

	protected FWProps newInstance(String appName, String customer) {
		return new FWProps(appName, customer, isProductionHost);
	}

	/**
	 * Returns a sorted list of customer names that have the property
	 * <code><i>appName</i>.enabled=true</code>. If <code>appName</code>
	 * is <code>null</code>, returns all customer names.
	 */
	public static List<String> listApplicationEnabledCustomers(String appName) {
		return INSTANCE.getApplicationEnabledCustomers(appName);
	}

	private void printAllCustomers() {
		List<String> customerNames = getApplicationEnabledCustomers(applicationName);
		for (String customer : customerNames) {
			System.out.println(customer);
		}
	}


	/**
	 * Sets the property with given <code>propName</code> to the given
	 * <code>value</code>. <b>This method is intended for use only by unit
	 * tests. If you want to use this method in unit tests use
	 * <code>com.fraudall.util.FWPropsTest.setProperty(String,String)</code>
	 * instead.</b>
	 */
	/*test*/ static void setProperty(String propName, String value) {
		checkForInitialization();
		INSTANCE.allProps.setProperty(propName, value);
	}

	private static final String USAGE = "FWProps [options] <property>*";
	private static final String L10N_OPT = "l10n";
	private static final String VALUE_OPT = "value";
	private static final String HELP_OPT = "help";
	private static final String CUSTOMER_OPT = "customer";
	private static final String APPLICATION_OPT = "application";
	private static final String FORCE_PROD_OPT = "prod";
	private static final String LIST_CUSTOMERS_OPT = "listcustomers";
	private static final String LIST_APPS_OPT = "listapps";

	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.addOption(APPLICATION_OPT, true, "Specify the name of the application");
		options.addOption(CUSTOMER_OPT, true, "Specify the name of the customer");
		options.addOption(HELP_OPT, false, "Display this usage message");
		options.addOption(VALUE_OPT, false, "Only print the value, not the key");
		options.addOption(L10N_OPT, true, "Print out all localization properties only to the named file");
		options.addOption(FORCE_PROD_OPT, false,
			"Force the loading of prod properties files; ignore user and host properties files");
		options.addOption(LIST_CUSTOMERS_OPT, false, "Write a list of customer names to stdout");
		options.addOption(LIST_APPS_OPT, false, "Write a list of application names to stdout");

		try {
			CommandDefinition line = new CommandDefinition(args, options, USAGE, HELP_OPT);
			FWProps.initialize(
				line.getOptionValue(APPLICATION_OPT), line.getOptionValue(CUSTOMER_OPT), line.hasOption(FORCE_PROD_OPT));
			if (line.hasOption(L10N_OPT)) {
				INSTANCE.printL10nProps(line.getOptionValue(L10N_OPT));
			} else if (line.hasOption(LIST_CUSTOMERS_OPT)) {
				INSTANCE.printAllCustomers();
			} else if (line.hasOption(LIST_APPS_OPT)) {
				INSTANCE.printAllApplications();
			} else {
				INSTANCE.printAllProps(Arrays.asList(line.getArgs()), line.hasOption(VALUE_OPT));
			}
		} catch (IllegalUsageException e) {
			e.printUsageAndExit();
		}
	}

}
