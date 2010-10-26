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

import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

/**
 * Tests the {@link SqlStatementIterator} implementation.
 *
 * @author Allan Heydon
 */
public class SqlStatementIteratorTest extends TestCase {
	private static final String COMMENT_LINE_1 = "-- This is a comment line.\n";
	private static final String COMMENT_LINE_2 = "\t  --So is this.\n";
	private static final String EMPTY_LINE = "\n";
	private static final String BLANK_LINE = "    \t    \n";
	private static final String STMT_1 = "SELECT * FROM dummy_table";
	private static final String STMT_2 = "SELECT count(*) FROM dummy_table";
	private static final String MULTI_LINE_STMT_PART_1 = "SELECT *";
	private static final String MULTI_LINE_STMT_PART_2 = "FROM dummy_table";
	private static final String MULTI_LINE_STMT_PART_3 = "WHERE cost > 10.0";
	private static final String MULTI_LINE_STMT =
		MULTI_LINE_STMT_PART_1 + " " + MULTI_LINE_STMT_PART_2 + " " + MULTI_LINE_STMT_PART_3;

	public SqlStatementIteratorTest() {
		super("SqlStatementIteratorTest");
	}

	// ================================================ constructor

	public void testConstructorClosesReaderIfItIsEmpty() {
		StringReaderMock r = new StringReaderMock("");
		new SqlStatementIterator(r);
		assertTrue(r.wasCloseCalled());
	}

	public void testConstructorClosesReaderIfItContainsOnlyCommentsAndBlankLines() {
		StringBuilder sb = buildStringContainingBlankAndCommentLinesOnly();
		StringReaderMock r = new StringReaderMock(sb.toString());
		new SqlStatementIterator(r);
		assertTrue(r.wasCloseCalled());
	}

	public void testConstructorThrowsExceptionIfFileStartsWithUnfinishedMultiLineStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append(MULTI_LINE_STMT_PART_1 + "\n");
		Reader r = new StringReader(sb.toString());
		try {
			new SqlStatementIterator(r);
			fail();
		} catch (IllegalStateException ex) {
			// expected case
		}
	}

	// ================================================ hasNext

	public void testHasNextReturnsFalseForEmptyReader() {
		Reader r = new StringReader("");
		SqlStatementIterator it = new SqlStatementIterator(r);
		assertFalse(it.hasNext());
	}

	public void testHasNextReturnsFalseForReaderContainingAllCommentsAndBlankLines() {
		StringBuilder sb = buildStringContainingBlankAndCommentLinesOnly();
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		assertFalse(it.hasNext());
	}

	public void testHasNextReturnsTrueForReaderContainingSingleLineStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append(STMT_1 + ";\n");
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		assertTrue(it.hasNext());
	}

	public void testHasNextReturnsFalseIfRestOfFileIsCommentsOrBlankLinesOnly() {
		StringBuilder sb = new StringBuilder();
		sb.append(STMT_1 + ";\n");
		addBlankAndCommentLines(/*INOUT*/ sb);
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		it.next(); // skip first statement
		assertFalse(it.hasNext());
	}

	public void testHasNextIgnoresCommentsAndBlankLinesBetweenStatements() {
		StringBuilder sb = new StringBuilder();
		sb.append(STMT_1 + ";\n");
		addBlankAndCommentLines(/*INOUT*/ sb);
		sb.append(STMT_2 + ";\n");
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		it.next(); // skip first statement
		assertTrue(it.hasNext());
	}

	// ================================================ next

	public void testNextThrowsIllegalStateExceptionOnExhaustedIterator() {
		Reader r = new StringReader("");
		SqlStatementIterator it = new SqlStatementIterator(r);
		try {
			it.next();
			fail();
		} catch (IllegalStateException ex) {
			// expected case
		}
	}

	public void testNextReturnsSingleLineStatementWithoutTrailingSemicolon() {
		StringBuilder sb = new StringBuilder();
		sb.append(STMT_1 + ";\n");
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		assertEquals(STMT_1, it.next());
	}

	public void testNextReturnsMultipleSingleLineStatementsWithoutTrailingSemicolons() {
		StringBuilder sb = new StringBuilder();
		sb.append(STMT_1 + ";\n");
		sb.append(STMT_2 + ";\n");
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		assertEquals(STMT_1, it.next());
		assertEquals(STMT_2, it.next());
	}

	public void testNextIgnoresBlankAndCommentLines() {
		StringBuilder sb = new StringBuilder();
		addBlankAndCommentLines(/*INOUT*/ sb);
		sb.append(STMT_1 + ";\n");
		addBlankAndCommentLines(/*INOUT*/ sb);
		sb.append(STMT_2 + ";\n");
		addBlankAndCommentLines(/*INOUT*/ sb);
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		assertEquals(STMT_1, it.next());
		assertEquals(STMT_2, it.next());
	}

	public void testNextClosesExhaustedReader() {
		StringReaderMock r = new StringReaderMock(STMT_1 + ";\n");
		SqlStatementIterator it = new SqlStatementIterator(r);
		it.next();
		assertTrue(r.wasCloseCalled());
	}

	public void testNextThrowsExceptionIfFileStartsWithUnfinishedMultiLineStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append(STMT_1 + ";\n");
		sb.append(MULTI_LINE_STMT_PART_1 + "\n");
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		try {
			it.next();
			fail();
		} catch (IllegalStateException ex) {
			// expected case
		}
	}

	public void testNextRecognizesMultiLineStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append(MULTI_LINE_STMT_PART_1 + EMPTY_LINE);
		sb.append(MULTI_LINE_STMT_PART_2 + EMPTY_LINE);
		sb.append(MULTI_LINE_STMT_PART_3 + ";\n");
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		assertEquals(MULTI_LINE_STMT, it.next());
	}

	public void testNextToleratesCommentsAndBlankLinesInMultiLineStatement() {
		StringBuilder sb = new StringBuilder();
		addBlankAndCommentLines(sb);
		sb.append(MULTI_LINE_STMT_PART_1 + EMPTY_LINE);
		addBlankAndCommentLines(sb);
		sb.append(MULTI_LINE_STMT_PART_2 + EMPTY_LINE);
		addBlankAndCommentLines(sb);
		sb.append(MULTI_LINE_STMT_PART_3 + ";\n");
		Reader r = new StringReader(sb.toString());
		SqlStatementIterator it = new SqlStatementIterator(r);
		assertEquals(MULTI_LINE_STMT, it.next());
	}

	// ================================================== close

	public void testCloseInvokesCloseMethodOfUnderlyingReader() {
		StringReaderMock r = new StringReaderMock(STMT_1 + ";\n");
		SqlStatementIterator it = new SqlStatementIterator(r);
		it.close();
		assertTrue(r.wasCloseCalled());
	}

	public void testCloseDoesNotInvokeCloseMethodOfUnderlyingReaderIfAlreadyClosed() {
		StringReaderMock r = new StringReaderMock(STMT_1 + ";\n");
		SqlStatementIterator it = new SqlStatementIterator(r);
		it.close();
		r.resetCloseCalled();
		it.close();
		assertFalse(r.wasCloseCalled());
	}

	// ================================================== helpers

	private StringBuilder buildStringContainingBlankAndCommentLinesOnly() {
		StringBuilder sb = new StringBuilder();
		addBlankAndCommentLines(/*INOUT*/ sb);
		return sb;
	}

	private void addBlankAndCommentLines(/*INOUT*/ StringBuilder sb) {
		sb.append(EMPTY_LINE);
		sb.append(COMMENT_LINE_1);
		sb.append(BLANK_LINE);
		sb.append(COMMENT_LINE_2);
		sb.append(BLANK_LINE);
	}

	private static class StringReaderMock extends StringReader {
		private boolean closeCalled;

		public StringReaderMock(String str) {
			super(str);
			resetCloseCalled();
		}

		@Override
		public void close() {
			super.close();
			closeCalled = true;
		}

		public boolean wasCloseCalled() {
			return closeCalled;
		}

		public void resetCloseCalled() {
			closeCalled = false;
		}
	}
}
