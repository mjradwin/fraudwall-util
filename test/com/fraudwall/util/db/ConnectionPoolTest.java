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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;

import com.fraudwall.util.AbstractPropsTest;
import com.fraudwall.util.db.ConnectionPool;
import com.fraudwall.util.db.ConnectionPool.ConnectionWrapper;

/**
 * Tests the {@link ConnectionPool} implementation.
 *
 * @author Allan Heydon
 */
public class ConnectionPoolTest extends AbstractPropsTest {
	private static final String TEST_DB1_SUFFIX = "_conn_pool_test_db1";
	private static final String TEST_DB2_SUFFIX = "_conn_pool_test_db2";

	private static String TEST_DB1_NAME;
	private static String TEST_DB2_NAME;

	private ConnectionPool pool;
	private DBConnectionUtils kcDbUtils;
	private DBConnectionUtils tpDbUtils;

	@Override
	protected void setUp() throws Exception {
		super.setUp(DEFAULT, TEST_CUSTOMER);

		// get pool instance
		pool = ConnectionPool.getInstance();

		// Set up test database names
		kcDbUtils = DBConnectionUtils.getInstance(DEFAULT);
		tpDbUtils = DBConnectionUtils.getInstance(DEFAULT);

		TEST_DB1_NAME = kcDbUtils.getDbDatabaseName() + TEST_DB1_SUFFIX;
		TEST_DB2_NAME = tpDbUtils.getDbDatabaseName() + TEST_DB2_SUFFIX;

		kcDbUtils.setDbDatabaseName(TEST_DB1_NAME);
		tpDbUtils.setDbDatabaseName(TEST_DB2_NAME);

		// drop test databases (if they exist)
		DBUtils.dropDB(TEST_DB1_NAME);
		DBUtils.dropDB(TEST_DB2_NAME);

		// create test databases
		DBUtils.createDB(TEST_DB1_NAME);
		DBUtils.createDB(TEST_DB2_NAME);
	}

	@Override
	protected void tearDown() throws Exception {
		// stop pool
		pool.stopPool();
		pool = null;

		// drop test databases
		DBUtils.dropDB(TEST_DB1_NAME);
		DBUtils.dropDB(TEST_DB2_NAME);

		// restore default database name
		tpDbUtils.setDbDatabaseName(null);
		kcDbUtils.setDbDatabaseName(null);
		kcDbUtils = tpDbUtils = null;

		super.tearDown();
	}

	// =========================================================== getInstance

	public void testGetInstanceUsesDefaultApplication() {
		ConnectionPool def = ConnectionPool.getInstance();
		ConnectionPool kc = ConnectionPool.getInstance(DEFAULT);
		assertSame(def, kc);
	}

	public void testGetInstanceCreatesNewPoolForNewApplication() {
		ConnectionPool def, kc, tp = null;
		try {
			def = ConnectionPool.getInstance();
			kc = ConnectionPool.getInstance(DEFAULT);
			tp = ConnectionPool.getInstance(DEFAULT);
			// default should still be kingcrab
			assertEquals(def, kc);
			// tigerprawn's should be different
			assertFalse(def == tp);
		} finally {
			if (tp != null) tp.stopPool();
		}
	}

	// =========================================================== checkout

	public void testCheckoutIncrementsConnectionCount() {
		int initCount = pool.connections;
		pool.checkout();
		assertEquals(initCount + 1, pool.connections);
		pool.checkout();
		assertEquals(initCount + 2, pool.connections);
	}

	public void testCheckoutReusesAvailableConnection() {
		Connection conn = pool.checkout();
		pool.checkin(conn);
		int initCount = pool.connections;
		pool.checkout();
		assertEquals(initCount, pool.connections);
	}

	public void testCheckoutExpiredAvailableConnection() {
		long timeout = pool.getConnectionIdleTime();
		pool.setConnectionIdleTime(0);
		Connection conn = pool.checkout();
		pool.checkin(conn);
		int initCount = pool.connections;
		pool.checkout();
		assertEquals(initCount, pool.connections);
		assertEquals(pool.getNumberAvailableConnections(), 0);
		pool.setConnectionIdleTime(timeout);
	}

	// =========================================================== checkin

	public void testCheckInDoesNotDecrementConnectionCount() {
		Connection conn = pool.checkout();
		int initCount = pool.connections;
		pool.checkin(conn);
		assertEquals(initCount, pool.connections);
	}

	public void testCheckInReturnsConnectionsToAvailableMap() {
		Connection conn1 = pool.checkout();
		Connection conn2 = pool.checkout();
		assertEquals(0, pool.available.size());
		pool.checkin(conn1);
		Queue<ConnectionWrapper> queue = pool.available;
		assertTrue(queueContainsConnection(queue, conn1));
		assertFalse(queueContainsConnection(queue, conn2));
		pool.checkin(conn2);
		assertTrue(queueContainsConnection(queue, conn1));
		assertTrue(queueContainsConnection(queue, conn2));
	}

	private boolean queueContainsConnection(Queue<ConnectionWrapper> queue, Connection conn) {
		for (ConnectionWrapper cw : queue) {
			if (cw.getConnection().equals(conn)) {
				return true;
			}
		}
		return false;
	}

	public void testCheckInDoesNotCloseConnection() throws SQLException {
		Connection conn1 = pool.checkout();
		assertFalse(conn1.isClosed());
		pool.checkin(conn1);
		assertFalse(conn1.isClosed());
	}

	public void testCheckinThrowsWhenStatementsAreActive() throws Exception {
		Connection conn = pool.checkout();
		Statement stmt = conn.createStatement();
		stmt.executeQuery("SELECT 123");
		try {
			pool.checkin(conn);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Attempting to checkin a connection with 1 active statement(s).", e.getMessage());
		}
	}

	public void testCheckinDoesNotThrowWhenStatementsAreNonActive() throws Exception {
		Connection conn = pool.checkout();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT 123");
		rs.close();
		try {
			pool.checkin(conn);
		} catch (IllegalStateException e) {
			assertEquals("Attempting to checkin a connection with 1 active statement(s).", e.getMessage());
		}
	}

	public void testCheckinDoesNotThrowWhenStatementsAreClosed() throws Exception {
		Connection conn = pool.checkout();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT 123");
		rs.close();
		stmt.close();
		pool.checkin(conn);
	}

	// =========================================================== stopPool

	public void testStopPoolResetsConnectionCount() {
		pool.checkout();
		pool.checkout();
		assertTrue(pool.connections > 0);
		pool.stopPool();
		assertEquals(0, pool.connections);
	}

	public void testStopPoolClearsAvailableAndInUseConnections() {
		// create 1 in-use and 1 available connection
		Connection conn1 = pool.checkout();
		pool.checkout();
		pool.checkin(conn1);
		assertEquals(1, pool.inUse.size());
		assertEquals(1, pool.available.size());

		// invoke stopPool()
		pool.stopPool();

		// check that both maps have been cleared
		assertEquals(0, pool.inUse.size());
		assertEquals(0, pool.available.size());
	}

	public void testStopPoolClosesAvailableAndInUseConnections() throws SQLException {
		// create 1 in-use and 2 available connection
		Connection conn1 = pool.checkout();
		Connection conn2 = pool.checkout();
		Connection conn3 = pool.checkout();
		pool.checkin(conn1);
		pool.checkin(conn2);
		assertFalse(conn1.isClosed());
		assertFalse(conn2.isClosed());
		assertFalse(conn3.isClosed());

		// invoke stopPool()
		pool.stopPool();

		// check that both connections are closed
		assertTrue(conn1.isClosed());
		assertTrue(conn2.isClosed());
		assertTrue(conn3.isClosed());
	}

	public void testMultipleConnectionPools() {
		// check the number of available connections in the pool
		assertEquals(0, pool.getNumberAvailableConnections());

		// checkout a default db connection
		Connection conn = pool.checkout();
		pool.checkin(conn);
		assertEquals(1, pool.getNumberAvailableConnections());

		// checkout a tigerprawn db connection
		ConnectionPool tp = ConnectionPool.getInstance(DEFAULT);
		try {
			assertNotSame(pool, tp);
			assertEquals(0, tp.getNumberAvailableConnections());
			conn = tp.checkout();
			tp.checkin(conn);
			assertEquals(1, pool.getNumberAvailableConnections());
			assertEquals(1, tp.getNumberAvailableConnections());
		} finally {
			if (tp != null) tp.stopPool();
		}
	}
}
