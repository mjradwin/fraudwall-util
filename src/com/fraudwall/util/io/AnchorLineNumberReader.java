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
import java.io.Reader;

/**
 * Reads text from a character-input stream, buffering characters so as to
 * provide for the efficient reading of characters, character arrays, and
 * lines. Also includes support for automatically counting line numbers.
 * <p>
 * This is exactly like a combination of Java's {@link java.io.LineNumberReader}
 * and its {@link java.io.BufferedReader} superclass, except that:
 * <ol>
 * <li>it does not support the {@link Reader#mark} operation;</li>
 * <li>it does not support the {@link java.io.LineNumberReader#setLineNumber}
 * operation; and</li>
 * <li>it accepts only single line feed characters ('\n') or a carriage return
 * ('\r') immediately followed by a line feed ('\n') as line terminators
 * (i.e., it treats stand-alone carriage returns ('\r') in the middle of a line
 * as normal characters).</li>
 * </ol>
 * (The second of these differences could easily be eliminated, but providing
 * clients with the ability to artificially change the current line number
 * seems like a dangerous feature to support in the absence of a clear
 * need for it.)
 */
public class AnchorLineNumberReader extends Reader {
	/** The underlying reader. */
	private Reader in;

	/** The buffer of characters last read from 'in'. */
	private char cb[];

	/** The number of characters in the buffer 'cb'. */
	private int nChars;

	/**
	 * The index of the next character to read from the buffer 'cb',
	 * which is also the number of characters that have already been
	 * read from that buffer.
	 */
	private int nextChar;

	/** The current line number */
	private int lineNumber = 0;

	private static final int DEFAULT_BUFFER_SIZE = 8192;
	private static final int EXPECTED_MAX_LINE_LENGTH = 400;

	/**
	 * Create a buffering character-input stream on the given underlying
	 * Reader <code>in</code> that uses an input buffer of size <code>sz</code>.
	 *
	 * @exception IllegalArgumentException If <code>sz</code> <= 0.
	 * @see #AnchorLineNumberReader(Reader)
	 */
	public AnchorLineNumberReader(Reader in, int sz) {
		super(in);
		if (sz <= 0) {
			throw new IllegalArgumentException("Buffer size <= 0");
		}
		this.in = in;
		cb = new char[sz];
		nextChar = nChars = 0;
	}

	/**
	 * Create a buffering character-input stream on the given underlying
	 * Reader <code>in</code> that uses a default-sized input buffer.
	 *
	 * @see #AnchorLineNumberReader(Reader, int)
	 */
	public AnchorLineNumberReader(Reader in) {
		this(in, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Returns the lock that this class uses to synchronize readers
	 * on the underlying {@link Reader} passed to the constructor.
	 * Clients that wish to access the underlying reader in any
	 * way in a separate thread must synchronize on this lock to
	 * prevent race conditions.
	 */
	public Object getLock() {
		return in;
	}

	/**
	 * Returns the number of lines that have been read successfully
	 * by the {@link #readLine()} method. Before any lines have been
	 * read, this method returns 0.
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/** Check to make sure that the stream has not been closed */
	private void ensureOpen() throws IOException {
		if (in == null) {
			throw new IOException("Stream closed");
		}
	}

	/**
	 * Fills the input buffer. Requires that 'lock' is held.
	 */
	private void fill() throws IOException {
		int n;
		do {
			n = in.read(cb, 0, cb.length);
		} while (n == 0);
		if (n > 0) {
			nChars = n;
			nextChar = 0;
		}
	}

	/**
	 * Reads and returns the next character from the input. This method also
	 * has the side-effect of incrementing the input line number if the
	 * character returned is a line feed ('\n') character.
	 *
	 * @return The character read, as an integer in the range 0 to 65535 (<tt>0x00-0xffff</tt>),
	 *         or -1 if the end of the stream has been reached.
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public int read() throws IOException {
		synchronized (lock) {
			ensureOpen();
			if (atEOF()) {
				return -1;
			}
			return incLineCount(cb[nextChar++]);
		}
	}

	/**
	 * Increments the line count if <code>c</code> is a line feed.
	 * Requires that 'lock' is held.
	 */
	private char incLineCount(char c) {
		if (c == '\n') {
			lineNumber++;
		}
		return c;
	}

	/**
	 * Reads characters into a portion of an array, reading from the underlying
	 * stream if necessary. Requires that 'lock' is held.
	 */
	private int readOnce(char[] cbuf, int off, int len) throws IOException {
		if (isBufferEmpty()) {
			/*
			 * If the requested length is at least as large as the buffer
			 * do not bother to copy the characters into the local buffer.
			 * In this way buffered streams will cascade harmlessly.
			 */
			if (len >= cb.length) {
				return in.read(cbuf, off, len);
			}
			fill();
			if (isBufferEmpty()) {
				return -1;
			}
		}
		int n = Math.min(len, nChars - nextChar);
		System.arraycopy(cb, nextChar, cbuf, off, n);
		nextChar += n;
		return n;
	}

	/**
	 * Reads characters into a portion of an array.
	 * <p>
	 * This method implements the general contract of the corresponding
	 * <code>{@link Reader#read(char[], int, int) read}</code> method of the
	 * <code>{@link Reader}</code> class. As an additional convenience, it
	 * attempts to read as many characters as possible by repeatedly invoking
	 * the <code>read</code> method of the underlying stream. This iterated
	 * <code>read</code> continues until one of the following conditions
	 * becomes true:
	 * <ul>
	 * <li>The specified number of characters have been read,</li>
	 * <li>The <code>read</code> method of the underlying stream returns
	 * <code>-1</code>, indicating end-of-file, or</li>
	 * <li> The <code>ready</code> method of the underlying stream returns
	 * <code>false</code>, indicating that further input requests would
	 * block.</li>
	 * </ul>
	 * If the first <code>read</code> on the underlying stream returns
	 * <code>-1</code> to indicate end-of-file then this method returns
	 * <code>-1</code>. Otherwise this method returns the number of
	 * characters actually read.
	 * <p>
	 * Subclasses of this class are encouraged, but not required, to attempt to
	 * read as many characters as possible in the same fashion.
	 * <p>
	 * Ordinarily this method takes characters from this stream's character
	 * buffer, filling it from the underlying stream as necessary. If, however,
	 * the buffer is empty, the mark is not valid, and the requested length is
	 * at least as large as the buffer, then this method will read characters
	 * directly from the underlying stream into the given array. Thus redundant
	 * <code>BufferedReader</code>s will not copy data unnecessarily.
	 *
	 * @param cbuf
	 *            Destination buffer.
	 * @param off
	 *            Offset at which to start storing characters.
	 * @param len
	 *            Maximum number of characters to read.
	 *
	 * @return The number of characters read, or -1 if the end of the stream has
	 *         been reached.
	 *
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public int read(char cbuf[], int off, int len) throws IOException {
		synchronized (lock) {
			ensureOpen();
			if ((off < 0) || (off > cbuf.length) || (len < 0)
				|| ((off + len) > cbuf.length) || ((off + len) < 0))
			{
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}

			int n = readOnce(cbuf, off, len);
			if (n <= 0) {
				return n;
			}
			while ((n < len) && in.ready()) {
				int n1 = readOnce(cbuf, off + n, len - n);
				if (n1 <= 0) {
					break;
				}
				n += n1;
			}
			incLineCount(cbuf, off, n);
			return n;
		}
	}

	/**
	 * Reads a line of text. A line is considered to be terminated by a line feed
	 * character ('\n') or by the end of the underlying input reader.
	 *
	 * @return A String containing the contents of the line, not including any
	 *         terminating line feed character, or null if the end of the stream
	 *         has been reached.
	 *
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	public String readLine() throws IOException {
		// Accumulates contents of line when line spans (at least one) buffer boundary.
		// If line is entirely within the contents of the current buffer 'cb', this local
		// variable will remain null (no StringBuilder is allocated).
		StringBuilder s = null;

		synchronized (lock) {
			ensureOpen();

			while (true) {
				if (atEOF()) {
					if (s != null) {
						lineNumber++;
						return s.toString();
					}
					return null;
				}
				int startChar = nextChar;
				boolean gotLineFeed = skipToLineFeed();
				int len = nextChar - startChar;

				if (gotLineFeed) {
					// found end of line in the current buffer
					String res = makeReadLineResult(startChar, len, s);
					lineNumber++;
					nextChar++; // skip past '\n'
					return res;
				}

				// line spans buffer boundary; append to 's' and continue
				if (s == null) {
					s = new StringBuilder(Math.max(EXPECTED_MAX_LINE_LENGTH, 2 * len));
				}
				assert len > 0;
				s.append(cb, startChar, len);
			}
		}
	}

	/**
	 * Requires that 'lock' is held.
	 */
	private String makeReadLineResult(int startChar, int len, StringBuilder s) {
		// if a carriage return occurs immediately before the line feed, don't include it.
		if (len == 0 && s != null && s.length() > 0 && s.charAt(s.length() - 1) == '\r') {
			s.setLength(s.length() - 1);
		} else if (len > 0 && cb[startChar + len - 1] == '\r') {
			len--;
		}
		return (s == null)
			? new String(cb, startChar, len)
			: s.append(cb, startChar, len).toString();
	}

	/**
	 * Advances 'nextChar' to the index of the next line feed character
	 * if one is found, and returns true. In the event that no line feed
	 * character exists in the remainder of the buffer, sets 'nextChar'
	 * to 'nChars' and returns false.
	 * <p>
	 * Requires that 'lock' is held.
	 */
	private boolean skipToLineFeed() {
		for (; nextChar < nChars; nextChar++) {
			if ((cb[nextChar] == '\n')) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to skip <code>n</code> characters.
	 *
	 * @return The number of characters actually skipped
	 *
	 * @exception IllegalArgumentException
	 *                If <code>n</code> is negative.
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public long skip(long n) throws IOException {
		if (n < 0L) {
			throw new IllegalArgumentException("skip value is negative");
		}
		synchronized (lock) {
			ensureOpen();
			long remainingCnt = n;
			while (remainingCnt > 0) {
				if (atEOF()) {
					break;
				}
				int advanceCnt = (int) Math.min(remainingCnt, nChars - nextChar);
				incLineCount(cb, nextChar, advanceCnt);
				remainingCnt -= advanceCnt;
				nextChar += advanceCnt;
			}
			return n - remainingCnt;
		}
	}

	/**
	 * Returns true if and only if this stream is ready to be read. A buffered
	 * character stream is ready if the buffer is not empty, or if the
	 * underlying character stream is ready.
	 *
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public boolean ready() throws IOException {
		synchronized (lock) {
			ensureOpen();
			return !isBufferEmpty() || in.ready();
		}
	}

	/**
	 * Closes this reader and all its underlying resources. This method is idempotent:
	 * all but the first call to this method are no-ops.
	 *
	 * @exception IOException If an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		synchronized (lock) {
			if (in != null) {
				in.close();
				in = null;
				cb = null;
			}
		}
	}

	/**
	 * Returns true if and only if there are no more characters to read.
	 * Requires that 'lock' is held.
	 */
	private boolean atEOF() throws IOException {
		if (isBufferEmpty()) {
			fill();
			if (isBufferEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Increments the 'lineNumber' member field by the number of
	 * line feed ('\n') characters contained in the sub-array
	 * cbuf[off] through cbuf[off + n - 1].
	 * <p>
	 * Requires that 'lock' is held.
	 */
	private void incLineCount(char[] cbuf, int off, int n) {
		for (int i = off; i < off + n; i++) {
			if (cbuf[i] == '\n') {
				lineNumber++;
			}
		}
	}

	/**
	 * Returns true if and only if the character buffer has been exhausted.
	 * Requires that 'lock' is held.
	 */
	private boolean isBufferEmpty() {
		return nextChar >= nChars;
	}
}
