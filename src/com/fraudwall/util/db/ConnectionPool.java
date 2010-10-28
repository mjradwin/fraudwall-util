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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.FWProps;
import com.fraudwall.util.exc.Require;
import com.mysql.jdbc.ConnectionImpl;

/**
 * A singleton connection pool.  Maintains an unbounded pool of database
 * connections, grouped by the JDBC URL used to establish the connection.
 * Uses {@link DBConnectionUtils#getDefaultJdbcUrl()} to determine the
 * JDBC URL to use for subsequent connections; a different database name
 * can be selected by calling {@link DBConnectionUtils#setDbDatabaseName}.
 * <p>
 * For proper operation, the {@link FWProps#initialize()}
 * method must have been called before this class is loaded.
 *
 * @author Kate
 * @author Allan Heydon
 */
public final class ConnectionPool implements ConnectionPoolMBean {

	private static final Log log = LogFactory.getLog(ConnectionPool.class);

	private static final long DEFAULT_CONNECTION_IDLE_TIME = DateUtils.MILLIS_PER_HOUR;

	private long connectionIdleTime = DEFAULT_CONNECTION_IDLE_TIME;

	// Map application name to ConnectionPool
	private static Map<String,ConnectionPool> pool =
		new HashMap<String,ConnectionPool>();

	private DBConnectionUtils dbutils;

	// total number of connections (both available and in-use)
	/*package*/ int connections = 0;

	// List of connections available for use
	/*package*/ Queue<ConnectionWrapper> available = new LinkedList<ConnectionWrapper>();

	// List of currently in use connections
	/*package*/ Set<ConnectionWrapper> inUse = new HashSet<ConnectionWrapper>();

	/**
	 * Constructs a new connection pool initialized for the specified application.
	 * <li>appName.db.host
	 * <li>appName.db.name
	 * <li>appName.db.user
	 * <li>appName.db.password
	 * will be read from the properties files
	 *
	 * @see DBConnectionUtils#getDbUserName()
	 * @see DBConnectionUtils#getDbPassword()
	 */
	private ConnectionPool(String appName) {
		dbutils = DBConnectionUtils.getInstance(appName);
	}

	/**
	 * Returns the connection pool for the application name specified
	 * during FWProps initialization.
	 * @return ConnectionPool for the DB associated with this app
	 */
	public static ConnectionPool getInstance() {
		return getInstance(null);
	}

	/**
	 * Returns the connection pool for the specified application.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 * @return ConnectionPool for the DB associated with the given application.
	 */
	public static synchronized ConnectionPool getInstance(String appName) {
		appName = getAppName(appName);
		ConnectionPool c = pool.get(appName);
		if (c == null) {
			c = new ConnectionPool(appName);
			pool.put(appName, c);
		}
		return c;
	}

	/**
	 * Returns <code>appName</code> if non-null; otherwise, returns the default
	 * application name, which is guaranteed to be non-null.
	 */
	public static String getAppName(String appName) {
		if (appName == null) {
			appName = FWProps.getApplicationName();
			Require.isNotNull(appName, "No application name set in FWProps!");
		}
		return appName;
	}

	/**
	 * Return a connection from the connection pool. If there isn't a
	 * connection available, ask the JDBC driver to create a new one.
	 */
	public synchronized Connection checkout() {
		ConnectionWrapper connection;
		while ((connection = getConnectionFromAvailablePool()) == null);
		inUse.add(connection);
		if (log.isDebugEnabled()) {
			log.debug("Checking out a connection.  Connections in use: " + inUse.size() + " " +
				(System.currentTimeMillis() - connection.lastUsedTime) + " ms since last use.");
		}
		return connection.getConnection();
	}

	/**
	 * Either grabs a non stale connection from the queue or if no connections
	 * are available creates a new connection.
	 * <p>
	 * Requires that the mutex associated with this class is held.
	 *
	 * @return a Connection
	 */
	private ConnectionWrapper getConnectionFromAvailablePool() {
		// clean up the connection pool first (remove expired and closed connections from the pool)
		Iterator<ConnectionWrapper> it = available.iterator();
		while (it.hasNext()) {
			ConnectionWrapper wrapper = it.next();
			if (!wrapper.checkConnection()) {
				it.remove();
				connections--;
			}
		}
		ConnectionWrapper connection = available.poll();
		if (connection == null) {
			// create a new connection
			return getConnection();
		} else {
			return connection;
		}
	}

	/**
	 * Internal method to create a new connection using the given JDBC URL.
	 * <p>
	 * Requires that the mutex associated with this class is held.
	 */
	private ConnectionWrapper getConnection() {
		connections++;
		if (log.isDebugEnabled()) {
			log.debug("Creating another database connection.  New number of connections: " + connections);
		}
		Connection connection = dbutils.createConnection();
		if (connection == null)
			throw new RuntimeException("Cannot get database connection");
		return new ConnectionWrapper(connection, connectionIdleTime);
	}

	/**
	 * Call when a connection is no longer needed.
	 */
	public synchronized void checkin(Connection connection) {
		if (connection == null) {
			log.warn("null connection passed to checkin().");
			return;
		}

		// remove connection from inUse pool
		if (!removeConnection(connection)) {
			// throw if connection is not in use
			throw new IllegalArgumentException("checkin called on connection not checked out from this pool");
		}

		// check that all statements on the connection have been closed
		int numActiveStatements = ((ConnectionImpl) connection).getActiveStatementCount();
		Require.isTrue(numActiveStatements == 0,
			"Attempting to checkin a connection with " + numActiveStatements + " active statement(s).");

		// add connection to available pool
		available.add(new ConnectionWrapper(connection, connectionIdleTime));
		if (log.isDebugEnabled()) {
			log.debug("Checking in a connection.  Number of available connections: " + available.size());
		}
	}

	private boolean removeConnection(Connection conn) {
		for (ConnectionWrapper cw : inUse) {
			if (cw.getConnection().equals(conn)) {
				inUse.remove(cw);
				return true;
			}
		}
		return false;
	}

	/**
	 * Closes all connections being managed by this connection
	 * pool, including both in-use and available connections.
	 */
	public synchronized void stopPool() {
		// close all the available connections
		closeConnections(available);
		available.clear();

		// close all the in-use connections
		closeConnections(inUse);
		inUse.clear();

		connections = 0;
	}

	/**
	 * Closes all connections in the given collection. If an error occurs
	 * while closing one of the connections, the error is logged and the
	 * method continues attempting to close the remainder of the connections.
	 * <p>
	 * Requires that the mutex associated with this class is held.
	 */
	private static void closeConnections(Collection<ConnectionWrapper> connections) {
		for (ConnectionWrapper conn : connections) {
			DBConnectionUtils.closeConnection(conn.getConnection());
		}
	}

	/**
	 * Returns the number of available connections for in this connection pool instance.
	 */
	public synchronized int getNumberAvailableConnections() {
		return available.size();
	}

	/**
	 * Returns the total number of available connections across all connection
	 * pool instances. Used by JMX to monitor connection pools.
	 * <p>
	 * MBean methods should be static since they're all about the global state
	 * of all ConnectionPools, but the way beans work, methods can not be
	 * static.
	 */
	public Integer getTotalNumberAvailableConnections() {
		int size=0;
		synchronized (ConnectionPool.class) {
			for (ConnectionPool p : pool.values()) {
				synchronized (p) {
					size += p.available.size();
				}
			}
		}
		return size;
	}

	/**
	 * Returns the total number of in-use connections across all connection
	 * pool instances. Used by JMX to monitor connection pools.
	 * <p>
	 * MBean methods should be static since they're all about the global state
	 * of all ConnectionPools, but the way beans work, methods can not be
	 * static.
	 */
	public synchronized Integer getTotalNumberInUseConnections() {
		int size=0;
		synchronized (ConnectionPool.class) {
			for (ConnectionPool p : pool.values()) {
				synchronized (p) {
					size += p.inUse.size();
				}
			}
		}
		return size;
	}

	public long getConnectionIdleTime() {
		return connectionIdleTime;
	}

	public void setConnectionIdleTime(long connectionIdleTime) {
		this.connectionIdleTime = connectionIdleTime;
	}

	/**
	 * A wrapper around the connection pool that keeps track of when the
	 * connection was last used.
	 */
	/*package*/ static class ConnectionWrapper {
		private final Connection conn;
		private final long connectionIdleTime;
		private final long lastUsedTime;

		private ConnectionWrapper(Connection conn, long connectionIdleTime) {
			this.conn = conn;
			this.connectionIdleTime = connectionIdleTime;
			this.lastUsedTime = System.currentTimeMillis();
		}

		/*package*/ Connection getConnection() {
			return conn;
		}

		/**
		 * Check the connection to see if it is still valid and it has not been idle for too long.
		 * We want to close down idle connection before the socket times out
		 *
		 * @return true if the connection should be used, false if it should be recycled
		 */
		private boolean checkConnection() {
			try {
				boolean connTimedOut = (System.currentTimeMillis() - lastUsedTime) > connectionIdleTime;
				if (connTimedOut && !conn.isClosed()) {
					// try to close down this connection
					conn.close();
				}
				return !connTimedOut && !conn.isClosed();
			} catch (Exception e) {
				log.warn("Exception trying to check the connection", e);
				return false;
			}
		}
	}
}
