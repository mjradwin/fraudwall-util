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
package com.fraudwall.util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.AnchorFatalError;
import com.fraudwall.util.FWProps;

/**
 * Utilities for creating MySQL database connections based on Anchor
 * configuration properties. Where one of these values is not specified
 * explicitly (e.g., as a method parameter), these methods use the following
 * property values to determine the host name, database name, user
 * name, and user password when connecting to the database:
 * <ul>
 * <li>appName<code>.db.host</code></li>
 * <li>appName<code>.db.name</code></li>
 * <li>appName<code>.db.user</code></li>
 * <li>appName<code>.db.password</code></li>
 * </ul>
 * <p>
 * In general, these methods should not be called directly by clients.
 * To save on the cost of creating database connections, use the
 * {@link ConnectionPool} class instead.
 *
 * @see FWProps
 * @see ConnectionPool
 * @author Allan Heydon
 */
public final class DBConnectionUtils {

	// Database property names
	private static final String DB_HOST_PROP_NAME = "host";
	private static final String DB_NAME_PROP_NAME = "name";
	private static final String DB_USER_PROP_NAME = "user";
	private static final String DB_PASSWORD_PROP_NAME = "password";

	private static final Log log = LogFactory.getLog(DBConnectionUtils.class);

	private static Map<String,DBConnectionUtils> utils = new HashMap<String,DBConnectionUtils>();

	/**
	 * This class keeps a database name, which is initially null. If null, the
	 * value of the "&lt;appName&gt;.db.name" property is used, but the
	 * {@link #setDbName()} method may be used to change it.
	 */
	private String dbDatabaseName = null;

	/* Cached representation of the default JDBC URL. This cached value must
	 * be cleared when the _dbDatabaseName is modified.
	 */
	private String defaultJdbcUrl = null;

	/* The database type prefix, used to distinguish different databases
	 * needed.
	 */
	private final String dbTypePrefix;

	static {
		try {
			log.debug("Initializing the database driver.");
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			throw new RuntimeException("Unable to instantiate JDBC driver.", e);
		}
	}

	private DBConnectionUtils(String appName) {
		this.dbTypePrefix = appName + ".db.";
	}

	/**
	 * Returns the {@link DBConnectionUtils} object for the given application.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 *
	 * @see #getDefaultInstance()
	 */
	public static synchronized DBConnectionUtils getInstance(String appName) {
		if (appName == null) {
			return getDefaultInstance();
		}
		DBConnectionUtils dbutils = utils.get(appName);
		if (dbutils == null) {
			dbutils = new DBConnectionUtils(appName);
			utils.put(appName,dbutils);
		}
		return dbutils;
	}

	/**
	 * Returns the {@link DBConnectionUtils} object for the default application,
	 * namely, the one passed to {@link FWProps#initialize(String, String)}).
	 *
	 * @see #getInstance(String)
	 */
	public static synchronized DBConnectionUtils getDefaultInstance() {
		return getInstance(FWProps.getApplicationName());
	}

	/**
	 * Returns the the MySQL host name from the properties
	 * file (namely, the value of the "db.host" property).
	 */
	public String getDbHostName() {
		return FWProps.getProperty(dbTypePrefix + DB_HOST_PROP_NAME);
	}

	/**
	 * Returns the the current MySQL database name, initially set
	 * from the properties file (namely, the value of the "db.name"
	 * property).
	 *
	 * @see #setDbDatabaseName(String)
	 */
	public synchronized String getDbDatabaseName() {
		if (dbDatabaseName == null) {
			dbDatabaseName = FWProps.getProperty(dbTypePrefix + DB_NAME_PROP_NAME);
			if (log.isDebugEnabled()) {
				log.debug(dbTypePrefix + DB_NAME_PROP_NAME + "=" + dbDatabaseName);
			}
		}
		return dbDatabaseName;
	}

	/**
	 * Sets the database name used by {@link #getDefaultJdbcUrl()} and
	 * by all Connection-creation methods that aren't parameterized by
	 * an explicit JDBC URL or database name.
	 *
	 * @see #getDbDatabaseName()
	 * @see #getDefaultJdbcUrl()
	 * @see #createConnection()
	 */
	public synchronized void setDbDatabaseName(String dbName) {
		dbDatabaseName = dbName;
		defaultJdbcUrl = null;
	}

	/**
	 * Returns the the MySQL user name from the properties
	 * file (namely, the value of the "db.user" property).
	 */
	public String getDbUserName() {
		return FWProps.getProperty(dbTypePrefix + DB_USER_PROP_NAME);
	}

	/**
	 * Returns the the MySQL password from the properties
	 * file (namely, the value of the "db.password" property).
	 */
	public String getDbPassword() {
		return FWProps.getProperty(dbTypePrefix + DB_PASSWORD_PROP_NAME);
	}

	/**
	 * Returns the JDBC URL for connecting to the MySQL database
	 * named <code>dbName</code> on the host named <code>hostName</code>.
	 *
	 * @see #getJdbcUrl
	 */
	public synchronized String getDefaultJdbcUrl() {
		if (defaultJdbcUrl == null) {
			defaultJdbcUrl = getJdbcUrl(getDbHostName(), getDbDatabaseName());
		}
		return defaultJdbcUrl;
	}

	/**
	 * Returns the JDBC URL for connecting to the MySQL database named
	 * <code>dbName</code> on the host named <code>hostName</code>.
	 *
	 * @param hostName
	 *            Name of the host running the MySQL database server/daemon to
	 *            connect to.
	 * @param dbName
	 *            Name of the database to connect to. May be <code>null</code>,
	 *            in which case a connection is not made to any particular named
	 *            database on the given host.
	 * @see #getDefaultJdbcUrl
	 */
	/*test*/ static String getJdbcUrl(String hostName, String dbName) {
		StringBuilder url = new StringBuilder();
		url.append("jdbc:mysql://").append(hostName).append("/");
		if (dbName != null) {
			url.append(dbName);
		}
		url.append("?netTimeoutForStreamingResults=0");
		url.append("&zeroDateTimeBehavior=convertToNull");
		url.append("&characterEncoding=UTF-8");
		return url.toString();
	}

	/**
	 * Returns a new connection to the MySQL database. The <code>db.host</code>,
	 * <code>db.name</code>, <code>db.user</code>, and <code>db.password</code>
	 * properties are used to determine the connection parameters.
	 * <p>
	 * Logs a fatal error and calls {@link System#exit} if the connection
	 * attempt failed.
	 *
	 * @see #createConnection(String)
	 * @see #createConnection(String, String, String)
	 * @see #createConnection(String, String, String, String)
	 */
	/*package*/ Connection createConnection() {
		return createConnection(getDbDatabaseName());
	}

	/**
	 * Returns a new connection to the MySQL database named <code>dbName</code>.
	 * The <code>db.host</code>, <code>db.user</code>, and <code>db.password</code>
	 * properties are used to determine the rest of the connection parameters.
	 * <p>
	 * Logs a fatal error and calls {@link System#exit} if the connection
	 * attempt failed.
	 *
	 * @param dbName
	 *            Name of the database to connect to. May be <code>null</code>,
	 *            in which case a connection is not made to any particular named
	 *            database on the given host.
	 * @see #createConnection()
	 * @see #createConnection(String, String, String)
	 * @see #createConnection(String, String, String, String)
	 */
	public Connection createConnection(String dbName) {
		return createConnection(getJdbcUrl(getDbHostName(), dbName), getDbUserName(), getDbPassword());
	}

	/**
	 * Returns a new connection to the MySQL database named <code>dbName</code>
	 * on the host named <code>hostName</code>, connecting via the given
	 * <code>userName</code> and <code>password</code>.
	 * <p>
	 * Logs a fatal error and calls {@link System#exit} if the connection
	 * attempt failed.
	 *
	 * @param dbName
	 *            Name of the database to connect to. May be <code>null</code>,
	 *            in which case a connection is not made to any particular named
	 *            database on the given host.
	 * @see #createConnection()
	 * @see #createConnection(String)
	 * @see #createConnection(String, String, String)
	 * @see #getJdbcUrl
	 */
	public Connection createConnection(String hostName, String dbName, String userName, String password) {
		return createConnection(getJdbcUrl(hostName, dbName), userName, password);
	}

	/**
	 * Returns a new connection on the given JDBC <code>url</code>, connecting
	 * via the given <code>userName</code> and <code>password</code>.
	 * <p>
	 * Logs a fatal error and calls {@link System#exit} if the connection
	 * attempt failed.
	 *
	 * @see #createConnection()
	 * @see #createConnection(String)
	 * @see #createConnection(String, String, String, String)
	 */
	static Connection createConnection(String url, String userName, String password) {
		if (log.isDebugEnabled()) {
			log.debug("Getting connection to: " + url);
		}
		try {
			Connection connection = DriverManager.getConnection(url, userName, password);
			checkDatabaseTimezoneIsUTC(connection);
			if (FWProps.getBooleanProperty("dbconnectionutils.strictUtf8")) {
				checkDatabaseCharacterEncodingIsUtf8(connection, url);
			}
			return connection;
		} catch (SQLException ex) {
			String message = "Unable to connect to database\n\tURL = " + url + "\n\tuser = " + userName;
			throw new AnchorFatalError(message, ex);
		}
	}

	private static void checkDatabaseTimezoneIsUTC(Connection connection) {
		try {
			checkDBResult(connection, "SELECT UNIX_TIMESTAMP('1970-01-01 00:00:00')", 0);
			checkDBResult(connection, "SELECT UNIX_TIMESTAMP('2008-11-03 21:00:00')", 1225746000L);
		} catch (SQLException e) {
			throw new AnchorFatalError("Unable to determine if the DB is in UTC", e);
		}
		if (!TimeZone.getDefault().getID().equals("UTC")) {
			throw new AnchorFatalError("This host is not in UTC :" + TimeZone.getDefault().getID());
		}
	}

	private static void checkDatabaseCharacterEncodingIsUtf8(Connection connection, final String url) {
		try {
			DBUtils.runSql(connection, /*largeResults=*/false,
				new SQLResultsProcessorClosure("SHOW VARIABLES LIKE 'character\\_set\\_%'") {
					@Override protected void processResult(AnchorResultSet rs, int count) throws SQLException {
						String var = rs.getString(1);
						if (!StringUtils.equals("character_set_filesystem", var) &&
							!StringUtils.equals("character_set_results", var)) {
							String val = rs.getString(2);
							if (! StringUtils.equals("utf8", val)) {
								throw new AnchorFatalError("Database " + url + " is not in UTF-8: " + var + " = '" + val + "'");
							}
						}
					}
				}
			);
		} catch (SQLException e) {
			throw new AnchorFatalError("Unable to determine if the DB is in UTF8", e);
		}
	}

	private static void checkDBResult(Connection connection, String sql, long expectedValue) throws SQLException {
		Statement stmt = connection.createStatement();
		try {
			AnchorResultSet rs = AnchorResultSet.create(stmt, sql);
			if (!rs.next()) {
				throw new AnchorFatalError("Unable to check timezone of the database.");
			}
			if (rs.getLong(1) != expectedValue) {
				throw new AnchorFatalError("Database is not in UTC.");
			}
		} finally {
			stmt.close();
		}
	}

	/**
	 * Closes the connection logging any exceptions
	 *
	 * @param conn the connection to close
	 * @return true if closed without exceptions, false otherwise
	 */
	public static boolean closeConnection(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
				return true;
			}
		} catch (Throwable t) {
			log.error("Unable to close connection.", t);
		}
		return false;
	}

	/**
	 * Closes the statement logging any exceptions
	 *
	 * @param stmt the statement to close
	 * @return true if closed without exceptions, false otherwise
	 */
	public static boolean closeStatement(Statement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
				return true;
			}
		} catch (Throwable t) {
			log.error("Unable to close the statement.", t);
		}
		return false;
	}
}
