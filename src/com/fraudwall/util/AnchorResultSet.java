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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Date;

import com.fraudwall.util.date.AnchorDateFormat;

/**
 * Provides a thin wrapper on top of a {@link ResultSet} that adds getDate(),
 * and hides getTimestamp(). This guarantees that all dates fetched from the DB
 * are always parsed in the UTC time zone.
 * <p>
 * This class does not actually implement the ResultSet interface because there
 * are hundreds of methods to implement and our code calls only a few of them.
 *
 * @author ryan
 */
public class AnchorResultSet {

	/**
	 * Thread-safe date formatter for reading and writing date-times to the DB
	 * which are ALWAYS in UTC.
	 */
	private static final ThreadSafeDateFormat DATE_TIME_FORMAT = new ThreadSafeDateFormat(DBUtils.DF_MYSQL);

	/**
	 * Thread-safe date formatter for reading and writing dates (with no
	 * timestamp) to the DB which are ALWAYS in UTC.
	 */
	private static final ThreadSafeDateFormat DATE_ONLY_FORMAT =
		new ThreadSafeDateFormat(new AnchorDateFormat("yyyy-MM-dd"));

	/**
	 * Possibly null statement that was used to generate this results set which will be
	 * closed by {@link #close()}.
	 */
	private final Statement stmt;

	/**
	 * Underlying {@link ResultSet} to which each getter delegates, and which will be
	 * closed by {@link #close()}.
	 */
	private final ResultSet rs;

	/**
	 * Executes the SQL query <code>query</code> using a new statement from <code>conn</code>.
	 * If the query produces no results (e.g. an UPDATE statement) or if an exception is
	 * generated, the statement will be closed.  Otherwise, a new AnchorResultSet
	 * will be returned such that when the {@link AnchorResultSet} is closed, both
	 * the {@link ResultSet} and {@link Statement} will be closed.
	 */
	public static AnchorResultSet create(Connection conn, String query) throws SQLException {
		return create(conn.createStatement(), query);
	}

	/**
	 * Executes the SQL query <code>query</code> using a <code>statement</code>.
	 * If the query produces no results (e.g. an UPDATE statement) or if an exception is
	 * generated, then <code>statement</code> will be closed.  Otherwise, a new AnchorResultSet
	 * will be returned such that when the {@link AnchorResultSet} is closed, both
	 * the {@link ResultSet} and {@link Statement} will be closed.
	 */
	public static AnchorResultSet create(PreparedStatement stmt) throws SQLException {
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery();
			return new AnchorResultSet(stmt, rs);
		} finally {
			// This is a ghetto mechanism for detecting that an exception has
			// been thrown.  In this case, we need to explicitly close
			// the statement
			if (rs == null) {
				stmt.close();
			}
		}
	}

	/**
	 * Executes the SQL query <code>query</code> using a new {@link PreparedStatement}
	 * generated from <code>connection</code>, and setting the placeholders in
	 * <code>sql</code> to <code>values</code>/
	 * If the query produces no results (e.g. an UPDATE statement) or if an exception is
	 * generated, then this statement will be closed.  Otherwise, a new AnchorResultSet
	 * will be returned such that when the {@link AnchorResultSet} is closed, both
	 * the {@link ResultSet} and {@link PreparedStatement} will be closed.
	 */
	public static AnchorResultSet create(Connection conn, String sql, Object ... values) throws SQLException {
		ResultSet rs = null;
		PreparedStatement stmt = conn.prepareStatement(sql);
		try {
			DBUtils.setPreparedStatementValues(stmt, values);
			rs = stmt.executeQuery();
			return new AnchorResultSet(stmt, rs);
		} finally {
			// This is a ghetto mechanism for detecting that an exception has
			// been thrown.  In this case, we need to explicitly close
			// the statement
			if (rs == null) {
				stmt.close();
			}
		}
	}

	/**
	 * Executes the SQL query <code>query</code> using a <code>statement</code>.
	 * If the query produces no results (e.g. an UPDATE statement) or if an exception is
	 * generated, then <code>statement</code> will be closed.  Otherwise, a new AnchorResultSet
	 * will be returned such that when the {@link AnchorResultSet} is closed, both
	 * the {@link ResultSet} and {@link Statement} will be closed.
	 */
	public static AnchorResultSet create(Statement stmt, String query) throws SQLException {
		Boolean hasResults = null;
		try {
			hasResults = stmt.execute(query);
			return hasResults ? new AnchorResultSet(stmt, stmt.getResultSet()) : null;
		} finally {
			// This is a ghetto mechanism for detecting that an exception has
			// been thrown.  In this case, we need to explicitly close
			// the statement
			if (hasResults == null) {
				stmt.close();
			}
		}
	}

	/**
	 * Creates a new AnchorResultSet that simply wraps the {@link ResultSet} <code>rs</code>.
	 */
	public AnchorResultSet(ResultSet rs) {
		this(/*stmt=*/null, rs);
	}

	private AnchorResultSet(Statement stmt, ResultSet rs) {
		this.stmt = stmt;
		this.rs = rs;
	}

	/**
	 * Returns the string value in the (1-based) column <code>i</code> of
	 * the current row. Returns <code>null</code> for the database value
	 * NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public String getString(int i) throws SQLException {
		return rs.getString(i);
	}

	/**
	 * Returns the string value in the column named <code>columnName</code> of
	 * the current row. Returns <code>null</code> for the database value
	 * NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public String getString(String columnName) throws SQLException {
		return rs.getString(columnName);
	}

	/**
	 * Returns the long value in the (1-based) column <code>i</code> of
	 * the current row. Returns 0L for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public long getLong(int i) throws SQLException {
		return rs.getLong(i);
	}

	/**
	 * Returns the long value in the column named <code>columnName</code> of
	 * the current row. Returns 0L for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public long getLong(String columnName) throws SQLException {
		return rs.getLong(columnName);
	}

	/**
	 * Returns the byte value in the (1-based) column <code>i</code> of
	 * the current row. Returns 0 for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public byte getByte(int i) throws SQLException {
		return rs.getByte(i);
	}

	/**
	 * Returns the short value in the (1-based) column <code>i</code> of
	 * the current row. Returns 0 for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public short getShort(int i) throws SQLException {
		return rs.getShort(i);
	}

	/**
	 * Returns the int value in the (1-based) column <code>i</code> of
	 * the current row. Returns 0 for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public int getInt(int i) throws SQLException {
		return rs.getInt(i);
	}

	/**
	 * Returns the int value in the column named <code>columnName</code> of
	 * the current row. Returns 0 for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public int getInt(String columnName) throws SQLException {
		return rs.getInt(columnName);
	}

	/**
	 * Returns the int value in the (1-based) column <code>i</code> of
	 * the current row. Returns 0 for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public BigDecimal getBigDecimal(int i) throws SQLException {
		return rs.getBigDecimal(i);
	}

	/**
	 * Returns the float value in the (1-based) column <code>i</code> of
	 * the current row. Returns 0.0F for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public float getFloat(int i) throws SQLException {
		return rs.getFloat(i);
	}

	/**
	 * Returns the double value in the column named <code>columnName</code> of
	 * the current row. Returns 0.0 for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public double getDouble(String columnName) throws SQLException {
		return rs.getDouble(columnName);
	}

	/**
	 * Returns the double value in the (1-based) column <code>i</code> of
	 * the current row. Returns 0.0 for the database value NULL.
	 * @exception SQLException if a database access error occurs.
	 */
	public double getDouble(int i) throws SQLException {
		return rs.getDouble(i);
	}

	/**
	 * Returns the boolean value in the (1-based) column <code>i</code> of
	 * the current row. Interprets the database value 1 as <em>true</em>, and the
	 * database values 0 and NULL as <em>false</em>.
	 * @exception SQLException if a database access error occurs.
	 */
	public boolean getBoolean(int i) throws SQLException {
		return rs.getBoolean(i);
	}

	/**
	 * Returns the Date value in the (1-based) column <code>i</code> of
	 * the current row. Returns <code>null</code> for the database value
	 * NULL. The date value is required to be a string of the form
	 * "yyyy-mm-dd HH:MM", and is parsed as a date/time in the UTC time
	 * zone.
	 * @exception SQLException if a database access error occurs.
	 */
	public Date getDate(int i) throws SQLException {
		String s = rs.getString(i);
		try {
			return s == null ? null : s.length() == 10 ? DATE_ONLY_FORMAT.parse(s) : DATE_TIME_FORMAT.parse(s);
		} catch (ParseException e) {
			throw new SQLException("Unable to parse date from mysql: '" + s + "'");
		} catch (NumberFormatException nfe) {
			throw new SQLException("Unable to parse date from mysql: '" + s + "'");
		}
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return rs.getMetaData();
	}

	public boolean wasNull() throws SQLException {
		return rs.wasNull();
	}

	/**
	 * Advances this result set to the next row. An <code>AnchorResultSet</code>
	 * cursor is initially positioned before the first row; the first call to
	 * the method <code>next</code> makes the first row the current row; the
	 * second call makes the second row the current row, and so on.
	 *
	 * @return <code>true</code> if the new current row is valid;
	 *         <code>false</code> if there are no more rows
	 * @exception SQLException if a database access error occurs.
	 */
	public boolean next() throws SQLException {
		return rs.next();
	}

	/**
	 * Closes the {@link ResultSet} and {@link Statement} underlying this object.
	 * @throws SQLException if a database access error occurs.
	 */
	public void close() throws SQLException {
		rs.close(); // explicity close rs since stmt might be null (and hence not closed)
		DBConnectionUtils.closeStatement(stmt);
	}

}

