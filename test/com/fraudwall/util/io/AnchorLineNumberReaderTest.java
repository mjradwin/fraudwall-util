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
package com.fraudwall.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;

import com.fraudwall.util.AbstractAnchorTest;
/**
 * Tests the {@link AnchorLineNumberReader} implementation.
 *
 * @author Allan Heydon
 */
public class AnchorLineNumberReaderTest extends AbstractAnchorTest {
	private static String[] LINES = new String[] {
		"1: This is line 1",
		"2: This is the second line of the file",
		"3: This line contains a \r character",
		"4: This \r contains \r multiple \r such \r characters",
	};
	private static String[] LINES2 = new String[] {
		"1: This is line 1\r",
		"2: This is the second line of the file\r",
		"3: This line contains a \r character\r",
		"4: This \r contains \r multiple \r such \r characters\r",
	};

	// ---------------------------------------------- constructor

	public void testConstructorSetsLineNumberToZero() {
		AnchorLineNumberReader rd = makeDefaultReader(LINES);
		assertEquals(0, rd.getLineNumber());
	}

	// ---------------------------------------------- getLock()

	public void testGetLockReturnsTheReaderPassedToTheConstructor() {
		Reader underlyingRd = makeUnderlyingReader(makeDefaultContents(LINES));
		AnchorLineNumberReader rd = new AnchorLineNumberReader(underlyingRd);
		assertSame(underlyingRd, rd.getLock());
	}

	// ---------------------------------------------- read()

	public void testReadReturnsNextCharacter() throws Exception {
		checkReadReturnsNextCharacter(LINES);
		checkReadReturnsNextCharacter(LINES2);
	}

	private void checkReadReturnsNextCharacter(String[] lines) throws IOException {
		Reader rd0 = makeUnderlyingReader(makeDefaultContents(lines));
		AnchorLineNumberReader rd1 = makeDefaultReader(lines);
		int c0;
		while ((c0 = rd0.read()) != -1) {
			assertEquals(c0, rd1.read());
		}
		assertEquals(-1, rd1.read());
	}

	public void testReadIncrementsLineNumberOnNewlineCharacter() throws Exception {
		checkReadIncrementsLineNumberOnNewlineCharacter(LINES);
		checkReadIncrementsLineNumberOnNewlineCharacter(LINES2);
	}

	private void checkReadIncrementsLineNumberOnNewlineCharacter(String[] lines)
		throws IOException
	{
		AnchorLineNumberReader rd = makeDefaultReader(lines);
		while (rd.read() != -1);
		assertEquals(lines.length, rd.getLineNumber());
	}

	public void testReadBuffersCorrectly() throws Exception {
		checkReadBuffersCorrectly(8 * 1024);
		checkReadBuffersCorrectly(1024);
		checkReadBuffersCorrectly(13);
		checkReadBuffersCorrectly(1);
	}

	private void checkReadBuffersCorrectly(int buffSz) throws Exception {
		checkReadBuffersCorrectly(buffSz, false);
		checkReadBuffersCorrectly(buffSz, true);
	}

	private void checkReadBuffersCorrectly(int buffSz, boolean includeCR)
		throws IOException
	{
		StringBuilder sb = makeMonsterContents(includeCR);
		Reader rd0 = makeUnderlyingReader(sb);
		AnchorLineNumberReader rd1 = makeReader(sb, buffSz);
		int c0;
		while ((c0 = rd0.read()) != -1) {
			assertEquals(c0, rd1.read());
		}
		assertEquals(-1, rd1.read());
	}

	public void testReadCountsLinesCorrectly() throws Exception {
		checkReadCountsLinesCorrectly(8 * 1024);
		checkReadCountsLinesCorrectly(1024);
		checkReadCountsLinesCorrectly(13);
		checkReadCountsLinesCorrectly(1);
	}

	private void checkReadCountsLinesCorrectly(int buffSz) throws Exception {
		checkReadCountsLinesCorrectly(buffSz, false);
		checkReadCountsLinesCorrectly(buffSz, true);
	}

	private void checkReadCountsLinesCorrectly(int buffSz, boolean includeCR)
		throws IOException
	{
		StringBuilder sb = makeMonsterContents(includeCR);
		int expLineCount = 0;
		AnchorLineNumberReader rd1 = makeReader(sb, buffSz);
		int c;
		while ((c = rd1.read()) != -1) {
			if (c == '\n') expLineCount++;
			assertEquals(expLineCount, rd1.getLineNumber());
		}
		assertEquals(expLineCount, rd1.getLineNumber());
	}

	// ---------------------------------------------- readLine

	public void testReadLineIncrementsLineNumber() throws IOException {
		checkReadLineIncrementsLineNumber(LINES);
		checkReadLineIncrementsLineNumber(LINES2);
	}

	private void checkReadLineIncrementsLineNumber(String[] lines)
		throws IOException
	{
		AnchorLineNumberReader rd = makeDefaultReader(lines);
		String line;
		for (int lineNo = 1; (line = rd.readLine()) != null; lineNo++) {
			assertEquals(lineNo, rd.getLineNumber());
			assertTrue(line.startsWith(lineNo + ":"));
		}
	}

	public void testReadLineIncrementsLineNumberWhenReaderDoesNotEndWithNewline()
		throws IOException
	{
		final String LAST_LINE = "line not ending with newline character";
		AnchorLineNumberReader rd = new AnchorLineNumberReader(new StringReader(LAST_LINE));
		assertEquals(0, rd.getLineNumber());
		assertEquals(LAST_LINE, rd.readLine());
		assertEquals(1, rd.getLineNumber());
	}

	public void testReadLineReadsCorrectResultsWhenReaderEndsWithNewline()
		throws IOException
	{
		checkReadLineReadsCorrectResultsWhenReaderEndsWithNewline(LINES);
		checkReadLineReadsCorrectResultsWhenReaderEndsWithNewline(LINES2);
	}

	private void checkReadLineReadsCorrectResultsWhenReaderEndsWithNewline(
			String[] lines) throws IOException {
		AnchorLineNumberReader rd = makeDefaultReader(lines);
		int lineNo;
		String line;
		for (lineNo = 0; (line = rd.readLine()) != null; lineNo++) {
			String expLine = lines[lineNo];
			if (expLine.endsWith("\r")) {
				expLine = expLine.substring(0, expLine.length() - 1);
			}
			assertEquals(expLine, line);
		}
		assertEquals(lines.length, lineNo);
	}

	public void testReadLineReadsCorrectResultsWhenReaderDoesNotEndWithNewline()
		throws IOException
	{
		final String LAST_LINE = "5: line 5, no trailing newline";
		StringBuilder sb = makeDefaultContents(LINES);
		sb.append(LAST_LINE);
		AnchorLineNumberReader rd = makeReader(sb);
		String line, lastLine = null;
		while ((line = rd.readLine()) != null) {
			lastLine = line;
		}
		assertEquals(LAST_LINE, lastLine);
	}

	public void testReadLineBuffersCorrectly() throws Exception {
		checkReadLineBuffersCorrectly(8 * 1024);
		checkReadLineBuffersCorrectly(1024);
		checkReadLineBuffersCorrectly(13);
		checkReadLineBuffersCorrectly(1);
	}

	private void checkReadLineBuffersCorrectly(int buffSz) throws Exception {
		checkReadLineBuffersCorrectly(buffSz, false);
		checkReadLineBuffersCorrectly(buffSz, true);
	}

	private void checkReadLineBuffersCorrectly(int buffSz, boolean includeCRs)
		throws IOException
	{
		StringBuilder sb = makeMonsterContents(includeCRs);
		BufferedReader rd0 = new BufferedReader(makeUnderlyingReader(sb), buffSz);
		AnchorLineNumberReader rd1 = makeReader(sb, buffSz);
		String s0;
		while ((s0 = rd0.readLine()) != null) {
			assertEquals(s0, rd1.readLine());
		}
		assertNull(rd1.readLine());
	}

	public void testReadLineCountsLinesCorrectly() throws Exception {
		checkReadLineCountsLinesCorrectly(8 * 1024);
		checkReadLineCountsLinesCorrectly(1024);
		checkReadLineCountsLinesCorrectly(13);
		checkReadLineCountsLinesCorrectly(1);
	}

	private void checkReadLineCountsLinesCorrectly(int buffSz) throws Exception {
		checkReadLineCountsLinesCorrectly(buffSz, false);
		checkReadLineCountsLinesCorrectly(buffSz, true);
	}

	private void checkReadLineCountsLinesCorrectly(int buffSz, boolean includeCRs)
		throws IOException
	{
		StringBuilder sb = makeMonsterContents(includeCRs);
		LineNumberReader rd0 = new LineNumberReader(makeUnderlyingReader(sb), buffSz);
		AnchorLineNumberReader rd1 = makeReader(sb, buffSz);
		while (rd0.readLine() != null) {
			rd1.readLine();
			assertEquals(rd0.getLineNumber(), rd1.getLineNumber());
		}
		assertNull(rd1.readLine());
		assertEquals(rd0.getLineNumber(), rd1.getLineNumber());
	}

	// ---------------------------------------------- close

	public void testClosePreservesLineNumber() throws IOException {
		AnchorLineNumberReader rd = makeDefaultReader(LINES);
		int lineCount = 0;
		try {
			while (rd.readLine() != null) {
				lineCount++;
			}
		} finally {
			rd.close();
		}
		assertEquals(lineCount, rd.getLineNumber());
	}

	public void testCloseClosesUnderlyingReader() throws IOException {
		MyStringReader rd0 = new MyStringReader("foo");
		AnchorLineNumberReader rd = new AnchorLineNumberReader(rd0);
		rd.close();
		assertTrue(rd0.wasClosed);
	}

	public void testCloseIsIdempotent() throws IOException {
		MyStringReader rd0 = new MyStringReader("foo");
		AnchorLineNumberReader rd = new AnchorLineNumberReader(rd0);
		rd.close();
		rd0.wasClosed = false;
		rd.close();
		assertFalse(rd0.wasClosed);
	}

	private static class MyStringReader extends StringReader {
		private boolean wasClosed = false;

		public MyStringReader(String s) {
			super(s);
		}

		@Override public void close() {
			wasClosed = true;
			super.close();
		}
	}

	// ---------------------------------------------- private helpers

	private AnchorLineNumberReader makeDefaultReader(String[] lines) {
		return makeReader(makeDefaultContents(lines));
	}

	private AnchorLineNumberReader makeReader(StringBuilder sb) {
		return makeReader(sb, /*buffSz=*/ 13);
	}

	private AnchorLineNumberReader makeReader(StringBuilder sb, int buffSz) {
		return new AnchorLineNumberReader(makeUnderlyingReader(sb), buffSz);
	}

	private Reader makeUnderlyingReader(StringBuilder sb) {
		return new StringReader(sb.toString());
	}

	private StringBuilder makeDefaultContents(String[] lines) {
		StringBuilder res = new StringBuilder(500);
		for (String line: lines) {
			res.append(line).append('\n');
		}
		return res;
	}

	private StringBuilder makeMonsterContents(boolean includeCR) {
		StringBuilder res = new StringBuilder(100000);
		for (int i = 1; i <= 23; i++) {
			for (int j = 0; j < 199; j += i) {
				// add a line of length 'j'
				for (int k = 0; k < j; k++) {
					res.append('+');
				}
				if (includeCR) res.append('\r');
				res.append('\n');
			}
		}
		return res;
	}
}
