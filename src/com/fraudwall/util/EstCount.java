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
package com.fraudwall.util;

import java.util.BitSet;

/**
 * Keeps an estimate of the number of unique keys that have been seen over some
 * time period. Because the exact answer is computationally expensive to compute,
 * mostly in terms of space, this class allows the client to trade-off space for
 * accuracy.<p>
 *
 * The counter is controlled by three parameters: W, a sliding window of time
 * (e.g, 24 hours); T, an interval of time to slide the window (e.g., 10
 * minutes); and N, the maximum number of distinct values.<p>
 *
 * At first blush, the implementation should keep W/T tables (with W=24hours,
 * T=10minutes, there would be 144 tables of history); each table maintains the
 * distinct keys seen during that time bucket. To find the number of keys seen
 * at a particular instant of time, we look at the current table and the
 * previous W/T tables to find the number of distinct keys across the 1+W/T
 * tables. When the time advances to the next interval, the oldest table is
 * discarded and a new one is created for the current interval.<p>
 *
 * To save space, we don't keep a table for each interval. Rather, we keep a bit
 * vector of size N. The underlying math postulates that this is "good enough"
 * (more details are available on
 * http://server1/wiki/index.php/DistinctHashCounters).<p>
 *
 * Currently, each bit vector is implemented using a BitSet, which is
 * constrained to be a multiple of 64.
 *
 * @author marc
 */
public final class EstCount<K> {

	private final long W; // window
	private final long T; // interval size
	private final int N; // maximum number of keys to count
	private final int blocks; // number of intervals in the window; = 1+int(W/T)

	private BitSet[] bits; // W/T of history, plus the current
	private BitSet acc; // OR of all bit vectors in bits

	private int currentBlock; // index into the bits array
	private long startTimeOfCurrentBlock; // starting time of the currentBlock

	public static <K> EstCount<K> create(long W, long T, int N) {
		return new EstCount<K>(W, T, N);
	}

	private EstCount(long W, long T, int N) {
		if (W % T != 0) {
			throw new IllegalArgumentException(
					"interval must be a factor of the time window");
		}
		this.W = W;
		this.T = T;
		this.N = N;
		blocks = 1 + (int) (this.W / this.T);
		bits = new BitSet[blocks];
		for (int i = 0; i < blocks; i++) {
			bits[i] = new BitSet(N);
		}
		acc = new BitSet(N);
		currentBlock = blocks - 1;
		startTimeOfCurrentBlock = -1;
	}

	/**
	 * Returns the estimate of the number of distinct keys that have been seen
	 * in the past W milliseconds.
	 */
	public int record(K key, long now) {
		if (startTimeOfCurrentBlock < 0) {
			startTimeOfCurrentBlock = now;
		} else if (now >= (startTimeOfCurrentBlock + T)) {
			acc.clear();
			int intervalsToSlide = (int) ((now - startTimeOfCurrentBlock) / T);
			if (intervalsToSlide >= blocks) {
				startTimeOfCurrentBlock = now;
				for (int i = 0; i < blocks; i++) {
					bits[i].clear();
				}
			} else {
				for (int i = 0; i < intervalsToSlide; i++) {
					startTimeOfCurrentBlock += T;
					currentBlock = (currentBlock + 1) % blocks;
					bits[currentBlock].clear();
				}
				for (int i = 0; i < blocks; i++) {
					acc.or(bits[i]);
				}
			}
		}
		int hash = hashOfKey(key, N);
		acc.set(hash);
		bits[currentBlock].set(hash);
		return acc.cardinality();
	}

	/**
	 * Return a hash of the specified key, between 0 and M-1, inclusive.
	 *
	 * NOTE: Using low-order bits; generally, not the most random bits of a
	 * random number.
	 */
	private int hashOfKey(K key, int M) {
		return Math.abs(key.hashCode()) % M;
	}
}
