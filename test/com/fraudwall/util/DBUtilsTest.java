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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateUtils;


/**
 * Tests the {@link DBUtils} implementation.
 *
 * @author Allan Heydon
 */
public class DBUtilsTest extends AbstractPropsTest {
	private static final String DB_NAME = "db_utils_test";
	private static final String TABLE_NAME = "test_table";
	private static final String CREATE_TABLE_COMMAND =
		"CREATE TABLE " + TABLE_NAME + " (" +
		"x INT NOT NULL COMMENT 'The number of values.', " +
		"PRIMARY KEY (x)" +
		")";

	@Override
	protected void setUp() throws Exception {
		super.setUp(DEFAULT, TEST_CUSTOMER);
		DBUtils.dropDB(DB_NAME); // In case we crashed and this DB already exists
		DBUtils.createDB(DB_NAME);
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(DB_NAME);
		DBUtils.runSql(CREATE_TABLE_COMMAND + " DEFAULT CHARACTER SET utf8");
	}

	@Override
	protected void tearDown() throws Exception {
		DBUtils.dropDB(DB_NAME);
		DBConnectionUtils.getDefaultInstance().setDbDatabaseName(null);
		super.tearDown();
	}

	/**
	 * Prints the values of the global and session SQL mode variables
	 * when accessed through the Java API. This is not a unit test, so
	 * the method name intentionally does NOT start with "test". To run
	 * locally, you can change the name to start with "test".
	 */
	public void printGlobalAndSessionSqlModes() throws Exception {
		printSqlVariable("global.sql_mode");
		printSqlVariable("session.sql_mode");
	}

	private void printSqlVariable(final String varName) throws SQLException {
		String val = DBUtils.fetchFirstString("SELECT @@" + varName);
		System.err.println(varName + " = " + val);
	}

	// ================================================= dropDatabase / createDatabase

	public void testCreateDbThrowsIfDatabaseNameIsNotAllLowerCase() throws Exception {
		try {
			DBUtils.createDB("DBUtilsTest");
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("Database name contains upper-case charater(s): DBUtilsTest", ex.getMessage());
		}
	}
	public void testCreateDbAndDropDbWorkAsExpected() throws SQLException {
		String dbName = genRandomDBName();
		assertFalse(databaseExists(dbName));
		try {
			DBUtils.createDB(dbName);
			assertTrue(databaseExists(dbName));
		} finally {
			DBUtils.dropDB(dbName);
			assertFalse(databaseExists(dbName));
		}
	}

	// ================================================= createTableLike / dropTable

	public void testCreateTableLikeAndDropTableWorkAsExpected() throws Exception {
		String tblName = genRandomTableName();
		assertFalse(tableExists(tblName));
		DBUtils.createTableLike(tblName, TABLE_NAME);
		try {
			assertTrue(tableExists(tblName));
		} finally {
			DBUtils.dropTable(tblName);
			assertFalse(tableExists(tblName));
		}
	}

	public void testDropTableIsANoOpIfTableDoesNotExist() throws Exception {
		String tblName = genRandomTableName();
		assertFalse(tableExists(tblName));
		DBUtils.createTableLike(tblName, TABLE_NAME);
		DBUtils.dropTable(tblName);
		assertFalse(tableExists(tblName));
		DBUtils.dropTable(tblName);
		assertFalse(tableExists(tblName));
	}

	public void testCreateTableLikeThrowsExceptionIfTargetTableAlreadyExists() throws Exception {
		String newTblName = genRandomTableName();
		try {
			DBUtils.createTableLike(newTblName, TABLE_NAME);
			assertTrue(tableExists(newTblName));
			try {
				DBUtils.createTableLike(newTblName, TABLE_NAME);
				fail();
			} catch (SQLException ex) {
				// expected case
				assertEquals("Table '" + newTblName + "' already exists", ex.getMessage());
			}
		} finally {
			DBUtils.dropTable(newTblName);
		}
	}

	public void testCreateTableLikeThrowsExceptionIfSourceTableDoesNotExist() throws Exception {
		String newTblName = genRandomTableName();
		String existTblName = genRandomTableName() + "_foo";
		assertFalse(tableExists(existTblName));
		try {
			DBUtils.createTableLike(newTblName, existTblName);
			fail();
		} catch (SQLException ex) {
			// expected case
		} finally {
			DBUtils.dropTable(newTblName);
		}
	}

	// ================================================= createTableLikeIfNotExists / dropTable

	public void testCreateTableLikeIfNotExistsAndDropTableWorkAsExpected() throws Exception {
		String tblName = genRandomTableName();
		assertFalse(tableExists(tblName));
		DBUtils.createTableLikeIfNotExists(tblName, TABLE_NAME);
		try {
			assertTrue(tableExists(tblName));
		} finally {
			DBUtils.dropTable(tblName);
			assertFalse(tableExists(tblName));
		}
	}

	public void testCreateTableLikeIfNotExistsIsANoopIfTheTableExists() throws Exception {
		String tblName = genRandomTableName();
		try {
			DBUtils.createTableLike(tblName, TABLE_NAME);
			DBUtils.runSql("INSERT INTO " + tblName + " VALUES (1), (2)");
			assertEquals(2, DBUtils.runSql("SELECT count(*) FROM " + tblName));

			DBUtils.createTableLikeIfNotExists(tblName, TABLE_NAME);
			assertTrue(tableExists(tblName));
			assertEquals(2, DBUtils.runSql("SELECT count(*) FROM " + tblName));
		} finally {
			DBUtils.dropTable(tblName);
			assertFalse(tableExists(tblName));
		}
	}

	// ================================================= createTableLikeAcrossDBs(String,String,String)

	public void testCreateTableLikeAcrossDBsSameTableNameThrowsIfSourceTableDoesNotExist() throws Exception {
		try {
			DBUtils.createTableLikeAcrossDBs("foo", DEFAULT, DEFAULT);
			fail();
		} catch (SQLException ex) {
			assertEquals("Table 'db_utils_test.foo@localhost' doesn't exist.", ex.getMessage());
		}
	}

	public void testCreateTableLikeAcrossDBsSameTableNameCreatesTableInTargetDBLikeTableInSourceDB() throws Exception {
		DBConnectionUtils wh = DBConnectionUtils.getInstance(DEFAULT);
		wh.setDbDatabaseName(null); // clear any existing name
		String whDbName = wh.getDbDatabaseName() + "_test";
		wh.setDbDatabaseName(whDbName);
		DBUtils.dropDB(DEFAULT, whDbName);
		DBUtils.createDB(DEFAULT, whDbName);
		DBUtils.createTableLikeAcrossDBs(TABLE_NAME, DEFAULT, DEFAULT);
		checkCreateTableLikeAcrossDBs(TABLE_NAME);
	}

	// ================================================= createTableLikeAcrossDBs(String,String,String,String)

	public void testCreateTableLikeAcrossDBsDiffTableNameThrowsIfSourceTableDoesNotExist() throws Exception {
		try {
			DBUtils.createTableLikeAcrossDBs("bar", DEFAULT, "foo", DEFAULT);
			fail();
		} catch (SQLException ex) {
			assertEquals("Table 'db_utils_test.foo@localhost' doesn't exist.", ex.getMessage());
		}
	}

	public void testCreateTableLikeAcrossDBsDiffTableNameCreatesTableInTargetDBLikeTableInSourceDB() throws Exception {
		DBConnectionUtils wh = DBConnectionUtils.getInstance(DEFAULT);
		wh.setDbDatabaseName(null); // clear any existing name
		String whDbName = wh.getDbDatabaseName() + "_test";
		wh.setDbDatabaseName(whDbName);
		DBUtils.dropDB(DEFAULT, whDbName);
		DBUtils.createDB(DEFAULT, whDbName);
		String newTableName = TABLE_NAME + "_new";
		DBUtils.createTableLikeAcrossDBs(newTableName, DEFAULT, TABLE_NAME, DEFAULT);
		checkCreateTableLikeAcrossDBs(newTableName);
	}

	private void checkCreateTableLikeAcrossDBs(String destTableName) throws SQLException {
		assertTrue(DBUtils.tableExists(DEFAULT, destTableName));
		String createTableCommand = (CREATE_TABLE_COMMAND + " ENGINE=MyISAM DEFAULT CHARSET=utf8")
			.replace(TABLE_NAME + " (", "`" + destTableName + "` (\n  ")
			.replace(") ENGINE", "\n) ENGINE")
			.replace("x INT", "`x` int(11)")
			.replace("KEY (x)", "KEY  (`x`)")
			.replace(", ", ",\n  ");
		assertStringsEqualsIgnoreWhiteSpace(
			createTableCommand, DBUtils.getCreateTableCommand(DEFAULT, destTableName));
	}

	// ================================================= renameTables

	public void testRenameTablesThrowsIfNumberOfTableNamesIsNotEven() throws Exception {
		checkRenameTablesThrows("a");
		checkRenameTablesThrows("a", "b", "c");
		checkRenameTablesThrows("a", "b", "c", "d", "e");
	}

	private void checkRenameTablesThrows(String... tblNames) throws SQLException {
		try {
			assertEquals(1, tblNames.length % 2);
			DBUtils.renameTables(tblNames);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testRenameTablesThrowsExceptionIfFromTableDoesNotExist() {
		String fromTblName = genRandomTableName();
		String toTblName = fromTblName + "_new";
		try {
			assertFalse(tableExists(fromTblName));
			DBUtils.renameTables(fromTblName, toTblName);
			fail();
		} catch (SQLException ex) {
			// expected case
		}
	}

	public void testRenameTablesThrowsExceptionIfToTableExists() throws Exception {
		String fromTblName = genRandomTableName();
		String toTblName = fromTblName + "_new";
		try {
			DBUtils.createTableLike(fromTblName, TABLE_NAME);
			DBUtils.createTableLike(toTblName, TABLE_NAME);
			assertTrue(tableExists(toTblName));
			DBUtils.renameTables(fromTblName, toTblName);
			fail();
		} catch (SQLException ex) {
			// expected case
		} finally {
			DBUtils.dropTable(fromTblName);
			DBUtils.dropTable(toTblName);
		}
	}

	public void testRenameTablesRenamesOneTableIntoAnother() throws Exception {
		String fromTblName = genRandomTableName();
		String toTblName = fromTblName + "_new";
		try {
			DBUtils.createTableLike(fromTblName, TABLE_NAME);
			assertTrue(tableExists(fromTblName));
			assertFalse(tableExists(toTblName));
			DBUtils.renameTables(fromTblName, toTblName);
			assertFalse(tableExists(fromTblName));
			assertTrue(tableExists(toTblName));
		} finally {
			DBUtils.dropTable(fromTblName);
			DBUtils.dropTable(toTblName);
		}
	}

	public void testRenameTablesWorksWithMultipleRenames() throws Exception {
		final int N = 5;
		String tblName = genRandomTableName();
		try {
			String fromTblName = tblName + "_0";
			DBUtils.createTableLike(fromTblName, TABLE_NAME);
			for (int i = 0; i < N; i++) {
				assertTrue(tableExists(fromTblName));
				String toTblName = tblName + "_" + (i+1);
				assertFalse(tableExists(toTblName));
				DBUtils.renameTables(fromTblName, toTblName);
				assertFalse(tableExists(fromTblName));
				assertTrue(tableExists(toTblName));
				fromTblName = toTblName;
			}
		} finally {
			for (int i = 0; i < N; i++) {
				DBUtils.dropTable(tblName + "_" + i);
			}
		}
	}

	// ================================================= escapeSqlStringLiteral

	public void testEscapeSqlStringLiteralReturnsCorrectResult() {
		assertEquals("foo/bar", DBUtils.escapeSqlStringLiteral("foo/bar"));
		assertEquals("foo\\\\bar", DBUtils.escapeSqlStringLiteral("foo\\bar"));
		assertEquals("C:\\\\foo\\\\bar", DBUtils.escapeSqlStringLiteral("C:\\foo\\bar"));
		assertEquals("foo''bar", DBUtils.escapeSqlStringLiteral("foo'bar"));
	}

	// ================================================= truncateTable

	public void testTruncateTableFailsIfTableDoesNotExist() throws Exception {
		try {
			DBUtils.truncateTable("non_existent_table");
			fail();
		} catch (AnchorFatalError ex) {
			// expected case
			assertEquals("Unable to truncate non_existent_table. Does such a table exist?", ex.getMessage());
		}
	}

	public void testTruncateTableDeletesAllRowsFromExistingTable() throws Exception {
		DBUtils.runSql("INSERT INTO " + TABLE_NAME + " VALUES (1),(2),(3)");
		assertEquals(3, DBUtils.runSql("SELECT count(*) FROM " + TABLE_NAME));
		DBUtils.truncateTable(TABLE_NAME);
		assertEquals(0, DBUtils.runSql("SELECT count(*) FROM " + TABLE_NAME));
	}

	// ================================================= truncateTableIfExists

	public void testTruncateTableIfExistsIsANoopIfTableDoesNotExist() throws Exception {
		DBUtils.truncateTableIfExists("non_existent_table");
	}

	public void testTruncateTableIfExistsDeletesAllRowsFromExistingTable() throws Exception {
		DBUtils.runSql("INSERT INTO " + TABLE_NAME + " VALUES (1),(2),(3)");
		assertEquals(3, DBUtils.runSql("SELECT count(*) FROM " + TABLE_NAME));
		DBUtils.truncateTableIfExists(TABLE_NAME);
		assertEquals(0, DBUtils.runSql("SELECT count(*) FROM " + TABLE_NAME));
	}

	// ================================================= listTables

	public void testListTablesReturnsAllTableNames() throws Exception {
		List<String> exp = new ArrayList<String>(Arrays.asList(TABLE_NAME));
		assertListEquals(exp, DBUtils.listTables());
		for (int i = 1; i <= 3; i++) {
			String tblName = TABLE_NAME + "_" + i;
			DBUtils.createTableLike(tblName, TABLE_NAME);
			exp.add(tblName);
			assertListEquals(exp, DBUtils.listTables());
		}
	}

	// ================================================= listTablesLike

	public void testListTablesLikeReturnsAllTableNamesMatchingPattern() throws Exception {
		final String pattern = TABLE_NAME + "__%";
		List<String> exp = new ArrayList<String>();
		assertListEquals(exp, DBUtils.listTablesLike(pattern));
		for (int i = 1; i <= 4; i++) {
			String tblName = TABLE_NAME;
			for (int j = 0; j < i; j++) tblName += "_";
			DBUtils.createTableLike(tblName, TABLE_NAME);
			if (i >= 2) exp.add(tblName);
			assertListEquals(exp, DBUtils.listTablesLike(pattern));
		}
	}

	// ================================================= databaseExists

	public void testDatabaseExistsReturnsCorrectValues() throws Exception {
		assertTrue(DBUtils.databaseExists(DB_NAME));
		DBUtils.dropDB(DB_NAME);
		assertFalse(DBUtils.databaseExists(DB_NAME));
	}

	// ================================================= getTableIndexes

	public void testGetTableIndexesReturnsEmptyMapIfTableHasNoIndexes() throws Exception {
		DBUtils.runSql("CREATE TABLE foo AS SELECT * FROM " + TABLE_NAME);
		assertTrue(DBUtils.getTableIndexes("foo").isEmpty());
	}

	public void testGetTableIndexesReturnsAllPrimaryAndNonPrimaryIndexesInTable() throws Exception {
		String sql =
			"CREATE TABLE foo (" +
			"w INT, x INT, y INT, z INT, " +
			"PRIMARY KEY (w, x), " +
			"INDEX (z, y, x), INDEX (z, y), INDEX(x, z))";
		DBUtils.runSql(sql);
		Map<String, List<String>> indexes = DBUtils.getTableIndexes("foo");
		assertEquals(4, indexes.size());
		assertListEquals(new String[] { "w", "x" }, indexes.get("PRIMARY"));
		assertListEquals(new String[] { "z", "y", "x" }, indexes.get("z"));
		assertListEquals(new String[] { "z", "y" }, indexes.get("z_2"));
		assertListEquals(new String[] { "x", "z" }, indexes.get("x"));
	}

	// ================================================= setPreparedStatementValues

	public void testSetPreparedStatementValuesIsANoOpIfValuesAreNull() throws Exception {
		invokeSetPreparedStatementValues((Object[]) null, new Closure());
	}

	public void testSetPreparedStatementValuesAcceptsStringType() throws Exception {
		 invokeSetPreparedStatementValues(new Object[] { "foobar" }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertEquals("foobar", rs.getString(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsBooleanType() throws Exception {
		 invokeSetPreparedStatementValues(new Object[] { Boolean.TRUE }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertTrue(rs.getBoolean(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsByteType() throws Exception {
		 invokeSetPreparedStatementValues(new Object[] { new Byte(Byte.MAX_VALUE) }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertEquals(Byte.MAX_VALUE, rs.getByte(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsShortType() throws Exception {
		 invokeSetPreparedStatementValues(new Object[] { new Short(Short.MAX_VALUE) }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertEquals(Short.MAX_VALUE, rs.getShort(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsIntType() throws Exception {
		 invokeSetPreparedStatementValues(new Object[] { Integer.MAX_VALUE }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertEquals(Integer.MAX_VALUE, rs.getInt(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsLongType() throws Exception {
		 invokeSetPreparedStatementValues(new Object[] { Long.MAX_VALUE }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertEquals(Long.MAX_VALUE, rs.getLong(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsFloatType() throws Exception {
		 invokeSetPreparedStatementValues(new Object[] { Float.MAX_VALUE }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertEquals(Float.MAX_VALUE, rs.getFloat(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsDoubleType() throws Exception {
		 invokeSetPreparedStatementValues(new Object[] { 1.2345678901234E123 }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertEquals(1.2345678901234E123, rs.getDouble(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsSqlDateType() throws Exception {
		final java.sql.Date now = new java.sql.Date(System.currentTimeMillis());
		invokeSetPreparedStatementValues(new Object[] { now }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				// hours, minutes, seconds, and milliseconds are not saved to the DB when using SQL Dates
				Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				cal1.setTime(now);
				Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				cal2.clear();
				cal2.set(cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DATE));
				Date gotDate = rs.getDate(1);
				assertInstanceOf(gotDate, java.util.Date.class);
				assertEquals(cal2.getTimeInMillis(), gotDate.getTime());
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsSqlTimestampType() throws Exception {
		final java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
		invokeSetPreparedStatementValues(new Object[] { now }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				long nowNoMillis = (now.getTime() / DateUtils.MILLIS_PER_SECOND) * DateUtils.MILLIS_PER_SECOND;
				assertEquals(new Date(nowNoMillis), rs.getDate(1));
			}
		 });
	}

	public void testSetPreparedStatementValuesAcceptsUtilDateType() throws Exception {
		final Date now = new Date();
		invokeSetPreparedStatementValues(new Object[] { now }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				// milliseconds are not saved to the DB when using SQL Timestamps
				long nowNoMillis = (now.getTime() / DateUtils.MILLIS_PER_SECOND) * DateUtils.MILLIS_PER_SECOND;
				assertEquals(new Date(nowNoMillis), rs.getDate(1));
			}
		 });
	}

	private static enum TestEnum {
		FOO, BAR, BAZ;
	}

	public void testSetPreparedStatementValuesAcceptsEnumType() throws Exception {
		invokeSetPreparedStatementValues(new Object[] { TestEnum.BAR }, new Closure() {
			@Override public void run(AnchorResultSet rs) throws Exception {
				assertEquals(TestEnum.BAR, TestEnum.valueOf(rs.getString(1)));
			}
		 });
	}

	public void testSetPreparedStatementValuesThrowsOnUnsupportedType() throws Exception {
		try {
			invokeSetPreparedStatementValues(new Object[] { this }, new Closure());
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals("cannot handle object of type " + getClass().getCanonicalName(), ex.getMessage());
		}
	}

	private void invokeSetPreparedStatementValues(Object[] args, Closure cl) throws Exception {
		Connection conn = DBConnectionUtils.getDefaultInstance().createConnection();
		try {
			String placeholders = ArrayUtils.isEmpty(args) ? "1" : "?";
			PreparedStatement ps = conn.prepareStatement("SELECT " + placeholders);
			try {
				DBUtils.setPreparedStatementValues(ps, args);
				assertTrue(ps.execute());
				AnchorResultSet rs = new AnchorResultSet(ps.getResultSet());
				try {
					assertTrue(rs.next());
					cl.run(rs);
				} finally {
					rs.close();
				}
			} finally {
				ps.close();
			}
		} finally {
			conn.close();
		}
	}

	private static class Closure {
		public void run(@SuppressWarnings("unused") AnchorResultSet rs) throws Exception { };
	}

	// ================================================= setPreparedStatementValues

	public void testGetIndexDirectoryReturnsNullWhenPropIsBlank() throws Exception {
		FWPropsTest.setProperty("dbutils.indexDirectory", "");
		assertNull(DBUtils.getIndexDirectory());
	}

	public void testGetIndexDirectoryReturnsCorrectValueWhenPropIsSet() throws Exception {
		FWPropsTest.setProperty("dbutils.indexDirectory", "/var/.idx");
		assertEquals("/var/.idx/" + DBConnectionUtils.getDefaultInstance().getDbDatabaseName(), DBUtils.getIndexDirectory());
	}

	// ================================================= listTables

	public void testListTablesReturnsCorrectValues() {
		assertListEquals(new String[] {TABLE_NAME}, DBUtils.listTables());
	}

	// ================================================= flushTables

	public void testFlushTablesDoesNotThrow() {
		DBUtils.flushTables(DBUtils.listTables());
		assertTrue(DBUtils.tableExists(TABLE_NAME));
	}

	// ================================================= selectIntoOutfile

	public void testSelectIntoOutfileProducesCommaDelimitedResults() throws Exception {
		checkSelectIntoOutfileProducesCorrectResults("SELECT 1 foo,2 bar", "foo,bar", "1,2");
	}

	public void testSelectIntoOutfileQuotesStringsWithSpecialCharacters() throws Exception {
		checkSelectIntoOutfileProducesCorrectResults("SELECT '\\t' foo, '\t' bar", "foo,bar", "\"\t\",\"\t\"");
		checkSelectIntoOutfileProducesCorrectResults("SELECT '\\t' foo, 1 bar", "foo,bar", "\"\t\",1");
		checkSelectIntoOutfileProducesCorrectResults("SELECT ',' foo, ',' bar", "foo,bar", "\",\",\",\"");
		checkSelectIntoOutfileProducesCorrectResults("SELECT '\"' foo, '\"baz\"' bar", "foo,bar", "\"\"\"\",\"\"\"baz\"\"\"");
	}

	public void testSelectIntoOutfileEscapesBackSlashes() throws Exception {
		checkSelectIntoOutfileProducesCorrectResults("SELECT '\\\\' foo, '\\\\' bar", "foo,bar", "\\\\,\\\\");
	}

	public void testSelectIntoOutfileEscapesNewlines() throws Exception {
		checkSelectIntoOutfileProducesCorrectResults("SELECT '\n' foo, '\\n' bar", "foo,bar", "\\n,\\n");
	}

	private void checkSelectIntoOutfileProducesCorrectResults(String sql, String ... lines) throws Exception {
		File outfile = new File(getCreatedOutputDir(), "outfile");
		DBUtils.selectIntoOutfile(sql, outfile);
		assertTextFilesEqual(createFileFromLines("expected", lines), outfile);
	}

	// ================================================= private helpers

	private boolean databaseExists(String dbName) {
		try {
			DBConnectionUtils db = DBConnectionUtils.getDefaultInstance();
			String url = DBConnectionUtils.getJdbcUrl(db.getDbHostName(), dbName);
			DriverManager.getConnection(url, db.getDbUserName(), db.getDbPassword());
			return true;
		} catch (SQLException ex) {
			return false;
		}
	}

	private String genRandomDBName() {
		int randInt = new Random().nextInt(10000);
		return "testdb" + randInt;
	}

	private boolean tableExists(String tableName) {
		try {
			DBUtils.runSql("SELECT count(*) FROM " + tableName);
			return true;
		} catch (SQLException ex) {
			return false;
		}
	}

	private String genRandomTableName() {
		int randInt = new Random().nextInt(10000);
		return "test_table_" + randInt;
	}
}
