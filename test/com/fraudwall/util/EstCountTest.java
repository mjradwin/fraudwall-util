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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tests the {@link EstCount} implementation.
 */
public class EstCountTest extends TestCase {
	private static final Log log = LogFactory.getLog(EstCountTest.class);

	long[] keys =  { 3,   1,  3,  5,  7,  2, 51, 52, 53, 54, 55, 56,  5,  7,  3};
	long[] times = { 0,  10, 11, 12, 40, 50, 51, 52, 53, 54, 55, 56, 60, 70, 80};

	public void test1() {
		doit(20, 10, 64);
	}

	public void test1a() {
		doit(20, 10, 4);
	}

	public void test2() {
		doit(20, 5, 64);
	}

	public void test3() {
		doit(15, 5, 64);
	}

	public void doit(int W, int T, int N) {
		EstCount<Long> e = EstCount.create(W, T, N);

		log.debug(String.format("Window=%d, Slider=%d, N=%d\n", W, T, N));
		for (int i = 0; i < keys.length; i++) {
			int v = e.record(keys[i], times[i]);
			log.debug(String.format("\ttime = %d, key = %d, return value = %d\n", times[i], keys[i], v));
		}
		assertTrue(true);
	}

}
