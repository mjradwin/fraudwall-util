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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.fraudwall.util.AbstractAnchorTest;
import com.fraudwall.util.AnchorFatalError;

/**
 * Tests the {@link AnchorCsvWriter} implementation.
 *
 * @author Allan Heydon
 */
public class AnchorCsvWriterTest extends AbstractAnchorTest {
	private static final String ALREADY_CLOSED_WRITTER_ERROR_MESSAGE =
		"This instance of the AnchorCsvWriter class has already been closed.";

	private static final String[] VALUES = new String[] {
		"foo", "  foo", "foo  ", "  foo  ", "\tfoo", "foo\t", "\tfoo\t"
	};

	private File tempFile;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tempFile = File.createTempFile("AnchorCsvWriterTest", ".csv");
	}

	@Override
	protected void tearDown() throws Exception {
		tempFile.delete();
		super.tearDown();
	}

	/** {@link AnchorCsvWriter#setDelimiter(char)} -------------------------------------------------------- */

	public void testSetDelimiterChangesDelimiterUsedInWritingSubsequentLines() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile);
		try {
		wr.writeRecord("abc", "def");
		wr.setDelimiter('|');
		wr.writeRecord("abc", "def");
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "abc,def", "abc|def");
	}

	/** {@link AnchorCsvWriter#write(String)} ------------------------------------------------------------- */

	public void testWriteThrowsIfWriterHasBeenClosed() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile);
		wr.close();
		try {
			wr.write("foo");
			fail();
		} catch (IOException ex) {
			assertEquals(ALREADY_CLOSED_WRITTER_ERROR_MESSAGE, ex.getMessage());
		}
	}

	public void testWriteDoesNotTrimLeadingOrTrailingWhiteSpace() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile);
		try {
			for (String value: VALUES) {
				wr.write(value);
			}
			wr.endRecord();
		} finally {
			wr.close();
		}
		checkFileContents();
	}

	public void testWriteConvertsNullToEmptyString() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile);
		try {
			wr.write("a"); wr.write(null); wr.write("b"); wr.write(null); wr.write("c");
			wr.endRecord();
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "a,,b,,c");
	}

	public void testWriteQualifiesDelimiter() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile, '|');
		try {
			wr.write("abc|def"); wr.write("ghi");
			wr.endRecord();
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "\"abc|def\"|ghi");
	}

	public void testWriteQualifiesAndDoublesTextQualifier() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile, '|');
		try {
			wr.write("abc\"def"); wr.write("ghi");
			wr.endRecord();
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "\"abc\"\"def\"|ghi");
	}

	public void testWriteQualifiesNewline() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile, '|');
		try {
			wr.write("abc\ndef"); wr.write("ghi");
			wr.endRecord();
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "\"abc", "def\"|ghi");
	}

	public void testWriteQualifiesLineFeed() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile, '|');
		try {
			wr.write("abc\rdef"); wr.write("ghi");
			wr.endRecord();
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "\"abc\rdef\"|ghi");
	}

	public void testWriteQualifiesFirstFieldStartingWithCommentCharacter() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile, '|');
		try {
			wr.write("#abc"); wr.write("ghi");
			wr.endRecord();
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "\"#abc\"|ghi");
	}

	public void testWriteDoesNotQualifyNonInitialFieldStartingWithCommentCharacter() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile, '|');
		try {
			wr.write("abc"); wr.write("#ghi");
			wr.endRecord();
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "abc|#ghi");
	}

	public void testWriteQualifiesEmptyFirstField() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile, '|');
		try {
			wr.write(""); wr.write("ghi");
			wr.endRecord();
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "\"\"|ghi");
	}

	/** {@link AnchorCsvWriter#writeRecord(String[])} ----------------------------------------------------- */

	public void testWriteRecordDoesNotTrimLeadingOrTrailingWhiteSpace() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile);
		try {
			wr.writeRecord(VALUES);
		} finally {
			wr.close();
		}
		checkFileContents();
	}

	public void testWriteRecordConvertsNullToEmptyString() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile);
		try {
			wr.writeRecord("a", null, "b", null, "c");
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile, "a,,b,,c");
	}

	public void testWriteRecordQualifiesFieldsCorrectly() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile, '|');
		try {
			wr.writeRecord("abc|def", "ghi");  // delimiter
			wr.writeRecord("abc\"def", "ghi"); // text qualifier
			wr.writeRecord("abc\ndef", "ghi"); // newline
			wr.writeRecord("abc\rdef", "ghi"); // linefeed
			wr.writeRecord("#abc", "ghi");     // first field comment
			wr.writeRecord("abc", "#ghi");     // non-first field comment
			wr.writeRecord("", "ghi");         // empty first field
		} finally {
			wr.close();
		}
		assertFileEquals(tempFile,
			"\"abc|def\"|ghi",
			"\"abc\"\"def\"|ghi",
			"\"abc", "def\"|ghi",
			"\"abc\rdef\"|ghi",
			"\"#abc\"|ghi",
			"abc|#ghi",
			"\"\"|ghi"
		);
	}

	/** {@link AnchorCsvWriter#endRecord()} -------------------------------------------------------------- */

	public void testEndRecordThrowsIfWriterHasBeenClosed() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile);
		wr.close();
		try {
			wr.endRecord();
			fail();
		} catch (IOException ex) {
			assertEquals(ALREADY_CLOSED_WRITTER_ERROR_MESSAGE, ex.getMessage());
		}
	}

	/** {@link AnchorCsvWriter#flush()} ------------------------------------------------------------------ */

	public void testFlushConvertsIOExceptionToAnchorFatalError() throws Exception{
		AnchorCsvWriter wr = new AnchorCsvWriter(createThrowingWriter(), ',');
		try {
			wr.flush();
			fail();
		} catch (AnchorFatalError ex) {
			assertEquals("Unexpected I/O exception", ex.getMessage());
			Throwable th = ex.getCause();
			assertInstanceOf(th, IOException.class);
			assertEquals("flush IOException", th.getMessage());
		}
	}

	/** {@link AnchorCsvWriter#close()} ------------------------------------------------------------------ */

	public void testCloseIsANoopOnAClosedWriter() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(tempFile);
		wr.close();
		wr.close();
	}

	public void testCloseConvertsIOExceptionToAnchorFatalError() throws Exception {
		AnchorCsvWriter wr = new AnchorCsvWriter(createThrowingWriter(), ',');
		try {
			wr.close();
			fail();
		} catch (AnchorFatalError ex) {
			assertEquals("Unexpected I/O exception", ex.getMessage());
			Throwable th = ex.getCause();
			assertInstanceOf(th, IOException.class);
			assertEquals("close IOException", th.getMessage());
		}
	}

	// --------------------------------------------- private helpers

	/**
	 * Returns a {@link Writer} on {@code tempFile} whose {@link Writer#flush()} and {@link Writer#close()}
	 * methods both throw an {@link IOException}.
	 */
	private Writer createThrowingWriter() throws FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), IOUtils.UTF8)) {
			@Override public void close() throws IOException {
				throw new IOException("close IOException");
			}

			@Override public void flush() throws IOException {
				throw new IOException("flush IOException");
			}
		};
	}

	// need a replacement for CsvReader with a better license
	private void checkFileContents() throws FileNotFoundException, IOException {
		AnchorLineNumberReader rd = new AnchorLineNumberReader(new FileReader(tempFile));
		try {
			rd.readLine();
		} finally {
			rd.close();
		}
		/*
		CsvReader rd = new CsvReader(tempFile.getPath());
		rd.setTrimWhitespace(false);
		try {
			assertTrue(rd.readRecord());
			for (int i = 0; i < VALUES.length; i++) {
				assertEquals(VALUES[i], rd.get(i));
			}
			assertFalse(rd.readRecord());
		} finally {
			rd.close();
		}
		*/
	}
}
