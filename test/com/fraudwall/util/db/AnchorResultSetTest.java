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
import java.text.ParseException;

import com.fraudwall.util.AbstractPropsTest;
import com.fraudwall.util.Utilities;
import com.fraudwall.util.date.AnchorDateFormat;
import com.fraudwall.util.db.AnchorResultSet;
import com.fraudwall.util.db.ConnectionPool;

public class AnchorResultSetTest extends AbstractPropsTest {

	private static final String DATE_TIME_STR = "2008-01-01 23:45:12";
	private static final String DATE_STR = "2008-01-05";
	private String dbName;

	@Override
	protected void setUp() throws Exception {
		setUp(DEFAULT, TEST_CUSTOMER);
		dbName =  Utilities.getCurrentUser() + "anchor_result_set_test";
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(dbName);
		ConnectionPool.getInstance().stopPool();
		DBUtils.dropDB(dbName);
		DBUtils.createDB(dbName);
		DBUtils.runSql("CREATE TABLE foo (d date, dt datetime);");
		DBUtils.runSql(String.format("INSERT INTO foo VALUES ('%s', '%s')", DATE_STR, DATE_TIME_STR));

		// clear the connection cache so we don't re-use a DB from a prior test
		ConnectionPool.getInstance().stopPool();
	}

	@Override
	protected void tearDown() throws Exception {
		DBUtils.dropDB(dbName);

		// stop the pool used for this DB so as not to affect future tests
		ConnectionPool.getInstance().stopPool();

		// restore default database name to use name specified in prop file
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(null);

		super.tearDown();
	}

	public void testGetDateReturnsCorrectValueWhenColumnValueIsDate() throws SQLException, ParseException {
		Connection conn = ConnectionPool.getInstance().checkout();
		try {
			Statement stmt = conn.createStatement();
			try {
				ResultSet rs = stmt.executeQuery("SELECT d FROM foo");
				rs.next();
				assertEquals(new AnchorDateFormat("yyyy-MM-dd").parse(DATE_STR), rs.getDate(1));
			} finally {
				stmt.close();
			}
		} finally {
			ConnectionPool.getInstance().checkin(conn);
		}
	}

	public void testGetDateReturnsCorrectValueWhenColumnValueIsDateTime() throws SQLException, ParseException {
		Connection conn = ConnectionPool.getInstance().checkout();
		try {
			Statement stmt = conn.createStatement();
			try {
				ResultSet rs = stmt.executeQuery("SELECT dt FROM foo");
				rs.next();
				assertEquals(new AnchorDateFormat("yyyy-MM-dd HH:mm:ss").parse(DATE_TIME_STR), rs.getTimestamp(1));
			} finally {
				stmt.close();
			}
		} finally {
			ConnectionPool.getInstance().checkin(conn);
		}
	}

	public void testGetDateReturnsNullWhenColumnValueIsNull() throws Exception {
		ResultSetMock resultSetMock = new ResultSetMock(null, new String[]{DATE_STR});
		resultSetMock.next();
		assertEquals(new AnchorDateFormat("yyyy-MM-dd").parse(DATE_STR), new AnchorResultSet(resultSetMock).getDate(1));
	}

	public void testGetDateReturnsNullWhenColumnValueIsDateTime() throws SQLException, ParseException {
		ResultSetMock resultSetMock = new ResultSetMock(null, new String[]{DATE_TIME_STR});
		resultSetMock.next();
		assertEquals(
			new AnchorDateFormat("yyyy-MM-dd HH:mm:ss").parse(DATE_TIME_STR),
			new AnchorResultSet(resultSetMock).getDate(1));
	}

}
