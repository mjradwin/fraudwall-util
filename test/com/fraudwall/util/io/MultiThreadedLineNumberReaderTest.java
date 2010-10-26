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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.fraudwall.util.AbstractPropsTest;

/**
 * Tests the {@link MultiThreadedLineNumberReader} implementation.
 *
 * @author Allan Heydon
 */
public class MultiThreadedLineNumberReaderTest extends AbstractPropsTest {
	private static final String INPUT_ROOT = ROOTDIR + "test/com/fraudwall/util/test-input/";
	private static final String UNCOMPRESSED_SMALL_FILE = INPUT_ROOT + "1000-lines.txt";
	private static final String COMPRESSED_SMALL_FILE = UNCOMPRESSED_SMALL_FILE + ".gz";
	private static final int SMALL_FILE_NUM_LINES = 1000;

	private static final String LA_INPUT_DIR = ROOTDIR + "test/com/fraudwall/loganalyzer/input/";
	private static final String COMPRESSED_FILE = LA_INPUT_DIR + "AdGuys-1Hour/adbrite_clicks-2007-07-31-16-00-00-240m.gz";

	// ====================================================== Constructor

	public void testConstructorThrowsFileNotFoundExceptionForNonFile() throws Exception {
		try {
			makeReader(INPUT_ROOT);
			fail();
		} catch (FileNotFoundException ex) {
			// expected case
		}
	}

	public void testConstructorThrowsFileNotFoundExceptionForNonExistentFile() throws Exception {
		try {
			makeReader(INPUT_ROOT + "non-existent-file.txt");
			fail();
		} catch (FileNotFoundException ex) {
			// expected case
		}
	}

	// ====================================================== readLine

	public void testReadLineReturnsNextLine() throws Exception {
		BufferedReader br = new BufferedReader(getInputStreamReader(UNCOMPRESSED_SMALL_FILE));
		MultiThreadedLineNumberReader mtbr = makeReader(COMPRESSED_SMALL_FILE);
		try {
			for (int i = 0; i < SMALL_FILE_NUM_LINES; i++) {
				String expLine = br.readLine();
				String gotLine = mtbr.readLine();
				assertEquals(expLine, gotLine);
			}
		} finally {
			br.close();
			mtbr.close();
		}
	}

	public void testReadLineIncrementsLineNumber() throws Exception {
		MultiThreadedLineNumberReader br = makeReader(COMPRESSED_SMALL_FILE);
		try {
			for (int i = 0; i < SMALL_FILE_NUM_LINES; i++) {
				assertEquals(i, br.getLineNumber());
				br.readLine();
			}
			assertEquals(SMALL_FILE_NUM_LINES, br.getLineNumber());
		} finally {
			br.close();
		}
	}

	public void testReadLineReturnsNullAtEOF() throws Exception {
		MultiThreadedLineNumberReader br = makeReader(COMPRESSED_SMALL_FILE);
		try {
			for (int i = 0; i < SMALL_FILE_NUM_LINES; i++) {
				br.readLine();
			}
			assertNull(br.readLine());
		} finally {
			br.close();
		}
	}

	public void testReadLineThrowsIOExceptionWhenExpected() throws Exception {
		final int N = 10;
		AnchorLineNumberReader rd =
			new MockMultiThreadedBufferedReader(COMPRESSED_SMALL_FILE, N);
		try {
			while (rd.readLine() != null);
			fail("Expected to encounter IOException");
		} catch (IOException ex) {
			assertEquals(N, rd.getLineNumber());
		} finally {
			rd.close();
		}
	}

	public void testReadLineThrowsIfMultiThreadedBufferedReaderHasBeenClosed() throws Exception {
		MultiThreadedLineNumberReader br = makeReader(COMPRESSED_SMALL_FILE);
		br.close();
		try {
			br.readLine();
			fail("Expected IllegalStateException");
		} catch (IllegalStateException ex) {
			// expected case
		}
	}

	// ====================================================== close / isClosed

	public void testCloseCausesIsClosedToReturnTrue() throws Exception {
		MultiThreadedLineNumberReader br = makeReader(COMPRESSED_SMALL_FILE);
		assertFalse(br.isClosed());
		br.close();
		assertTrue(br.isClosed());
	}

	public void testCloseClosesUnderlyingReader() throws Exception {
		MockMultiThreadedBufferedReader mock = new MockMultiThreadedBufferedReader(COMPRESSED_SMALL_FILE);
		assertFalse(mock.underlyingReaderIsClosed());
		mock.close();
		assertTrue(mock.underlyingReaderIsClosed());
	}

	// ====================================================== speed "test"

	/**
	 * Compares the performance of using a normal BufferedReader with the
	 * MultiThreadedBufferedReader on a .gz file. This is not a real unit test
	 * for two reasons: 1) it contains no assertions, and 2) it writes to
	 * System.out. It's meant to be run by hand to measure relative performance.
	 * To run it, change the name of the method so as not to start with an "X".
	 * Important: Do not accidentally check in this class without a leading "X"
	 * on the method name!
	 */
	public void DISABLEDtestMultiThreadedVsSingleThreadedReadSpeed() throws IOException {
		double rate1 = 1.0 / timeMultipleReads(/*multiThreaded=*/ false);
		double rate2 = 1.0 / timeMultipleReads(/*multiThreaded=*/ true);
		System.out.println(String.format("Speedup = %.1f%%", 100.0 * ((rate2 - rate1)/rate1)));
	}

	private long timeMultipleReads(boolean multiThreaded) throws IOException {
		final int N = 10;

		// read through once to warm up caches
		processFile(multiThreaded);

		System.out.println("Reading file " + N + " times; multiThreaded = " + multiThreaded);
		long startT = System.currentTimeMillis();
		for (int i = 0; i < N; i++) {
			processFile(multiThreaded);
		}
		long deltaT = System.currentTimeMillis() - startT;
		System.out.println("  ... in " + deltaT + " ms");
		return deltaT;
	}

	private void processFile(boolean multiThreaded) throws IOException {
		AnchorLineNumberReader rd = makeLineNumberReader(COMPRESSED_FILE, multiThreaded);
		try {
			processBufferedReader(rd);
		} finally {
			rd.close();
		}
	}

	private void processBufferedReader(AnchorLineNumberReader br) throws IOException {
		String line;
		StringBuffer sb = new StringBuffer(10000);
		while ((line = br.readLine()) != null) {
			// process line
			String[] fields = line.split("\t");
			for (String field: fields) {
				if (field.length() > 0) {
					sb.append(field.charAt(field.length() - 1));
				}
			}
		}
	}

	// ====================================================== private helpers

	private static AnchorLineNumberReader makeLineNumberReader(String fileName, boolean multiThreaded)
		throws FileNotFoundException, IOException
	{
		return IOUtils.getLineNumberReader(new File(fileName), IOUtils.UTF8, multiThreaded);
	}

	private static MultiThreadedLineNumberReader makeReader(String fileName) throws Exception {
		return (MultiThreadedLineNumberReader) makeLineNumberReader(fileName, true);
	}

	/**
	 * A mock {@link MultiThreadedLineNumberReader} that has support for testing if it has
	 * been closed and also for throwing an {@link IOException} after a specified
	 * number of lines have been read.
	 *
	 * @author Allan Heydon
	 */
	private static class MockMultiThreadedBufferedReader extends MultiThreadedLineNumberReader {
		private int goodLineCount;
		private boolean brIsClosed = false;

		public MockMultiThreadedBufferedReader(String fileName) throws Exception {
			this(fileName, Integer.MAX_VALUE);
		}

		public MockMultiThreadedBufferedReader(String fileName, int numLines)
			throws Exception
		{
			super(getInputStreamReader(fileName));
			goodLineCount = numLines;
		}

		@Override public String readLine() throws IOException {
			if (goodLineCount-- <= 0) {
				throw new IOException();
			}
			return super.readLine();
		}

		@Override protected void superClose() throws IOException {
			super.close();
			brIsClosed = true;
		}

		public boolean underlyingReaderIsClosed() {
			return brIsClosed;
		}
	}

	/**
	 * Returns a new {@link InputStreamReader} on the given <code>file</code>,
	 * unzipping the file on the fly if the file's name ends with ".gz".
	 * The {@link #UTF8} encoding is used to do byte-to-character conversion.
	 *
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading.
	 * @throws IOException
	 *             if an I/O error occurs or <code>file</code> has a ".bz2"
	 *             extension.
	 */
	private static InputStreamReader getInputStreamReader(String fileName)
		throws FileNotFoundException, IOException
	{
		return IOUtils.getInputStreamReader(new File(fileName), IOUtils.UTF8);
	}

}
