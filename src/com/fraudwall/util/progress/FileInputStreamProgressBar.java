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
package com.fraudwall.util.progress;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.fraudwall.util.ArgCheck;


/**
 * A {@link ProgressBar} that tracks the progress of reading from a
 * given {@link FileInputStream}.
 *
 * @see CountingProgressBar
 * @author Allan Heydon
 */
public class FileInputStreamProgressBar {
	private final FileChannel channel;
	private final Object lock;
	private final ProgressBar progress;
	private long nextUpdate;

	/**
	 * Constructs a new progress bar on the given file input stream
	 * <code>fis</code>.
	 *
	 * @param lock The object that will be locked before probing the
	 * underlying {@link FileChannel} for the current file position.
	 * This must be non-null, and should be the same object that is
	 * held by all threads performing a read (either directly or
	 * indirectly) against <code>fis</code>.
	 */
	public FileInputStreamProgressBar(FileInputStream fis, Object lock) throws IOException {
		ArgCheck.isNotNull(fis, "fis");
		ArgCheck.isNotNull(lock, "lock");
		channel = fis.getChannel();
		this.lock = lock;
		progress = new ProgressBar(channel.size());
		progress.setETA(true);
		nextUpdate = 0L;
	}

	public void update() throws IOException {
		long pos;
		synchronized (lock) {
			pos = channel.position();
		}
		if (pos >= nextUpdate) {
			nextUpdate = progress.update(pos);
		}
	}

	public void finish() throws IOException {
		long fileSize;
		synchronized (lock) {
			fileSize = channel.size();
		}
		if (fileSize >= nextUpdate) {
			progress.update(fileSize);
		}
		progress.getOutput().println();
	}
}
