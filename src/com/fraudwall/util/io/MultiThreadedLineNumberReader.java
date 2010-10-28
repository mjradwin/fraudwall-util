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
import java.nio.CharBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.fraudwall.util.AnchorThread;
import com.fraudwall.util.exc.ArgCheck;
import com.fraudwall.util.exc.Require;

/**
 * Buffered reader that forks a separate thread to read lines from the backing
 * reader. This is useful if getting lines from the backing reader is compute
 * intensive, such as when reading from a compressed file and decompressing
 * on-the-fly. In such cases, overall throughput may be improved when running on
 * a multi-processor.
 * <p>
 * This class actually extends {@link AnchorLineNumberReader}, which means it
 * also includes support for counting the number of lines that have been read.
 * <p>
 * Because a separate thread reads lines from the backing reader, clients of
 * this class should call the {@link #readLine} method only to read lines.
 * Calling any of the other methods for reading from or repositioning the
 * backing reader will result in an {@link UnsupportedOperationException}.
 * These methods include {@link #read()}, {@link #read(char[])},
 * {@link #read(java.nio.CharBuffer)}, {@link #read(char[], int, int)},
 * {@link #ready}, {@link #reset()}, and {@link #skip}.
 * <p>
 * If clients read an instance of this class until it is exhausted (i.e., until
 * a call of {@link #readLine} has returned null), the backing reader thread will exit
 * automatically. However, those clients not reading to end-of-file must call
 * the {@link #close} method to terminate the backing reader thread. (It's a
 * good idea to call {@link #close} in any case to release handles on the underlying
 * file system resources.)
 * <p>
 * The methods of this class are not thread-safe. Although the implementation of
 * this class guarantees thread-safe access to internal data structures, clients
 * are required to guarantee that calls of this class's public methods are
 * invoked by at most a single thread at a time.
 *
 * @author Allan Heydon
 */
public class MultiThreadedLineNumberReader extends AnchorLineNumberReader {
	/**
	 * The default number of lines buffered between the producer and
	 * consumer threads. The lengths of those lines is unbounded. This
	 * parameter is quite different from the size of the <em>character</em>
	 * buffer used by the superclass.
	 */
	private static final int DEFAULT_BUFF_SZ = 100;

	/** Sentinel used to indicate that the end of the input file has been reached. */
	private static final Object EOF_OBJECT = new Object();

	/**
	 * Number of lines read by the client. This will be at most the number
	 * of lines actually read from the underlying reader.
	 */
	private int clientLineNumber;

	private BlockingQueue<Object> q;
	private Thread th;

	public MultiThreadedLineNumberReader(Reader in) {
		this(in, DEFAULT_BUFF_SZ);
	}

	public MultiThreadedLineNumberReader(Reader in, int lineBufferSize) {
		super(in);
		ArgCheck.isTrue(lineBufferSize > 0, "lineBufferSize parameter must be positive");
		clientLineNumber = 0;
		q = new ArrayBlockingQueue<Object>(lineBufferSize);
		th = new ReaderThread(this, q);
		th.start();
	}

	/**
	 * Invokes the {@link AnchorBufferedReader#readLine readLine} method of the super
	 * class. Required so ReaderThread can invoke the non-overridden readLine method.
	 */
	private String superReadLine() throws IOException {
		return super.readLine();
	}

	/**
	 * Thread that reads lines from an {@link AnchorLineNumberReader} and puts them on a
	 * {@link BlockingQueue}. When it hits end-of-file, it puts the {@link #EOF_OBJECT}
	 * sentinel on the queue and exits; when it encounters an IOException, it puts the
	 * exception on the queue and exits. It also exits silently if it is interrupted.
	 */
	private static class ReaderThread extends AnchorThread {
		private final MultiThreadedLineNumberReader rd;
		private final BlockingQueue<Object> q;

		private ReaderThread(MultiThreadedLineNumberReader br, BlockingQueue<Object> q) {
			super("MultiThreadedReaderThread");
			this.rd = br;
			this.q = q;
		}

		@Override
		public void Run() {
			try {
				try {
					String line;
					while ((line = rd.superReadLine()) != null) {
						q.put(line);
					}
					q.put(EOF_OBJECT);
				} catch (IOException ex) {
					q.put(ex);
				}
			} catch (InterruptedException ex) {
				// nothing to do; exit thread cleanly
			}
		}
	}

	@Override
	public int getLineNumber() {
		return clientLineNumber;
	}

	@Override
	public String readLine() throws IOException {
		Require.isFalse(isClosed(), "MultiThreadedBufferedReader has been closed");
		try {
			Object next = q.take();
			if (next instanceof IOException) {
				throw (IOException) next;
			} else if (next == EOF_OBJECT) {
				return null;
			} else {
				clientLineNumber++;
				return (String) next;
			}
		} catch (InterruptedException ex) {
			// this should never happen
			throw new IllegalStateException("Reading thread was unexpectedly interrupted");
		}
	}

	/**
	 * Invokes the {@link AnchorLineNumberReader#close close} method on the
	 * super class. Useful for testing purposes.
	 */
	protected void superClose() throws IOException {
		super.close();
	}

	@Override
	public void close() throws IOException {
		if (!isClosed()) {
			th.interrupt();
			try {
				th.join();
			} catch (InterruptedException ex) {
				// ignore
			} finally {
				q = null;
				th = null;
				superClose();
			}
		}
	}

	/**
	 * Returns true if and only if this method's {@link #close} method
	 * has been invoked.
	 */
	public boolean isClosed() {
		return (q == null);
	}

	// unsupported operations

	@Override public int read() {
		throw new UnsupportedOperationException("read() not supported");
	}

	@Override public int read(char[] cbuf) {
		throw new UnsupportedOperationException("read(char[]) not supported");
	}

	@Override public int read(char[] cbuf, int off, int len) {
		throw new UnsupportedOperationException("read(char[],int,int) not supported");
	}

	@Override public int read(CharBuffer target) {
		throw new UnsupportedOperationException("read(Charbuffer) not supported");
	}

	@Override public boolean ready() {
		throw new UnsupportedOperationException("ready() not supported");
	}

	@Override public void reset() {
		throw new UnsupportedOperationException("reset() not supported");
	}

	@Override public long skip(long n) {
		throw new UnsupportedOperationException("skip(long) not supported");
	}

}
