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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.fraudwall.util.AbstractPropsTest;
import com.fraudwall.util.FWProps;
import com.fraudwall.util.db.ConnectionPool;


public class DBConnectionUtilsTest extends AbstractPropsTest {

	private static final String DB_NAME = "db_connection_utils_test";
	private static final String TABLE_NAME = DB_NAME + ".test_table";
	private static final String JDBC_OPTIONS =
		"?netTimeoutForStreamingResults=0&zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8";

	@Override
	protected void setUp() throws Exception {
		super.setUp(DEFAULT, TEST_CUSTOMER);
	}

	@Override
	protected void tearDown() throws Exception {
		// clear any overridden database name
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(null);

		// clear out any connections created with DB_NAME
		ConnectionPool.getInstance().stopPool();

		super.tearDown();
	}

	public void testGetInstanceReturnsCorrectInstance() {
		DBConnectionUtils def = DBConnectionUtils.getDefaultInstance();
		DBConnectionUtils kc = DBConnectionUtils.getInstance(DEFAULT);
		assertEquals(def,kc);
	}
	// =============================================================== getDbDatabaseName

	public void testGetDbDatabaseNameReturnsDefaultFromPropertyFile() {
		String expectedDbName = FWProps.getStringProperty(DEFAULT + ".db.name");
		String gotDbName = DBConnectionUtils.getDefaultInstance().getDbDatabaseName();
		assertEquals(expectedDbName, gotDbName);
		String unExpectedDbName = FWProps.getStringProperty(DEFAULT + ".db.name");
		assertFalse(unExpectedDbName.equals(gotDbName));
	}

	public void testGetDbDatabaseNameReturnsNamePassedToSetDbDatabaseName() {
		String initDbName = FWProps.getStringProperty(DEFAULT + ".db.name");
		String newDbName = initDbName + "_test";
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(newDbName);
		String gotDbName = DBConnectionUtils.getDefaultInstance().getDbDatabaseName();
		assertEquals(newDbName, gotDbName);
	}

	// =============================================================== setDbDatabaseName

	public void testSetDbDatabaseNameWithNullArgClearsDbNameOverride() {
		// remember initial DB name
		String initDbName = FWProps.getStringProperty(DEFAULT + ".db.name");

		// set new DB name
		String newDbName = initDbName + "_test";
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(newDbName);
		assertEquals(newDbName, DBConnectionUtils.getDefaultInstance().getDbDatabaseName());

		// clear overridden DB name
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(null);

		// check that getDbDatabaseName() returns initial value from prop file
		assertEquals(initDbName, DBConnectionUtils.getDefaultInstance().getDbDatabaseName());
	}

	// =============================================================== getJdbcUrl

	public void testGetJdbcUrl() {
		assertEquals("jdbc:mysql://quux/" + JDBC_OPTIONS, DBConnectionUtils.getJdbcUrl("quux", null));
		assertEquals("jdbc:mysql://foo/bar" + JDBC_OPTIONS, DBConnectionUtils.getJdbcUrl("foo", "bar"));
	}

	// =============================================================== getDefaultJdbcUrl

	public void testGetDefaultJdbcUrl() {
		String dbHost = FWProps.getStringProperty(DEFAULT + ".db.host");
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName("foobar");
		String expected = "jdbc:mysql://" + dbHost + "/foobar" + JDBC_OPTIONS;
		assertEquals(expected, DBConnectionUtils.getDefaultInstance().getDefaultJdbcUrl());
	}

	// =============================================================== testZeroDateTimeBehavior

	public void testNullTimestampConvertedToJavaNullString() throws Exception {
		assertNull(insertAndReadStringValue("TIMESTAMP NULL", "NULL"));
	}

	public void testNullTimestampConvertedToJavaNullTimestamp() throws Exception {
		assertNull(insertAndReadTimestampValue("TIMESTAMP NULL", "NULL"));
	}

	public void testZeroTimestampConvertedToNullTimestamp() throws Exception {
		assertNull(insertAndReadTimestampValue("TIMESTAMP", "0"));
	}

	public void testZeroTimestampConvertedToJavaNullString() throws Exception {
		assertNull(insertAndReadStringValue("TIMESTAMP", "0"));
	}

	public void testNullTimestampConvertedToJavaNullDate() throws Exception {
		assertNull(insertAndReadDateValue("TIMESTAMP NULL", "NULL"));
	}

	public void testZeroTimestampConvertedToNullDate() throws Exception {
		assertNull(insertAndReadDateValue("TIMESTAMP", "0"));
	}

	public void testZeroUnixDatetimeNotConvertedToNullString() throws Exception {
		assertEquals("1970-01-01 00:00:00.0", insertAndReadStringValue("DATETIME", "FROM_UNIXTIME(0)"));
	}

	public void testZeroUnixDatetimeNotConvertedToNullTimestamp() throws Exception {
		assertEquals(new Timestamp(0), insertAndReadTimestampValue("DATETIME", "FROM_UNIXTIME(0)"));
	}

	public void testZeroUnixDatetimeNotConvertedToNullDate() throws Exception {
		assertEquals(new Date(0), insertAndReadDateValue("DATETIME", "FROM_UNIXTIME(0)"));
	}

	public void testNonZeroTimestampConvertedToNonNullString() throws Exception {
		assertEquals("1970-01-01 00:00:01.0", insertAndReadStringValue("TIMESTAMP", "'1970-01-01 00:00:01'"));
		assertEquals("2009-09-11 23:34:45.0", insertAndReadStringValue("TIMESTAMP", "'2009-09-11 23:34:45.6'"));
	}

	public void testNonZeroTimestampConvertedToNonNullTimestamp() throws Exception {
		assertEquals(new Timestamp(makeTimeInMillis(1970, 1, 1, 0, 0, 1, "UTC")),
			insertAndReadTimestampValue("TIMESTAMP", "'1970-01-01 00:00:01'"));
		assertEquals(new Timestamp(makeTimeInMillis(2009, 9, 11, 23, 34, 45, "UTC")),
			insertAndReadTimestampValue("TIMESTAMP", "'2009-09-11 23:34:45.6'"));
	}

	public void testNonZeroTimestampConvertedToNonNullDate() throws Exception {
		// note that the time components have been zeroed out
		assertEquals(new Date(makeTimeInMillis(1970, 1, 1, 0, 0, 0, "UTC")),
			insertAndReadDateValue("TIMESTAMP", "'1970-01-01 00:00:01'"));
		assertEquals(new Date(makeTimeInMillis(2009, 9, 11, 0, 0, 0, "UTC")),
			insertAndReadDateValue("TIMESTAMP", "'2009-09-11 23:34:45.6'"));
	}


	// =============================================================== testUtf8EncodingBehavior

	public static final String ESPANOL = "espa\u00F1ol";
	public static final String CHINESE = "\u4E2D\u6587";
	private static final String SELECT_CHINESE = "SELECT '" + CHINESE + "'";
	private static final String SELECT_ESPANOL = "SELECT '" + ESPANOL + "'";

	public void testEncodingsOfTestDataAreDifferentLengths() throws Exception {
		// Make sure that the test data we selected is encoded differently in
		// both UTF-8 and LATIN 1
		assertFalse(ESPANOL.getBytes("ISO8859_1").length == ESPANOL.getBytes("UTF8").length);
		assertFalse(CHINESE.getBytes("ISO8859_1").length == CHINESE.getBytes("UTF8").length);
	}

	public void testSelectReturnsCorrectString() throws Exception {
		checkSelectReturnsCorrectString(CHINESE, SELECT_CHINESE);
		checkSelectReturnsCorrectString(ESPANOL, SELECT_ESPANOL);
	}

	private void checkSelectReturnsCorrectString(String expected, String sql) throws Exception {
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(DB_NAME);
		DBUtils.createDB(DB_NAME);
		try {
			assertEquals(expected, DBUtils.fetchFirstString(sql));
		} finally {
			DBUtils.dropDB(DB_NAME);
		}
	}

	public void testInsertIntoUtf8TableWritesCorrectData() throws Exception {
		assertEquals(CHINESE, insertAndReadValue("VARCHAR(255)", "'" + CHINESE + "'", 0));
		assertEquals(ESPANOL, insertAndReadValue("VARCHAR(255)", "'" + ESPANOL + "'", 0));
	}

	// =============================================================== helpers

	private static final int STRING_TYPE = 0, TIMESTAMP_TYPE = 1, DATE_TYPE = 2;

	private String insertAndReadStringValue(String sqlType, String insertValue) throws SQLException {
		return (String) insertAndReadValue(sqlType, insertValue, STRING_TYPE);
	}

	private Timestamp insertAndReadTimestampValue(String sqlType, String insertValue) throws SQLException {
		return (Timestamp) insertAndReadValue(sqlType, insertValue, TIMESTAMP_TYPE);
	}

	private Date insertAndReadDateValue(String sqlType, String insertValue) throws SQLException {
		return (Date) insertAndReadValue(sqlType, insertValue, DATE_TYPE);
	}

	private Object insertAndReadValue(String sqlType, String insertValue, int returnType) throws SQLException {
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(DB_NAME);
		DBUtils.createDB(DB_NAME);
		DBUtils.dropTable(TABLE_NAME);
		try {
			DBUtils.runSql("CREATE TABLE " + TABLE_NAME + " (x " + sqlType + ")");
			DBUtils.runSql("INSERT INTO " + TABLE_NAME + " VALUES (" + insertValue + ")");
			Connection conn = DBConnectionUtils.getDefaultInstance().createConnection();
			try {
				Statement stmt = conn.createStatement();
				try {
					boolean isResultSet = stmt.execute("SELECT * FROM " + TABLE_NAME);
					assertTrue(isResultSet);
					ResultSet rs = stmt.getResultSet();
					rs.next();
					switch (returnType) {
						case TIMESTAMP_TYPE: return rs.getTimestamp(1);
						case DATE_TYPE: return rs.getDate(1);
						default: return rs.getString(1);
					}
				} finally {
					stmt.close();
				}
			} finally {
				conn.close();
			}
		} finally {
			DBUtils.dropTable(TABLE_NAME);
			DBUtils.dropDB(DB_NAME);
		}
	}
}
