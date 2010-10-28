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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.FWProps;
import com.fraudwall.util.Utilities;
import com.fraudwall.util.date.AnchorDateFormat;
import com.fraudwall.util.exc.AnchorFatalError;
import com.fraudwall.util.exc.ArgCheck;
import com.fraudwall.util.exc.Require;
import com.fraudwall.util.io.AnchorCsvWriter;

/**
 * MySQL database utility methods. Where one of these values is not
 * specified explicitly (e.g., as a method parameter), these methods
 * use the following property values to determine the host name,
 * database name, user name, and user password when connecting to
 * the database:
 * <ul>
 * <li>db.host</li>
 * <li>db.name</li>
 * <li>db.user</li>
 * <li>db.password</li>
 * </ul>
 *
 * @see DBConnectionUtils
 * @see ConnectionPool
 * @author Allan Heydon
 */
public abstract class DBUtils {
	private static final Log log = LogFactory.getLog(DBUtils.class);

	/**
	 * Date formatter for reading and writing dates to the DB
	 * which are ALWAYS in UTC.
	 */
	public static final DateFormat DF_MYSQL = new AnchorDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Drops the database with the given name if it exists. All
	 * data in this database will be lost, including all of its
	 * meta-data. The database is deleted from the MySQL database
	 * running on the machine identified by the <code>db.host</code>
	 * property value.
	 */
	public static void dropDB(String dbName) throws SQLException {
		dropDB(null, dbName);
	}

	/**
	 * Drops the database with the given name if it exists. All
	 * data in this database will be lost, including all of its
	 * meta-data. The database is deleted from the MySQL database
	 * running on the machine identified by the <code>db.host</code>
	 * property value for the specified application.
	 */
	public static void dropDB(String appName, String dbName) throws SQLException {
		runSqlAgainstBuiltInDatabase(appName, "DROP DATABASE IF EXISTS " + dbName);
	}

	/**
	 * Creates a new database with the given name. The database
	 * will not contain any tables. The database is created in the
	 * MySQL database running on the machine identified by the
	 * <code>db.host</code> property value.
	 */
	public static void createDB(String dbName) throws SQLException {
		createDB(null, dbName);
	}

	/**
	 * Creates a new database with the given name. The database
	 * will not contain any tables. The database is created in the
	 * MySQL database running on the machine identified by the
	 * <code>db.host</code> property value for the passed in application.
	 */
	public static void createDB(String appName, String dbName) throws SQLException {
		ArgCheck.isTrue(dbName.equals(dbName.toLowerCase()) || !Utilities.isCalledFromUnitTest(),
			"Database name contains upper-case charater(s): " + dbName);
		runSqlAgainstBuiltInDatabase(appName, "CREATE DATABASE " + dbName);
		makeIndexDirectory(appName);
	}

	private static void makeIndexDirectory(String appName) throws SQLException {
		String indexDirectory = getIndexDirectory(appName);
		if (indexDirectory != null) {
			makeDirectory(appName, indexDirectory);
		}
	}

	public static void makeDirectory(String appName, String dirName) throws SQLException {
		runSqlAgainstBuiltInDatabase(appName, "SELECT MAKE_DIRECTORY('" + dirName + "')");
	}

	public static void removeDirectory(String appName, String dirName) throws SQLException {
		runSqlAgainstBuiltInDatabase(appName, "SELECT REMOVE_DIRECTORY('" + dirName + "')");
	}

	/**
	 * Drops the table named <code>tblName</code> in the current
	 * database if it exists.
	 */
	public static void dropTable(String tblName) throws SQLException {
		dropTable(/*appName*/null, tblName);
	}

	/**
	 * Drops the table named <code>tblName</code> in the <code>appName</code>
	 * database if it exists.
	 */
	public static void dropTable(String appName, String tblName) throws SQLException {
		runSql(appName, "DROP TABLE IF EXISTS " + tblName);
	}

	/**
	 * Creates a new table named <code>newTblName</code> like the existing
	 * table named <code>existTblName</code> in the current database. Uses the
	 * default database of the current application.
	 *
	 * @throws SQLException
	 *             If there is an error creating the table, such as if the table
	 *             named <code>newTblName</code> already exists or if the
	 *             table named <code>existTblName</code> does not exist.
	 * @see #createTableLike(String, String, String)
	 * @see #createTableLikeIfNotExists(String, String)
	 * @see #createTableLikeIfNotExists(String, String, String)
	 * @see #createTableLikeWithoutIndexes(String, String)
	 * @see #createTableLikeWithoutIndexes(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String, String)
	 */
	public static void createTableLike(String newTblName, String existTblName) throws SQLException {
		createTableLike(null, newTblName, existTblName);
	}

	/**
	 * Creates a new table named <code>newTblName</code> like the existing
	 * table named <code>existTblName</code> in the database of the
	 * application named <code>appName</code>.
	 *
	 * @throws SQLException
	 *             If there is an error creating the table, such as if the table
	 *             named <code>newTblName</code> already exists or if the table
	 *             named <code>existTblName</code> does not exist.
	 * @see #createTableLike(String, String)
	 * @see #createTableLikeIfNotExists(String, String)
	 * @see #createTableLikeIfNotExists(String, String, String)
	 * @see #createTableLikeWithoutIndexes(String, String)
	 * @see #createTableLikeWithoutIndexes(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String, String)
	 */
	public static void createTableLike(String appName, String newTblName, String existTblName) throws SQLException {
		runSql(appName, "CREATE TABLE " + newTblName + " LIKE " + existTblName);
	}

	/**
	 * Creates a new table named <code>newTblName</code> like the existing
	 * table named <code>existTblName</code> in the current database. If a
	 * table named <code>newTblName</code> already exists, this method is a
	 * no-op. Uses the default database of the current application.
	 *
	 * @throws SQLException
	 *             If there is an error creating the table, such as if no table
	 *             named <code>existTblName</code> exists.
	 * @see #createTableLikeIfNotExists(String, String, String)
	 * @see #createTableLike(String, String)
	 * @see #createTableLikeWithoutIndexes(String, String)
	 * @see #createTableLikeWithoutIndexes(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String, String)
	 */
	public static void createTableLikeIfNotExists(String newTblName, String existTblName) throws SQLException {
		createTableLikeIfNotExists(null, newTblName, existTblName);
	}

	/**
	 * Creates a new table named <code>newTblName</code> like the existing
	 * table named <code>existTblName</code> in the database of the
	 * application named <code>appName</code>. If a table named
	 * <code>newTblName</code> already exists, this method is a no-op.
	 *
	 * @throws SQLException
	 *             If there is an error creating the table, such as if no table
	 *             named <code>existTblName</code> exists.
	 * @see #createTableLikeIfNotExists(String, String)
	 * @see #createTableLike(String, String, String)
	 * @see #createTableLikeWithoutIndexes(String, String)
	 * @see #createTableLikeWithoutIndexes(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String, String)
	 */
	public static void createTableLikeIfNotExists(String appName, String newTblName, String existTblName) throws SQLException {
		runSql(appName, "CREATE TABLE IF NOT EXISTS " + newTblName + " LIKE " + existTblName);
	}

	/**
	 * Creates a new table named <code>newTblName</code> with the same
	 * column names and types as <code>existTblName</code> but without
	 * any indexes (or primary key) in the default database.
	 *
	 * @throws SQLException
	 *             If there is an error creating the table, such as if the table
	 *             named <code>newTblName</code> already exists or if the table
	 *             named <code>existTblName</code> does not exist.
	 *
	 * @see #createTableLikeWithoutIndexes(String, String, String)
	 * @see #createTableLike(String, String)
	 * @see #createTableLikeIfNotExists(String, String)
	 * @see #createTableLikeIfNotExists(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String, String)
	 */
	public static void createTableLikeWithoutIndexes(String newTblName, String existTblName) throws SQLException {
		createTableLikeWithoutIndexes(/*appName=*/ null, newTblName, existTblName);
	}

	/**
	 * Creates a new table named <code>newTblName</code> with the same
	 * column names and types as <code>existTblName</code> but without
	 * any indexes (or primary key) in the database associated with
	 * <code>appName</code>.
	 *
	 * @throws SQLException
	 *             If there is an error creating the table, such as if the table
	 *             named <code>newTblName</code> already exists or if the table
	 *             named <code>existTblName</code> does not exist.
	 *
	 * @see #createTableLikeWithoutIndexes(String, String)
	 * @see #createTableLike(String, String, String)
	 * @see #createTableLikeIfNotExists(String, String)
	 * @see #createTableLikeIfNotExists(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String)
	 * @see #createTableLikeAcrossDBs(String, String, String, String)
	 */
	public static void createTableLikeWithoutIndexes(String appName, String newTblName, String existTblName)
		throws SQLException
	{
		DBUtils.runSql(appName, "CREATE TABLE " + newTblName + " AS SELECT * FROM " + existTblName + " LIMIT 0");
	}


	/**
	 * Creates a new table named <code>tblName</code> in the database of
	 * the application named <code>toAppName</code> like the existing table
	 * named <code>tblName</code> in the database of the application
	 * named <code>fromAppName</code>. Hence, this form of the method is
	 * used to create table in one database to be like a table with the
	 * same name in a possibly different database.
	 * <p>
	 * If a table named <code>tblName</code> already exists in
	 * <code>toAppName</code>, it is recreated to be like the existing table.
	 *
	 * @throws SQLException
	 *             If there is an error creating the table, such as if no table
	 *             named <code>tblName</code> exists in the database for the
	 *             application named <code>fromAppName</code>.
	 *
	 * @see #createTableLikeAcrossDBs(String, String, String, String)
	 * @see #createTableLike(String, String)
	 * @see #createTableLike(String, String, String)
	 * @see #createTableLikeIfNotExists(String, String)
	 * @see #createTableLikeIfNotExists(String, String, String)
	 * @see #createTableLikeWithoutIndexes(String, String)
	 * @see #createTableLikeWithoutIndexes(String, String, String)
	 */
	public static void createTableLikeAcrossDBs(
		final String tblName, final String toAppName, final String fromAppName)
		throws SQLException
	{
		createTableLikeAcrossDBs(tblName, toAppName, tblName, fromAppName);
	}

	/**
	 * Creates a new table named <code>tblName</code> in the database of
	 * the application named <code>toAppName</code> like the existing table
	 * named <code>tblName</code> in the database of the application
	 * named <code>fromAppName</code>. Hence, this form of the method is
	 * used to create table in one database to be like a table with the
	 * same name in a possibly different database.
	 * <p>
	 * If a table named <code>tblName</code> already exists in
	 * <code>toAppName</code>, it is recreated to be like the existing table.
	 *
	 * @throws SQLException
	 *             If there is an error creating the table, such as if no table
	 *             named <code>tblName</code> exists in the database for the
	 *             application named <code>fromAppName</code>.
	 *
	 * @see #createTableLikeAcrossDBs(String, String, String)
	 * @see #createTableLike(String, String)
	 * @see #createTableLike(String, String, String)
	 * @see #createTableLikeIfNotExists(String, String)
	 * @see #createTableLikeIfNotExists(String, String, String)
	 * @see #createTableLikeWithoutIndexes(String, String)
	 * @see #createTableLikeWithoutIndexes(String, String, String)
	 */
	public static void createTableLikeAcrossDBs(
		final String toTblName, final String toAppName,
		final String fromTblName, final String fromAppName)
		throws SQLException
	{
		log.info(String.format("Creating table %s like %s",
			getDatabaseTableHostName(toAppName, toTblName),
			getDatabaseTableHostName(fromAppName, fromTblName)));
		runSql(toAppName, getCreateTableCommand(fromAppName, fromTblName)
			.replace(createTableCommandPrefix(fromTblName), createTableCommandPrefix(toTblName)));
	}

	private static String createTableCommandPrefix(String tblName) {
		return "CREATE TABLE `" + tblName + "` (";
	}

	/**
	 * Returns the "CREATE TABLE" command for creating the (existing) table named
	 * <code>tblName</code> on the database for the application named <code>appName</code>.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 */
	public static String getCreateTableCommand(final String appName, final String tblName) throws SQLException {
		try {
			return runSql(appName, new SQLResultsClosure<String>("SHOW CREATE TABLE " + tblName) {
				@Override public String exec(AnchorResultSet rs) throws SQLException {
					return rs.next() ? rs.getString(2) : null;
				}
			});
		} catch (SQLException ex) {
			// fall through
		}
		String dbTableName = getDatabaseTableHostName(appName, tblName);
		throw new SQLException("Table '" + dbTableName + "' doesn't exist.");
	}

	private static String getDatabaseTableHostName(String appName, String tblName) {
		DBConnectionUtils db = DBConnectionUtils.getInstance(appName);
		return db.getDbDatabaseName() + "." + tblName + "@" + db.getDbHostName();
	}

	/**
	 * Atomically renames one or more database tables in this application's
	 * default database. For example, the call:
	 * <pre>
	 * renameTables("a", "b", "c", "a");
	 * </pre>
	 * will rename the table "a" to have name "b", and will then rename the
	 * table "c" to have the name "a". The renamings are performed in the order
	 * in which the tables are listed, so the example above will not produce an
	 * error when "c" is renamed because the table named "a" will no longer
	 * exist at that point.
	 *
	 * @param tblNames
	 *            An even number of table names. The specified table names
	 *            should be a sequence of pairs, where each pair represents a
	 *            "from" name and the "to" name to which that table should be
	 *            renamed.
	 *
	 * @throws SQLException
	 *             If an error occurs executing the rename statement, such as if
	 *             one of the "from" table names does not exist, or if one of
	 *             the "to" table names already exists.
	 * @see #renameTablesForApp(String, String...)
	 */
	public static void renameTables(String... tblNames) throws SQLException {
		renameTablesForApp(null, tblNames);
	}

	/**
	 * Atomically renames one or more database tables in the database associated
	 * with the given application. For example, the call:
	 * <pre>
	 * renameTables("a", "b", "c", "a");
	 * </pre>
	 * will rename the table "a" to have name "b", and will then rename the
	 * table "c" to have the name "a". The renamings are performed in the order
	 * in which the tables are listed, so the example above will not produce an
	 * error when "c" is renamed because the table named "a" will no longer
	 * exist at that point.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 * @param tblNames
	 *            An even number of table names. The specified table names
	 *            should be a sequence of pairs, where each pair represents a
	 *            "from" name and the "to" name to which that table should be
	 *            renamed.
	 *
	 * @throws SQLException
	 *             If an error occurs executing the rename statement, such as if
	 *             one of the "from" table names does not exist, or if one of
	 *             the "to" table names already exists.
	 * @see #renameTables(String...)
	 */
	public static void renameTablesForApp(String appName, String... tblNames) throws SQLException {
		ArgCheck.isTrue(tblNames.length % 2 == 0, "renameTables: number of argument tables is not even");
		StringBuilder sql = new StringBuilder(100);
		sql.append("RENAME TABLE ");
		for (int i = 0 ; i < tblNames.length; i += 2) {
			if (i > 0) {
				sql.append(", ");
			}
			sql.append(tblNames[i]).append(" TO ").append(tblNames[i+1]);
		}
		runSql(appName, sql.toString());
	}


	/**
	 * Executes <code>sql</code> in the default database
	 * and returns the value of the first column of the first row as a {@link Integer}
	 * or <code>null</code> if the value is SQL NULL or no rows are returned.
	 */
	public static Double fetchFirstDouble(String sql) throws SQLException {
		return fetchFirstDouble(null, sql);
	}

	/**
	 * Executes <code>sql</code> in the database associated with <code>appName</code>
	 * and returns the value of the first column of the first row as a {@link Double}
	 * or <code>null</code> if the value is SQL NULL or no rows are returned.
	 */
	public static Double fetchFirstDouble(String appName, String sql) throws SQLException {
		return runSql(appName, new SQLResultsClosure<Double>(sql) {
			@Override public Double exec(AnchorResultSet results) throws SQLException {
				return results.next() ? results.getDouble(1) : null;
			}
		});
	}

	/**
	 * Executes <code>sql</code> in the default database
	 * and returns the value of the first column of the first row as a {@link Integer}
	 * or <code>null</code> if the value is SQL NULL or no rows are returned.
	 */
	public static Integer fetchFirstInt(String sql) throws SQLException {
		return fetchFirstInt(null, sql);
	}

	/**
	 * Executes <code>sql</code> in the database associated with <code>appName</code>
	 * and returns the value of the first column of the first row as a {@link Integer}
	 * or <code>null</code> if the value is SQL NULL or no rows are returned.
	 */
	public static Integer fetchFirstInt(String appName, String sql) throws SQLException {
		return runSql(appName, new SQLResultsClosure<Integer>(sql) {
			@Override public Integer exec(AnchorResultSet results) throws SQLException {
				return results.next() ? results.getInt(1) : null;
			}
		});
	}

	/**
	 * Executes <code>sql</code> in the default database
	 * and returns the value of the first column of the first row as a {@link java.util.Date}
	 * or <code>null</code> if the value is SQL NULL or no rows are returned.
	 */
	public static java.util.Date fetchFirstDate(String sql) throws SQLException {
		return fetchFirstDate(null, sql);
	}

	/**
	 * Executes <code>sql</code> in the database associated with <code>appName</code>
	 * and returns the value of the first column of the first row as a {@link java.util.Date}
	 * or <code>null</code> if the value is SQL NULL or no rows are returned.
	 */
	public static java.util.Date fetchFirstDate(String appName, String sql) throws SQLException {
		return runSql(appName, new SQLResultsClosure<java.util.Date>(sql) {
			@Override public java.util.Date exec(AnchorResultSet results) throws SQLException {
				return results.next() ? results.getDate(1) : null;
			}
		});
	}

	/**
	 * Executes <code>sql</code> in the default database
	 * and returns the value of the first column of the first row as a {@link String}
	 * or <code>null</code> if the value is SQL NULL or no rows are returned.
	 */
	public static String fetchFirstString(String sql) throws SQLException {
		return fetchFirstString(null, sql);
	}

	/**
	 * Executes <code>sql</code> in the database associated with <code>appName</code>
	 * and returns the value of the first column of the first row as a {@link String}
	 * or <code>null</code> if the value is SQL NULL or no rows are returned.
	 */
	public static String fetchFirstString(String appName, String sql) throws SQLException {
		return runSql(appName, new SQLResultsClosure<String>(sql) {
			@Override public String exec(AnchorResultSet results) throws SQLException {
				return results.next() ? results.getString(1) : null;
			}
		});
	}

	private static void runSqlAgainstBuiltInDatabase(String appName, String sqlString) throws SQLException {
		Connection conn = DBConnectionUtils.getInstance(appName).createConnection(null);
		try {
			runSql(conn, /*largeResults=*/ false, new SQLIgnoreResultsClosure(sqlString));
		} finally {
			conn.close();
		}
	}

	/**
	 * Returns true iff <code>databaseName</code> exists on the database server
	 * for the default application.
	 */
	public static boolean databaseExists(final String databaseName) {
		return databaseExists(/*appName=*/ (String)null, databaseName);
	}

	/**
	 * Returns true iff <code>databaseName</code> exists in the database server
	 * on the host configured for <code>appName</code>.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 */
	public static boolean databaseExists(String appName, final String databaseName) {
		Connection conn = DBConnectionUtils.getInstance(appName).createConnection(null);
		try {
			try {
				return databaseExists(conn, databaseName);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new AnchorFatalError("Unable to determine if databse exists " + databaseName, e);
		}
	}

	/**
	 * Returns true iff <code>databaseName</code> exists on the database server
	 * for the passed in database connection.
	 */
	public static boolean databaseExists(Connection conn, final String databaseName) throws SQLException {
		return runSql(conn, /*largeResults=*/ false,
			new SQLResultsClosure<Boolean>("SHOW DATABASES LIKE '" + databaseName + "'") {
				@Override public Boolean exec(AnchorResultSet rs) throws SQLException {
					return rs.next();
				}
			});
	}

	/**
	 * Returns a map containing an entry for each index in the table named
	 * <code>tableName</code> in the default database.
	 *
	 * @return A map that maps the name of each of the table's indexes to the
	 *         list of columns in that index (in sequence order). The name of
	 *         the primary key index is always "PRIMARY". The result is
	 *         guaranteed to be non-<code>null</code>, but it may be empty.
	 */
	public static Map<String,List<String>> getTableIndexes(String tableName) throws SQLException {
		return getTableIndexes(/*appName=*/ null, tableName);
	}

	/**
	 * Returns a map containing an entry for each index in the table named
	 * <code>tableName</code> in database associated with the given application.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 * @return A map that maps the name of each of the table's indexes to the
	 *         list of columns in that index (in sequence order). The name of
	 *         the primary key index is always "PRIMARY". The result is
	 *         guaranteed to be non-<code>null</code>, but it may be empty.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,List<String>> getTableIndexes(String appName, String tableName) throws SQLException {
		return (Map<String,List<String>>) DBUtils.runSql(appName,
			new SQLResultsClosure("SHOW INDEXES FROM " + tableName) {
				@Override public Object exec(AnchorResultSet results) throws SQLException {
					Map<String,List<String>> res = new HashMap<String,List<String>>();
					while (results.next()) {
						String keyName = results.getString("Key_name");
						List<String> columns = res.get(keyName);
						if (columns == null) {
							columns = new ArrayList<String>();
							res.put(keyName, columns);
						}
						int sequence = results.getInt("Seq_in_index") - 1; // make 0-based
						String columnName = results.getString("Column_name");
						Require.isTrue(sequence == columns.size(), "Index columns not returned in sequence order");
						columns.add(columnName);
					}
					return res;
				}
			}
		);
	}

	/**
	 * Opens a new database connection and executes the SQL statement specified
	 * by <code>sqlString</code>. The supplied statement must either return an
	 * update count (i.e., it must be an UPDATE or DELETE statement), or it must
	 * return a result set consisting of a single row with a single integer-valued
	 * column (e.g., a "SELECT count(*)..." statement).
	 * <p>
	 * This method runs against the database of the default application (namely,
	 * the one passed to {@link FWProps#initialize(String, String)}.
	 *
	 * @param sqlString
	 *            The SQL statement to be executed via a call to
	 *            {@link Statement#execute(String)}.
	 * @return The update count or the integer value of the first column
	 *         of the first result row.
	 * @throws SQLException
	 *             If there is an error executing the SQL statement.
	 */
	public static long runSql(final String sqlString) throws SQLException {
		return runSql(/*appName=*/ null, sqlString);
	}

	/**
	 * Opens a new database connection and executes the SQL statement specified
	 * by <code>sqlString</code>. The supplied statement must either return an
	 * update count (i.e., it must be an UPDATE or DELETE statement), or it must
	 * return a result set consisting of a single row with a single integer-valued
	 * column (e.g., a "SELECT count(*)..." statement).
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 * @param sqlString
	 *            The SQL statement to be executed via a call to
	 *            {@link Statement#execute(String)}.
	 * @return The update count or the long value of the first column
	 *         of the first result row.
	 * @throws SQLException
	 *             If there is an error executing the SQL statement.
	 */
	public static long runSql(String appName, final String sqlString) throws SQLException {
		return runSql(appName, new SQLClosure<Long>() {
			@Override public Long exec(Statement stmt) throws SQLException {
				AnchorResultSet rs = logAndExecute(stmt, sqlString);
				if (rs != null) {
					return rs.next()? rs.getLong(1) : 0;
				} else {
					return (long) stmt.getUpdateCount();
				}
			}
		});
	}

	/**
	 * Opens a database {@link Connection}, creates a {@link Statement} on that
	 * connection, and runs the provided closure on that Statement. Takes care
	 * of closing the Statement and returning the Connection to the connection
	 * pool.
	 * <p>
	 * This method runs against the database of the default application (namely,
	 * the one passed to {@link FWProps#initialize(String, String)}.
	 *
	 * @param sqlExec
	 *            A closure whose {@link SQLClosure#exec exec} method is
	 *            invoked.
	 * @return The result returned by the execution of the
	 *         {@link SQLClosure#exec exec} method.
	 * @throws SQLException
	 *             If an exception occurs performing any operation against the
	 *             database.
	 */
	public static <T> T runSql(SQLClosure<T> sqlExec) throws SQLException {
		return runSql(/*largeResults=*/ false, sqlExec);
	}

	/**
	 * Opens a database {@link Connection}, creates a {@link Statement} on that
	 * connection, and runs the provided closure on that Statement. Takes care
	 * of closing the Statement and returning the Connection to the connection
	 * pool.
	 * <p>
	 * This method runs against the database of the default application (namely,
	 * the one passed to {@link FWProps#initialize(String, String)}.
	 * @param largeResults
	 *            If true, then a large result set statement will be created,
	 *            otherwise a standard statement will be used.
	 * @param sqlExec
	 *            A closure whose {@link SQLClosure#exec exec} method is
	 *            invoked.
	 *
	 * @return The result returned by the execution of the
	 *         {@link SQLClosure#exec exec} method.
	 * @throws SQLException
	 *             If an exception occurs performing any operation against the
	 *             database.
	 */
	public static <T> T runSql(boolean largeResults, SQLClosure<T> sqlExec) throws SQLException {
		return runSql(/*appName=*/ (String) null, largeResults, sqlExec);
	}

	/**
	 * Checks out a database {@link Connection}, creates a {@link Statement} on that
	 * connection, and runs the provided closure on that Statement. Takes care
	 * of closing the Statement and returning the Connection to the connection
	 * pool.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 * @param sqlExec
	 *            A closure whose {@link SQLClosure#exec exec} method is
	 *            invoked.
	 * @return The result returned by the execution of the
	 *         {@link SQLClosure#exec exec} method.
	 * @throws SQLException
	 *             If an exception occurs performing any operation against the
	 *             database.
	 */
	public static <T> T runSql(String appName, SQLClosure<T> sqlExec) throws SQLException {
		return runSql(appName, /*largeResults=*/ false, sqlExec);
	}

	/**
	 * Checks out a database {@link Connection}, creates a {@link Statement} on that
	 * connection, and runs the provided closure on that Statement. Takes care
	 * of closing the Statement and returning the Connection to the connection
	 * pool.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 * @param sqlExec
	 *            A closure whose {@link SQLClosure#exec exec} method is
	 *            invoked.
	 * @param largeResults
	 *            If true, then a large result set statement will be created,
	 *            otherwise a standard statement will be used.
	 * @return The result returned by the execution of the
	 *         {@link SQLClosure#exec exec} method.
	 * @throws SQLException
	 *             If an exception occurs performing any operation against the
	 *             database.
	 */
	public static <T> T runSql(String appName, boolean largeResults, SQLClosure<T> sqlExec) throws SQLException {
		Connection conn = ConnectionPool.getInstance(appName).checkout();
		try {
			return runSql(conn, largeResults, sqlExec);
		} finally {
			ConnectionPool.getInstance(appName).checkin(conn);
		}
	}

	/**
	 * Creates a {@link Statement} on the connection <code>conn</code>
	 * and runs the provided closure on that Statement. Takes care
	 * of closing the Statement.
	 *
	 * @param conn
	 *            Connection to use when executing the query.
	 * @param largeResults
	 *            If true, then a large result set statement will be created,
	 *            otherwise a standard statement will be used.
	 * @param sqlExec
	 *            A closure whose {@link SQLClosure#exec exec} method is
	 *            invoked.
	 * @return The result returned by the execution of the
	 *         {@link SQLClosure#exec exec} method.
	 * @throws SQLException
	 *             If an exception occurs performing any operation against the
	 *             database.
	 */
	public static <T> T runSql(Connection conn, boolean largeResults, SQLClosure<T> sqlExec) throws SQLException {
		Statement stmt = largeResults ? createLargeResultSetStatement(conn) : conn.createStatement();
		try {
			return sqlExec.exec(stmt);
		} finally {
			stmt.close();
		}
	}

	/**
	 * Executes the SQL statements in the given <code>sqlFile</code> against
	 * the default database.
	 *
	 * @throws FileNotFoundException
	 *             If there is an error reading lines from the given file.
	 * @throws SQLException
	 *             If there is an error running the SQL statements in the
	 *             supplied file against the supplied database connection.
	 */
	public static void runSqlFromFile(File sqlFile) throws FileNotFoundException, SQLException {
		runSqlFromFile(/*appName=*/ (String) null, sqlFile);
	}

	/**
	 * Executes the SQL statements in the given <code>sqlFile</code> against
	 * the database associated with the application named <code>appName</code>.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 * @throws FileNotFoundException
	 *             If there is an error reading lines from the given file.
	 * @throws SQLException
	 *             If there is an error running the SQL statements in the
	 *             supplied file against the supplied database connection.
	 */
	public static void runSqlFromFile(String appName, File sqlFile) throws FileNotFoundException, SQLException {
		Connection conn = ConnectionPool.getInstance(appName).checkout();
		try {
			runSqlFromFile(conn, sqlFile);
		} finally {
			ConnectionPool.getInstance(appName).checkin(conn);
		}
	}

	/**
	 * Executes the SQL statements in the given <code>sqlFile</code> against
	 * the given database connection <code>conn</code>.
	 *
	 * @throws FileNotFoundException
	 *             If there is an error reading lines from the given file.
	 * @throws SQLException
	 *             If there is an error running the SQL statements in the
	 *             supplied file against the supplied database connection.
	 */
	public static void runSqlFromFile(Connection conn, File sqlFile) throws FileNotFoundException, SQLException {
		Statement stmt = conn.createStatement();
		try {
			runSqlFromFile(stmt, sqlFile);
		} finally {
			stmt.close();
		}
	}

	private static void runSqlFromFile(Statement stmt, File sqlFile)
		throws FileNotFoundException, SQLException
	{
		if (log.isDebugEnabled()) {
			log.debug("Running SQL file: " + sqlFile.getPath());
		}
		SqlStatementIterator it = new SqlStatementIterator(new FileReader(sqlFile));
		try {
			while (it.hasNext()) {
				stmt.execute(it.next());
			}
		} finally {
			it.close();
		}
	}

	/**
	 * Returns the value of the LAST_INSERT_ID() function on the connection
	 * <code>conn</code>. This will be the value of the auto-increment field
	 * of the object that was most recently saved on the given connection.
	 *
	 * @return The last inserted ID value.
	 * @throws SQLException
	 *             if an error occurs connecting to the database, or if the
	 *             selection of the last inserted ID returns 0 rows.
	 */
	public static long getLastInsertId(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		try {
			AnchorResultSet rs = AnchorResultSet.create(st, "SELECT LAST_INSERT_ID()");
			if (rs != null && rs.next()) {
				return rs.getLong(1);
			}
			throw new SQLException("SELECT LAST_INSERT_ID() did not return results or results contained 0 rows");
		} finally {
			st.close();
		}
	}

	/**
	 * Returns a version of <code>s</code> with all back-slash ('\\')
	 * characters escaped so that the result can be used in a SQL string
	 * literal (between single-quote characters) in a SQL statement. This
	 * just precedes each back-slash by another back-slash, thereby escaping
	 * it.
	 */
	public static String escapeSqlStringLiteral(String s) {
		return s.replace("\\", "\\\\").replace("'", "''").replace("\n", "\\n").replace("\r", "\\r");
	}

	/**
	 * Returns a new statement on the specified connection with the necessary options
	 * to avoid running out of memory when reading a large result set.
	 */
	public static Statement createLargeResultSetStatement(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(Integer.MIN_VALUE);
		return stmt;
	}

	/**
	 * Returns a new prepared statement on the specified connection with the necessary options
	 * to avoid running out of memory when reading a large result set.
	 */
	public static PreparedStatement createLargeResultSetPreparedStatement(Connection conn, String sql) throws SQLException {
		PreparedStatement st = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(Integer.MIN_VALUE);
		return st;
	}

	/**
	 * Deletes all rows from the database table named <code>tableName</code> if it exists.
	 *
	 * @throws AnchorFatalError if the named table does not exist.
	 * @see #truncateTable(String, String)
	 * @see #truncateTableIfExists(String)
	 */
	public static void truncateTable(String tableName) {
		truncateTable(null, tableName);
	}

	/**
	 * Deletes all rows from the database table named <code>tableName</code> if it exists.
	 *
	 * @param appName The name of the application whose database contains the table to be truncated.
	 *
	 * @throws AnchorFatalError if the named table does not exist.
	 *
	 * @see #truncateTable(String)
	 * @see #truncateTableIfExists(String)
	 */
	public static void truncateTable(String appName, String tableName) {
		try {
			runSql(appName, "TRUNCATE TABLE " + tableName);
		} catch (SQLException e) {
			throw new AnchorFatalError("Unable to truncate " + tableName + ". Does such a table exist?", e);
		}
	}

	/**
	 * Deletes all rows from the database table named <code>tableName</code>. If the
	 * table does not exist, this method is a no-op.
	 *
	 * @see #truncateTableIfExists(String, String)
	 * @see #truncateTable(String)
	 */
	public static void truncateTableIfExists(String tableName) {
		truncateTableIfExists(null, tableName);
	}

	/**
	 * Deletes all rows from the database table named <code>tableName</code>. If the
	 * table does not exist, this method is a no-op.
	 *
	 * @param appName The name of the application whose database contains the table to be truncated.
	 *
	 * @see #truncateTableIfExists(String)
	 * @see #truncateTable(String, String)
	 */
	public static void truncateTableIfExists(String appName, String tableName) {
		if (tableExists(appName, tableName)) {
			truncateTable(appName, tableName);
		}
	}

	/**
	 * Returns a list of the names of each table in the default
	 * database.
	 *
	 * @see #listTablesLike(String)
	 * @see #listTables(String)
	 */
	public static List<String> listTables() {
		return listTables(null);
	}

	/**
	 * Returns a list of the names of all tables in the database
	 * associated with the application <code>appName</code>.
	 *
	 * @see #listTablesLike(String, String)
	 * @see #listTables()
	 */
	public static List<String> listTables(String appName) {
		return listTablesLike(appName, null);
	}

	/**
	 * Returns a list of the names of all tables in the default database LIKE
	 * the given <code>pattern</code>.
	 *
	 * @param pattern
	 *            A SQL string pattern that may contain "%" wildcard characters.
	 *            If <code>null</code>, the names of all tables in the
	 *            database are returned.
	 *
	 * @see #listTables()
	 * @see #listTablesLike(String, String)
	 */
	public static List<String> listTablesLike(String pattern) {
		return listTablesLike(null, pattern);
	}

	/**
	 * Returns a list of the names of all tables in the database associated with
	 * the application <code>appName</code> LIKE the given
	 * <code>pattern</code>.
	 *
	 * @param pattern
	 *            A SQL string pattern that may contain "%" wildcard characters.
	 *            If <code>null</code>, the names of all tables in the
	 *            database are returned.
	 *
	 * @see #listTables(String)
	 * @see #listTablesLike(String)
	 */
	public static List<String> listTablesLike(String appName, String pattern) {
		final List<String> tables = new ArrayList<String>();
		try {
			String sql = "SHOW TABLES" + (pattern == null ? "" : (" LIKE '" + pattern + "'"));
			runSql(appName, new SQLResultsProcessorClosure(sql) {
				@Override protected void processResult(AnchorResultSet results, int count) throws SQLException {
					tables.add(results.getString(1));
				}
			});
			return tables;
		} catch (SQLException e) {
			throw new AnchorFatalError("Unable to list tables", e);
		}
	}

	/**
	 * Flushes each table in <code>tables</code> in the default database.
	 *
	 * @see #flushAllTables()
	 * @see #flushTables(String, List)
	 */
	public static void flushTables(List<String> tables) {
		flushTables(null, tables);
	}

	/**
	 * Flushes each table in <code>tables</code> in the
	 * database associated with <code>appName</code>.
	 *
	 * @see #flushAllTables(String)
	 * @see #flushTables(List)
	 */
	public static void flushTables(String appName, List<String> tables) {
		try {
			runSql(appName, "FLUSH TABLES " + StringUtils.join(tables, ", "));
		} catch (SQLException e) {
			throw new AnchorFatalError("Unable to flush tables", e);
		}
	}

	/**
	 * Flushes <em>all</em> tables in the default database.
	 *
	 * @see #flushTables(List)
	 * @see #flushAllTables(String)
	 */
	public static void flushAllTables() {
		flushAllTables((String) null);
	}

	/**
	 * Flushes <em>all</em> tables in the database associated with <code>appName</code>.
	 *
	 * @see #flushTables(String, List)
	 * @see #flushAllTables()
	 */
	public static void flushAllTables(String appName) {
		flushTables(appName, listTables(appName));
	}

	/**
	 * Returns true iff the specified table exists in the default database.
	 */
	public static boolean tableExists(String tableName) {
		return tableExists(/*appName=*/ null, tableName);
	}

	/**
	 * Returns true iff the specified table exists in the database for
	 * the application named <code>appName</code>, or the default database
	 * if <code>appName</code> is null.
	 *
	 * @param appName
	 *            The name of the application, e.g. "tigerprawn". If
	 *            <code>null</code>, the database of the default application
	 *            is used (namely, the one passed to
	 *            {@link FWProps#initialize(String, String)}).
	 */
	public static boolean tableExists(String appName, String tableName) {
		try {
			return DBUtils.runSql(appName, new SQLResultsClosure<Boolean>("SHOW TABLES LIKE '" + tableName + "'") {
				@Override public Boolean exec(AnchorResultSet results) throws SQLException {
					return results.next();
				}

			});
		} catch (SQLException e) {
			throw new AnchorFatalError("Unable to show tables like " + tableName, e);
		}
	}

	private static final String INDEX_DIRECTORY_PROP_NAME = "dbutils.indexDirectory";

	public static String getIndexDirectory() {
		return getIndexDirectory(null);
	}

	public static String getIndexDirectory(String appName) {
		String indexDir = FWProps.getStringProperty(INDEX_DIRECTORY_PROP_NAME);
		if (StringUtils.isBlank(indexDir)) {
			return null;
		}
		indexDir += "/" + DBConnectionUtils.getInstance(appName).getDbDatabaseName();
		return indexDir;
	}

	public static PreparedStatement prepareStatement(Connection conn, String sql, Object ... values) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql);
		setPreparedStatementValues(stmt, values);
		return stmt;
	}

	/**
	 * Sets the given <code>values</code> on the prepared statement <code>st</code>. The values
	 * are set on the statement starting at index 1 (the first index).
	 *
	 * @param values The values to set on the statement. May be <code>null</code> or empty.
	 * @throws SQLException ff there is an error setting any prepared statement value.
	 * @throws IllegalArgumentException if one of the objects in <code>values</code> is not
	 * a recognized value type. The recognized types are instances of {@link String},
	 * {@link Boolean}, {@link Byte}, {@link Short}, {@link Integer}, {@link Long},
	 * {@link Float}, {@link Double}, {@link java.sql.Date}, {@link java.util.Date},
	 * {@link java.sql.Timestamp}, and {@link Enum}.
	 */
	@SuppressWarnings("unchecked")
	public static void setPreparedStatementValues(PreparedStatement st, Object... values)
		throws SQLException
	{
		if (values != null) {
			int index = 1;
			for (Object value : values) {
				if (value instanceof String) {
					st.setString(index++, (String)value);
				} else if (value instanceof Boolean) {
					st.setBoolean(index++, (Boolean)value);
				} else if (value instanceof Byte) {
					st.setByte(index++, (Byte)value);
				} else if (value instanceof Short) {
					st.setShort(index++, (Short)value);
				} else if (value instanceof Integer) {
					st.setInt(index++, (Integer)value);
				} else if (value instanceof Long) {
					st.setLong(index++, (Long)value);
				} else if (value instanceof Float) {
					st.setFloat(index++, (Float) value);
				} else if (value instanceof Double) {
					st.setDouble(index++, (Double) value);
				} else if (value instanceof Date) {
					st.setDate(index++, (Date)value);
				} else if (value instanceof Timestamp) {
					st.setTimestamp(index++, (Timestamp)value);
				} else if (value instanceof java.util.Date) {
					st.setTimestamp(index++, new Timestamp(((java.util.Date) value).getTime()));
				} else if (value instanceof Enum) {
					st.setString(index++, value.toString());
				} else {
					throw new IllegalArgumentException(
						"cannot handle object of type " + value.getClass().getName());
				}
			}
		}
	}

	/**
	 * Writes the SQL to the log object at info-level.
	 */
	private static void logSql(String sql) {
		for (String line: sql.split("\n")) {
			if (line.startsWith("LOAD DATA CONCURRENT LOCAL INFILE")) {
				int idx = line.indexOf(" FIELDS OPTIONALLY ENCLOSED BY");
				if (idx > 0) {
					line = line.substring(0, idx) + " ...";
				}
			}
			log.info(line);
		}
	}

	public static AnchorResultSet logAndExecute(Statement st, String sql) throws SQLException {
		logSql(sql);
		return AnchorResultSet.create(st, sql);
	}

	/**
	 * Executes <code>sql</code> in the default database and writes the
	 * results into <code>outFile</code> with the following
	 * characteristics.
	 * <p>
	 * <ul>
	 * <li>Fields will be terminated by comma
	 * <li>Fields will be optionally enclosed by double-quotes
	 * <li>Fields will be escaped by backslash
	 * </ul>
	 *
	 * @return the number of rows exported
	 * @see #selectIntoOutfile(String, String, File)
	 * @see #selectIntoOutfile(String, String, File, char)
	 * @see #loadDataLocalInfile(String, String, File)
	 */
	public static int selectIntoOutfile(String sql, File outFile) throws SQLException {
		return selectIntoOutfile(null, sql, outFile);
	}

	/**
	 * Executes <code>sql</code> in the database associated with
	 * <code>appName</code> and writes the results into
	 * <code>outFile</code> with the following characteristics.
	 * <p>
	 * <ul>
	 * <li>Fields will be terminated by comma
	 * <li>Fields will be optionally enclosed by double-quotes
	 * <li>Fields will be escaped by backslash
	 *</ul>
	 *
	 * @return the number of rows exported
	 * @see #selectIntoOutfile(String, File)
	 * @see #selectIntoOutfile(String, String, File, char)
	 * @see #loadDataLocalInfile(String, String, File)
	 */
	public static int selectIntoOutfile(String appName, String sql, File outFile) throws SQLException {
		return selectIntoOutfile(appName, sql, outFile, ',');
	}

	/**
	 * Executes <code>sql</code> in the database associated with
	 * <code>appName</code> and writes the results into
	 * <code>outFile</code> with the following characteristics.
	 * <p>
	 * <ul>
	 * <li>Fields will be terminated by <code>delimeter</code>
	 * <li>Fields will be optionally enclosed by double-quotes
	 * <li>Fields will be escaped by backslash
	 *</ul>
	 *
	 * @return the number of rows exported
	 * @see #selectIntoOutfile(String, File)
	 * @see #selectIntoOutfile(String, String, File)
	 * @see #loadDataLocalInfile(String, String, File, char)
	 */
	public static int selectIntoOutfile(String appName, String sql, File outFile, char delimeter) throws SQLException {
		log.info("Selecting results into outfile " + outFile);
		final AnchorCsvWriter writer = new AnchorCsvWriter(outFile, delimeter);
		return runSql(appName, /*largeResults=*/ true, new SQLResultsClosure<Integer>(sql) {
			@Override public Integer exec(AnchorResultSet results) throws SQLException {
				try {
					String[] columns = getColumnNames(results);
					for (int i=0; i < columns.length; i++) {
						writer.write(columns[i]);
					}
					writer.endRecord();

					int successCount = 0;
					while (results.next()) {
						for (int i=0; i < columns.length; i++) {
							String val = results.getString(i + 1);
							writer.write(val == null ? "NULL" : escapeOutfileString(val));
						}
						writer.endRecord();
						successCount++;
					}
					writer.close();
					return successCount;
				} catch (IOException e) {
					throw new AnchorFatalError("unable to write data", e);
				}
			}
		});
	}

	public static String[] getColumnNames(AnchorResultSet rs) {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			String[] columnNames = new String[metaData.getColumnCount()];
			for (int i=0; i < columnNames.length; i++) {
				columnNames[i] = metaData.getColumnName(i + 1);
			}
			return columnNames;
		} catch (SQLException e) {
			throw new AnchorFatalError("Unable to get result set meta data", e);
		}
	}

	/**
	 * Loads the data from the file <code>inFile</code> into the table named <code>tableName</code>
	 * in the database associated with <code>appName</code>. The fields of the output file are delimited
	 * by commas.
	 *
	 * @see #loadDataLocalInfile(String, String, File, char)
	 * @see #selectIntoOutfile(String, File)
	 * @see #selectIntoOutfile(String, String, File)
	 */
	public static void loadDataLocalInfile(String appName, String tableName, File inFile) throws SQLException {
		loadDataLocalInfile(appName, tableName, inFile, ',');
	}

	/**
	 * Loads the data from the file <code>inFile</code> into the table named <code>tableName</code>
	 * in the database associated with <code>appName</code>. The fields of the output file are delimited
	 * by the given <code>delimeter</code> character.
	 *
	 * @see #loadDataLocalInfile(String, String, File)
	 * @see #selectIntoOutfile(String, String, File, char)
	 */
	public static void loadDataLocalInfile(String appName, String tableName, File inFile, char delimeter) throws SQLException {
		DBUtils.runSql(appName, String.format(
			"LOAD DATA LOCAL INFILE '%s' INTO TABLE %s " +
			"FIELDS TERMINATED BY '%c' OPTIONALLY ENCLOSED BY '\"' IGNORE 1 LINES",
			inFile, tableName, delimeter)
		);
	}

	/**
	 * Returns a version of <code>s</code> with all back-slash ('\\')
	 * characters escaped so that the result can be used in a SQL string
	 * literal (between single-quote characters) in a SQL statement. This
	 * just precedes each back-slash by another back-slash, thereby escaping
	 * it.
	 */
	private static String escapeOutfileString(String s) {
		int numMatches = countSpecialCharacters(s);
		if (numMatches == 0) {
			return s;
		}
		StringBuilder sb = new StringBuilder(s.length() + numMatches);
		for (int i =0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				sb.append("\\\\");
			} else if (c == '\n') {
				sb.append("\\n");
			} else if (c == '\r') {
				sb.append("\\r");
			} else {
				sb.append(c);
			}
		}
//		return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
		return sb.toString();
	}

	private static int countSpecialCharacters(String s) {
		int numMatches = 0;
		for (int i =0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' || c == '\n' || c == '\r') {
				numMatches++;
			}
		}
		return numMatches;
	}

	/**
	 * Returns the "mysql" command for connecting to the database associated
	 * with the application named <code>appName</code>. mysql is invoked with
	 * "-v -v -v" to produce full debugging output and "-A" for fast start-up.
	 */
	public static String getMysqlCommandPrefix(String appName) {
		DBConnectionUtils db = DBConnectionUtils.getInstance(appName);
		return String.format("mysql -v -v -v -A -h %s -u %s --password=%s %s",
			db.getDbHostName(), db.getDbUserName(), db.getDbPassword(), db.getDbDatabaseName());
	}

	/**
	 * Helper method for creating the MySQL command-line to run the file named
	 * <code>sqlFileName</code>. When not invoked by this class or sub-classes,
	 * this method is intended only for test purposes.
	 */
	public static String getMysqlCommand(String appName, String sqlFileName) {
		String parent = new File(sqlFileName).getParent();
		String optionalCdCmd = (parent != null ? "cd " + parent + " && " : "");
		return optionalCdCmd + getMysqlCommandPrefix(appName) + " < " + sqlFileName;
	}
}
