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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fraudwall.util.StringUtils;
import com.fraudwall.util.exc.ArgCheck;

/**
 * Mock implementation of the {@link ResultSet} interface for testing purposes.
 * Since it is intended only for testing, this is by no means a complete implementation.
 * Many of its methods throw {@link UnsupportedOperationException}. If you need to
 * use one of those methods from a unit test, feel free to implement it.
 * <p>
 * By design, this class provides read-only access to a set of results supplied
 * to it in String form. Hence, none of the methods that mutate the database will
 * ever be supported.
 *
 * @author Allan Heydon
 */
public class ResultSetMock implements ResultSet {

	// ------------------- result set state

	/** Index of the current row being read (0-based). */
	private int currRow = -1;

	/** Columns of the result set (may be null). */
	private Map<String,Integer> columnNames;

	/** Rows of the result set. */
	private List<String[]> rowData = new ArrayList<String[]>();

	/**
	 * Indicates whether the last column value that was read was null.
	 *
	 * @see #wasNull()
	 */
	private boolean lastColumnReadWasNull = false;

	// ------------------- mock object state

	/** Mock state for recording method calls. */
	private boolean closed;

	// ------------------- data initialization methods

	public ResultSetMock() {
		columnNames = null;
		closed = false;
	}

	public ResultSetMock(String[] columnNames) {
		this();
		if (columnNames != null) {
			this.columnNames = new HashMap<String,Integer>(columnNames.length);
			for (int i = 0; i < columnNames.length; i++) {
				this.columnNames.put(columnNames[i], i);
			}
		}
	}

	public ResultSetMock(String[] columnNames, String[] singleRow) {
		this(columnNames);
		addRowData(singleRow);
	}

	public void addRowData(String[] row) {
		ArgCheck.isNotNull(row, "row");
		if (columnNames != null && columnNames.size() != row.length) {
			ArgCheck.equals(columnNames.size(), row.length, "incorrect number of values in row");
		}
		rowData.add(row);
	}

	// ---------------------------- cursor/row positioning methods

	public int getRow()  {
		return currRow + 1;
	}

	public boolean next()  {
		currRow++;
		return !isAfterLast();
	}

	public boolean previous()  {
		currRow--;
		return !isBeforeFirst();
	}

	public boolean absolute(int row) {
		if (row < 0) {
			// index from end of row
			currRow = Math.max(-1, rowData.size() + row);
		} else {
			currRow = Math.min(rowData.size(), row - 1);
		}
		return !(isBeforeFirst() || isAfterLast());
	}

	public boolean relative(int rows)  {
		currRow = Math.max(-1, Math.min(rowData.size(), currRow + rows));
		return !(isBeforeFirst() || isAfterLast());
	}

	public void beforeFirst()  {
		currRow = -1;
	}

	public boolean isBeforeFirst()  {
		return currRow < 0;
	}

	public boolean first()  {
		currRow = 0;
		return rowData.size() > 0;
	}

	public boolean isFirst()  {
		return currRow == 0;
	}

	public void afterLast() {
		currRow = rowData.size();
	}

	public boolean isAfterLast()  {
		return currRow >= rowData.size();
	}

	public boolean last()  {
		currRow = rowData.size() - 1;
		return rowData.size() > 0;
	}

	public boolean isLast()  {
		return currRow == rowData.size() - 1;
	}

	// ---------------------------- column lookup by index

	private String getColumnValue(int columnIndex) throws SQLException {
		if (isBeforeFirst()) {
			throw new SQLException("Cursor positioned before first row");
		}
		if (isAfterLast()) {
			throw new SQLException("Cursor positioned after last row");
		}
		String[] row = rowData.get(currRow);
		int arrayIndex = columnIndex - 1; // columnIndex is 1-based
		if (arrayIndex < 0 || arrayIndex >= row.length) {
			throw new SQLException(
				"Column index " + columnIndex + " is out of bounds; " +
				"table contains only " + row.length + " + columns");
		}
		String res = row[arrayIndex];
		lastColumnReadWasNull = (res == null);
		return res;
	}

	public boolean wasNull()  {
		return lastColumnReadWasNull;
	}

	public String getString(int columnIndex) throws SQLException {
		return getColumnValue(columnIndex);
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		String columnVal = getColumnValue(columnIndex);
		return columnVal == null ? false : StringUtils.parseBoolean(columnVal);
	}

	public short getShort(int columnIndex) throws SQLException {
		String columnVal = getColumnValue(columnIndex);
		return columnVal == null ? 0 : Short.parseShort(columnVal);
	}

	public int getInt(int columnIndex) throws SQLException {
		String columnVal = getColumnValue(columnIndex);
		return columnVal == null ? 0 : Integer.parseInt(columnVal);
	}

	public long getLong(int columnIndex) throws SQLException {
		String columnVal = getColumnValue(columnIndex);
		return columnVal == null ? 0L : Long.parseLong(columnVal);
	}

	public float getFloat(int columnIndex) throws SQLException {
		String columnVal = getColumnValue(columnIndex);
		return columnVal == null ? 0.0F : Float.parseFloat(columnVal);
	}

	public double getDouble(int columnIndex) throws SQLException {
		String columnVal = getColumnValue(columnIndex);
		return columnVal == null ? 0.0 : Double.parseDouble(columnVal);
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		String columnVal = getColumnValue(columnIndex);
		return columnVal == null ? null : new BigDecimal(columnVal);
	}

	@Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		BigDecimal res = getBigDecimal(columnIndex);
		if (res != null) {
			res = res.setScale(scale);
		}
		return res;
	}

	// ---------------------------- column lookup by name

	public int findColumn(String columnName) throws SQLException {
		if (columnNames == null) {
			throw new IllegalStateException("No column names specified for this ResultSetMock");
		}
		Integer res = columnNames.get(columnName);
		if (res == null) {
			throw new SQLException("Unknown column name: \"" + columnName + "\"");
		}
		return res;
	}

	public String getString(String columnName) throws SQLException {
		return getString(findColumn(columnName));
	}

	public boolean getBoolean(String columnName) throws SQLException {
		return getBoolean(findColumn(columnName));
	}

	public short getShort(String columnName) throws SQLException {
		return getShort(findColumn(columnName));
	}

	public int getInt(String columnName) throws SQLException {
		return getInt(findColumn(columnName));
	}

	public long getLong(String columnName) throws SQLException {
		return getLong(findColumn(columnName));
	}

	public float getFloat(String columnName) throws SQLException {
		return getFloat(findColumn(columnName));
	}

	public double getDouble(String columnName) throws SQLException {
		return getDouble(findColumn(columnName));
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException  {
		return getBigDecimal(findColumn(columnName));
	}

	@Deprecated
	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		return getBigDecimal(findColumn(columnName), scale);
	}

	// ---------------------------- close methods

	public void close()  {
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}

	// ----------------------------- no-op methods

	public void setFetchDirection(int direction)  {
		// no-op
	}

	public void setFetchSize(int rows)  {
		// no-op
	}

	public void refreshRow()  {
		// no-op
	}

	// ----------------------------- unsupported methods

	public void cancelRowUpdates()  {
		throw new UnsupportedOperationException();
	}

	public void clearWarnings()  {
		throw new UnsupportedOperationException();
	}

	public void deleteRow()  {
		throw new UnsupportedOperationException();
	}

	public Array getArray(int i)  {
		throw new UnsupportedOperationException();
	}

	public Array getArray(String colName)  {
		throw new UnsupportedOperationException();
	}

	public InputStream getAsciiStream(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public InputStream getAsciiStream(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public InputStream getBinaryStream(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public InputStream getBinaryStream(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public Blob getBlob(int i)  {
		throw new UnsupportedOperationException();
	}

	public Blob getBlob(String colName)  {
		throw new UnsupportedOperationException();
	}

	public byte getByte(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public byte getByte(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public byte[] getBytes(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public byte[] getBytes(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public Reader getCharacterStream(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public Reader getCharacterStream(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public Clob getClob(int i)  {
		throw new UnsupportedOperationException();
	}

	public Clob getClob(String colName)  {
		throw new UnsupportedOperationException();
	}

	public int getConcurrency()  {
		throw new UnsupportedOperationException();
	}

	public String getCursorName()  {
		throw new UnsupportedOperationException();
	}

	public Date getDate(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public Date getDate(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public Date getDate(int columnIndex, Calendar cal)  {
		throw new UnsupportedOperationException();
	}

	public Date getDate(String columnName, Calendar cal)  {
		throw new UnsupportedOperationException();
	}

	public int getFetchDirection()  {
		throw new UnsupportedOperationException();
	}

	public int getFetchSize()  {
		throw new UnsupportedOperationException();
	}

	public ResultSetMetaData getMetaData()  {
		throw new UnsupportedOperationException();
	}

	public Object getObject(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public Object getObject(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public Object getObject(int i, Map<String, Class<?>> map)  {
		throw new UnsupportedOperationException();
	}

	public Object getObject(String colName, Map<String, Class<?>> map)  {
		throw new UnsupportedOperationException();
	}

	public Ref getRef(int i)  {
		throw new UnsupportedOperationException();
	}

	public Ref getRef(String colName)  {
		throw new UnsupportedOperationException();
	}

	public Statement getStatement()  {
		throw new UnsupportedOperationException();
	}

	public Time getTime(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public Time getTime(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public Time getTime(int columnIndex, Calendar cal)  {
		throw new UnsupportedOperationException();
	}

	public Time getTime(String columnName, Calendar cal)  {
		throw new UnsupportedOperationException();
	}

	public Timestamp getTimestamp(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public Timestamp getTimestamp(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal)  {
		throw new UnsupportedOperationException();
	}

	public Timestamp getTimestamp(String columnName, Calendar cal)  {
		throw new UnsupportedOperationException();
	}

	public int getType()  {
		throw new UnsupportedOperationException();
	}

	public URL getURL(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public URL getURL(String columnName)  {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public InputStream getUnicodeStream(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public InputStream getUnicodeStream(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public SQLWarning getWarnings()  {
		throw new UnsupportedOperationException();
	}

	public void insertRow()  {
		throw new UnsupportedOperationException();
	}

	public void moveToCurrentRow()  {
		throw new UnsupportedOperationException();
	}

	public void moveToInsertRow()  {
		throw new UnsupportedOperationException();
	}

	public boolean rowDeleted()  {
		throw new UnsupportedOperationException();
	}

	public boolean rowInserted()  {
		throw new UnsupportedOperationException();
	}

	public boolean rowUpdated()  {
		throw new UnsupportedOperationException();
	}

	public void updateArray(int columnIndex, Array x)  {
		throw new UnsupportedOperationException();
	}

	public void updateArray(String columnName, Array x)  {
		throw new UnsupportedOperationException();
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length)  {
		throw new UnsupportedOperationException();
	}

	public void updateAsciiStream(String columnName, InputStream x, int length)  {
		throw new UnsupportedOperationException();
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x)  {
		throw new UnsupportedOperationException();
	}

	public void updateBigDecimal(String columnName, BigDecimal x)  {
		throw new UnsupportedOperationException();
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length)  {
		throw new UnsupportedOperationException();
	}

	public void updateBinaryStream(String columnName, InputStream x, int length)  {
		throw new UnsupportedOperationException();
	}

	public void updateBlob(int columnIndex, Blob x)  {
		throw new UnsupportedOperationException();
	}

	public void updateBlob(String columnName, Blob x)  {
		throw new UnsupportedOperationException();
	}

	public void updateBoolean(int columnIndex, boolean x)  {
		throw new UnsupportedOperationException();
	}

	public void updateBoolean(String columnName, boolean x)  {
		throw new UnsupportedOperationException();
	}

	public void updateByte(int columnIndex, byte x)  {
		throw new UnsupportedOperationException();
	}

	public void updateByte(String columnName, byte x)  {
		throw new UnsupportedOperationException();
	}

	public void updateBytes(int columnIndex, byte[] x)  {
		throw new UnsupportedOperationException();
	}

	public void updateBytes(String columnName, byte[] x)  {
		throw new UnsupportedOperationException();
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length)  {
		throw new UnsupportedOperationException();
	}

	public void updateCharacterStream(String columnName, Reader reader, int length)  {
		throw new UnsupportedOperationException();
	}

	public void updateClob(int columnIndex, Clob x)  {
		throw new UnsupportedOperationException();
	}

	public void updateClob(String columnName, Clob x)  {
		throw new UnsupportedOperationException();
	}

	public void updateDate(int columnIndex, Date x)  {
		throw new UnsupportedOperationException();
	}

	public void updateDate(String columnName, Date x)  {
		throw new UnsupportedOperationException();
	}

	public void updateDouble(int columnIndex, double x)  {
		throw new UnsupportedOperationException();
	}

	public void updateDouble(String columnName, double x)  {
		throw new UnsupportedOperationException();
	}

	public void updateFloat(int columnIndex, float x)  {
		throw new UnsupportedOperationException();
	}

	public void updateFloat(String columnName, float x)  {
		throw new UnsupportedOperationException();
	}

	public void updateInt(int columnIndex, int x)  {
		throw new UnsupportedOperationException();
	}

	public void updateInt(String columnName, int x)  {
		throw new UnsupportedOperationException();
	}

	public void updateLong(int columnIndex, long x)  {
		throw new UnsupportedOperationException();
	}

	public void updateLong(String columnName, long x)  {
		throw new UnsupportedOperationException();
	}

	public void updateNull(int columnIndex)  {
		throw new UnsupportedOperationException();
	}

	public void updateNull(String columnName)  {
		throw new UnsupportedOperationException();
	}

	public void updateObject(int columnIndex, Object x)  {
		throw new UnsupportedOperationException();
	}

	public void updateObject(String columnName, Object x)  {
		throw new UnsupportedOperationException();
	}

	public void updateObject(int columnIndex, Object x, int scale)  {
		throw new UnsupportedOperationException();
	}

	public void updateObject(String columnName, Object x, int scale)  {
		throw new UnsupportedOperationException();
	}

	public void updateRef(int columnIndex, Ref x)  {
		throw new UnsupportedOperationException();
	}

	public void updateRef(String columnName, Ref x)  {
		throw new UnsupportedOperationException();
	}

	public void updateRow()  {
		throw new UnsupportedOperationException();
	}

	public void updateShort(int columnIndex, short x)  {
		throw new UnsupportedOperationException();
	}

	public void updateShort(String columnName, short x)  {
		throw new UnsupportedOperationException();
	}

	public void updateString(int columnIndex, String x)  {
		throw new UnsupportedOperationException();
	}

	public void updateString(String columnName, String x)  {
		throw new UnsupportedOperationException();
	}

	public void updateTime(int columnIndex, Time x)  {
		throw new UnsupportedOperationException();
	}

	public void updateTime(String columnName, Time x)  {
		throw new UnsupportedOperationException();
	}

	public void updateTimestamp(int columnIndex, Timestamp x)  {
		throw new UnsupportedOperationException();
	}

	public void updateTimestamp(String columnName, Timestamp x)  {
		throw new UnsupportedOperationException();
	}

	public int getHoldability() {
		throw new UnsupportedOperationException();
	}

	public Reader getNCharacterStream(int columnIndex) {
		throw new UnsupportedOperationException();
	}

	public Reader getNCharacterStream(String columnLabel) {
		throw new UnsupportedOperationException();
	}

	public NClob getNClob(int columnIndex) {
		throw new UnsupportedOperationException();
	}

	public NClob getNClob(String columnLabel) {
		throw new UnsupportedOperationException();
	}

	public String getNString(int columnIndex) {
		throw new UnsupportedOperationException();
	}

	public String getNString(String columnLabel) {
		throw new UnsupportedOperationException();
	}

	public RowId getRowId(int columnIndex) {
		throw new UnsupportedOperationException();
	}

	public RowId getRowId(String columnLabel) {
		throw new UnsupportedOperationException();
	}

	public SQLXML getSQLXML(int columnIndex) {
		throw new UnsupportedOperationException();
	}

	public SQLXML getSQLXML(String columnLabel) {
		throw new UnsupportedOperationException();
	}

	public void updateAsciiStream(int columnIndex, InputStream x) {
		throw new UnsupportedOperationException();
	}

	public void updateAsciiStream(String columnLabel, InputStream x) {
		throw new UnsupportedOperationException();
	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateBinaryStream(int columnIndex, InputStream x) {
		throw new UnsupportedOperationException();
	}

	public void updateBinaryStream(String columnLabel, InputStream x) {
		throw new UnsupportedOperationException();
	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateBinaryStream(String columnLabel, InputStream x, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateBlob(int columnIndex, InputStream inputStream) {
		throw new UnsupportedOperationException();
	}

	public void updateBlob(String columnLabel, InputStream inputStream) {
		throw new UnsupportedOperationException();
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateBlob(String columnLabel, InputStream inputStream, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateCharacterStream(int columnIndex, Reader x) {
		throw new UnsupportedOperationException();
	}

	public void updateCharacterStream(String columnLabel, Reader reader) {
		throw new UnsupportedOperationException();
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateCharacterStream(String columnLabel, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateClob(int columnIndex, Reader reader) {
		throw new UnsupportedOperationException();
	}

	public void updateClob(String columnLabel, Reader reader) {
		throw new UnsupportedOperationException();
	}

	public void updateClob(int columnIndex, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateClob(String columnLabel, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateNCharacterStream(int columnIndex, Reader x) {
		throw new UnsupportedOperationException();
	}

	public void updateNCharacterStream(String columnLabel, Reader reader) {
		throw new UnsupportedOperationException();
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateNCharacterStream(String columnLabel, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateNClob(int columnIndex, NClob clob) {
		throw new UnsupportedOperationException();
	}

	public void updateNClob(String columnLabel, NClob clob) {
		throw new UnsupportedOperationException();
	}

	public void updateNClob(int columnIndex, Reader reader) {
		throw new UnsupportedOperationException();
	}

	public void updateNClob(String columnLabel, Reader reader) {
		throw new UnsupportedOperationException();
	}

	public void updateNClob(int columnIndex, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateNClob(String columnLabel, Reader reader, long length) {
		throw new UnsupportedOperationException();
	}

	public void updateNString(int columnIndex, String string) {
		throw new UnsupportedOperationException();
	}

	public void updateNString(String columnLabel, String string) {
		throw new UnsupportedOperationException();
	}

	public void updateRowId(int columnIndex, RowId x) {
		throw new UnsupportedOperationException();
	}

	public void updateRowId(String columnLabel, RowId x) {
		throw new UnsupportedOperationException();
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject) {
		throw new UnsupportedOperationException();
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject) {
		throw new UnsupportedOperationException();
	}

	public boolean isWrapperFor(Class<?> iface) {
		throw new UnsupportedOperationException();
	}

	public <T> T unwrap(Class<T> iface) {
		throw new UnsupportedOperationException();
	}
}
