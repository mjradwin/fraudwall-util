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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang.time.DateUtils;

import com.fraudwall.util.AbstractAnchorTest;

/**
 * Tests the {@link TimeGrainRotatingWriter} implementation.
 *
 * @author ryan
 */
public class TimeGrainRotatingWriterTest extends AbstractAnchorTest {

	private static final long GRAIN = DateUtils.MILLIS_PER_DAY;
	private static final String LINE1 = "This is a line of test data.";
	private static final String LINE2 = "This is another line of test data.";

	private MockTimeGrainRotatingOutputStream mock;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mock = new MockTimeGrainRotatingOutputStream();
	}

	@Override
	public void tearDown() throws Exception {
		mock = null;
		super.tearDown();
	}

	// ==================================================== getTimegrain

	public void testGetTimegrainReturnsCorrectTimegrain() {
		assertEquals(0, mock.getTimegrain(0));
		assertEquals(0, mock.getTimegrain(GRAIN - 1));
		assertEquals(GRAIN, mock.getTimegrain(GRAIN));
		assertEquals(GRAIN, mock.getTimegrain(2*GRAIN-1));
	}

	// ==================================================== write

	public void testWriteOpensNewOutputStreamWhenFirstWriteIsCalled() throws IOException {
		mock.writeln(0L, LINE1);
		assertEquals(1, mock.getNumStreamsOpened());
	}

	public void testWriteOpensNewOutputStreamWhenNewTimeGrainIsPresent() throws IOException {
		mock.writeln(0, LINE1);
		assertEquals(1, mock.getNumStreamsOpened());
		mock.writeln(GRAIN - 1, LINE1);
		assertEquals(1, mock.getNumStreamsOpened());
		mock.writeln(GRAIN, LINE1);
		assertEquals(2, mock.getNumStreamsOpened());
	}

	public void testWriteActuallyWritesDataToTheCorrectOutputStream() throws IOException {
		mock.writeln(0, LINE1);
		mock.writeln(GRAIN, LINE2);
		assertEquals(LINE1 + "\n", new String(mock.getPreviousOutput()));
		assertEquals(LINE2 + "\n", new String(mock.getCurrentOutput()));
	}

	public void testWriteClosesOutputStreamWhenOpeningNewStream() throws IOException {
		mock.writeln(0, LINE1);
		mock.writeln(GRAIN, LINE2);
		assertEquals(true, mock.isPreviousClosed());
	}

	// ==================================================== close

	public void testCloseIsNullSafe() throws IOException {
		mock.close();
	}

	public void testCloseClosesCurrentOutputStream() throws IOException {
		mock.writeln(0, LINE1);
		mock.writeln(GRAIN, LINE2);
		assertEquals(true, mock.isPreviousClosed());
		assertEquals(false, mock.isCurrentClosed());
		mock.close();
		assertEquals(true, mock.isPreviousClosed());
	}

	// ==================================================== private helper classes

	static class MockTimeGrainRotatingOutputStream extends TimeGrainRotatingWriter {
		private int numStreamsOpened;
		private MockByteArrayWriter out;
		private MockByteArrayWriter prev;

		public MockTimeGrainRotatingOutputStream() {
			super(GRAIN);
		}

		@Override
		public Writer getWriter(long t) {
			numStreamsOpened++;
			prev = out;
			out = new MockByteArrayWriter();
			return out;
		}

		public int getNumStreamsOpened() {
			return numStreamsOpened;
		}

		public String getPreviousOutput() {
			return prev.toString();
		}

		public boolean isPreviousClosed() {
			return prev.isClosed();
		}

		public boolean isCurrentClosed() {
			return out.isClosed();
		}

		public String getCurrentOutput() {
			return out.toString();
		}
	}

	/**
	 * Just like a ByteArrayOutputStream but it records if it has
	 * been closed.
	 * @author ryan
	 *
	 */
	static class MockByteArrayWriter extends StringWriter {
		private boolean isClosed = false;

		@Override
		public void close() throws IOException {
			super.close();
			isClosed = true;
		}

		public boolean isClosed() {
			return isClosed;
		}
	}
}
