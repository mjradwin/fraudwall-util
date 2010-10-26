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
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Subclass of {@link GZIPInputStream} capable of reading a GZIP'ed file that
 * consists of multiple GZIP'ed files concatenated together. The Sun {@link GZIPInputStream}
 * stops reading such files at the first file part, as documented in Java
 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4691425">bug 4691425</a>.
 * This class is adapted from code found in that bug discussion.
 *
 * @author Allan Heydon
 */
public class MultiMemberGZIPInputStream extends GZIPInputStream {
	private MultiMemberGZIPInputStream root, substream;
	private int buffSize;
	private boolean eos;

	public MultiMemberGZIPInputStream(InputStream in, int buffSize) throws IOException {
		// Wrap the stream in a PushbackInputStream...
		super(new PushbackInputStream(in, buffSize), buffSize);
		this.buffSize = buffSize;
	}

	private MultiMemberGZIPInputStream(MultiMemberGZIPInputStream curr, int size) throws IOException {
		super(curr.in, size);
		this.buffSize = size;
		this.root = (curr.root == null) ? curr : curr.root;
		this.root.substream = this;
	}

	@Override
	public int read(byte[] inputBuffer, int inputBufferOffset, int inputBufferLen) throws IOException {
		if (eos) {
			return -1;
		}

		if (this.substream != null) {
			return this.substream.read(inputBuffer, inputBufferOffset, inputBufferLen);
		}

		int charsRead = super.read(inputBuffer, inputBufferOffset, inputBufferLen);
		if (charsRead == -1) {
			// Push any remaining buffered data back onto the stream
			// If the stream is then not empty, use it to construct
			// a new instance of this class and delegate this and any
			// future calls to it...
			int n = inf.getRemaining() - 8;
			if (n > 0) {
				// More than 8 bytes remaining in inflater
				// First 8 are gzip trailer. Add the rest to any un-read data...
				((PushbackInputStream) this.in).unread(buf, len - n, n);
			} else {
				// Nothing in the buffer. We need to know whether or not
				// there is unread data available in the underlying stream
				// since the base class will not handle an empty file.
				// Read a byte to see if there is data and if so,
				// push it back onto the stream...
				byte[] b = new byte[1];
				int ret = in.read(b, 0, 1);
				if (ret == -1) {
					eos = true;
					return -1;
				} else {
					((PushbackInputStream) this.in).unread(b, 0, 1);
				}
			}

			MultiMemberGZIPInputStream nextSubStream = new MultiMemberGZIPInputStream(this, this.buffSize);
			return nextSubStream.read(inputBuffer, inputBufferOffset, inputBufferLen);
		} else {
			return charsRead;
		}
	}
}
