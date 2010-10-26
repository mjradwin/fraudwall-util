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

import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DecayVariableCollectionTest extends TestCase {

	private static final long[] KEYS = { 1001, 2002, 1001, 3003, 3003, 5005,
			6006, 1001, 4004, 1001, 3003, 2002, 4004, 1001, 5005, 5005, 1001,
			2002, 5005, 1001, 1001, 2002 };

	private final Log log = LogFactory
			.getLog(DecayVariableCollectionTest.class);

	@SuppressWarnings("unchecked")
	public void testUpdate1() {
		DecayVariableCollection<Integer> d = makeDecayVariableCollection("test1", 7, 8.0, 0.25, 20);

		Random r = new Random(0);
		long time = 100;
		for (int i = 0; i < 10000; i++) {
			Integer key = new Integer(r.nextInt(10));
			if (i % 1000 == 0) {
				updateCollectionForKey(d, key, time);
			} else {
				d.update(key, time);
			}
			time += 2;
		}
		IndexedPriorityQueue pq = d.getPQ();
		assertEquals(7, pq.size());
	}

	@SuppressWarnings("unchecked")
	public void testUpdate2() {
		DecayVariableCollection<String> d = makeDecayVariableCollection("test2", 5, 2.0, 0.25, 5);

		// invoke method
		long time = 100;
		for (long key : KEYS) {
			updateCollectionForKey(d, key, time);
			time += 5;
		}
		
		// check results
		IndexedPriorityQueue pq = d.getPQ();
		assertEquals(4, pq.size());
		assertNotNull(pq.get(4004));
		assertNotNull(pq.get(5005));
		assertNotNull(pq.get(1001));
		assertNotNull(pq.get(2002));
	}

	@SuppressWarnings("unchecked")
	public void testUpdate3() {
		DecayVariableCollection<String> d = makeDecayVariableCollection("test3", 3, 2.0, 0.1, 2);

		// invoke method
		long time = 100;
		for (long key : KEYS) {
			updateCollectionForKey(d, key, time);
			time += 1;
		}
		
		// check results
		IndexedPriorityQueue pq = d.getPQ();
		assertEquals(3, pq.size());
		assertNotNull(pq.get(5005));
		assertNotNull(pq.get(2002));
		assertNotNull(pq.get(1001));
	}

	// ======================================================= private helpers

	private <T> DecayVariableCollection<T> makeDecayVariableCollection(
			String collectionType, int maxSize, double energyUnit,
			double killThreshold, int halfLife) {
		DecayVariableCollection<T> d = new DecayVariableCollection<T>(
				collectionType, maxSize, energyUnit, killThreshold, halfLife);
		if (log.isDebugEnabled())
			log.debug(d);
		return d;
	}

	private <T> void updateCollectionForKey(DecayVariableCollection<T> d,
			long key, long time) {
		String oldKT = d.toStringKillTime(key);
		double newValue = d.update(key, time).getPriority();
		String newKT = d.toStringKillTime(key);
		if (log.isDebugEnabled()) {
			log.debug("@time = " + time + ". " + key + "'s killtime moved " + oldKT + " to "
					+ newKT + ". new value = " + newValue);
			log.debug(d.toStringPriorityQueue());
		}
	}
}