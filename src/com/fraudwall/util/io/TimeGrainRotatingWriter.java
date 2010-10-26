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
import java.io.Writer;

/**
 * Provides a wrapper on top of a {@link Writer} that
 * writes records into a per-time grain stream.  Subclasses
 * must implement {@link #getWriter(long)} which will
 * be called when a new time grain has been encountered.
 *
 * @author ryan
 */
public abstract class TimeGrainRotatingWriter {
	private Writer out = null;
	private long currentGrain;
	private long grainDuration;

	/**
	 * Construct a new TimeGrainRotatingOutputStream object with
	 * the specified grain duration.
	 * @param grainDuration Duration of the time grain in milliseconds
	 */
	public TimeGrainRotatingWriter(long grainDuration) {
		this.grainDuration = grainDuration;
		this.currentGrain = -1;
	}

	/**
	 * Wrapper around {@link Writer#write(String, int, int)} that writes the
	 * specified String subarray followed by a newline character to the current
	 * stream if this event is in the current time grain. If this event is not
	 * from the current time grain, then this method will open a new output
	 * stream.
	 */
	public synchronized void writeln(long t, String s, int off, int len) throws IOException {
		if (out == null || currentGrain != getTimegrain(t)) {
			currentGrain = getTimegrain(t);
			openWriter(currentGrain);
		}
		out.write(s, off, len);
		out.write('\n');
	}

	/**
	 * Synonymous with write(t, s, 0, s.length()).
	 */
	public void writeln(long t, String s) throws IOException {
		writeln(t, s, 0, s.length());
	}

	/**
	 * Gets the time grain for this time in milliseconds.
	 * @return the first millisecond in this time grain
	 */
	/*package*/ long getTimegrain(long t) {
		return grainDuration * (t/grainDuration);
	}

	/**
	 * Opens the output steam that corresponds to this time and
	 * closes the current output stream if already open.
	 */
	private void openWriter(long t) throws IOException {
		if (out != null) {
			out.close();
		}
		out = getWriter(t);
	}

	/**
	 * Return a new output stream for this time grain.
	 * @param t first millisecond in this time grain
	 */
	public abstract Writer getWriter(long t) throws IOException;

	/**
	 * Close the current output stream
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (out != null) {
			out.close();
		}
	}
}
