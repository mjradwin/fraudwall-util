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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Tests the {@link LRUSet} implementation.
 *
 * @author Allan Heydon
 */
public class LRUSetTest extends AbstractAnchorTest {

	// --------------------------------- contains

	public void testContainsReturnsFalseForElementNotInCache() {
		LRUSet<Integer> cache = LRUSet.create(3);
		assertFalse(cache.contains(0));
		cache.add(1);
		assertFalse(cache.contains(0));
		cache.add(2);
		assertFalse(cache.contains(0));
	}

	public void testContainsReturnsTrueForElementInCache() {
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		assertTrue(cache.contains(1));
		assertTrue(cache.contains(2));
	}

	public void testContainsReturnsFalseForElementRemovedFromCache() {
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		cache.remove(1);
		assertFalse(cache.contains(1));
	}

	public void testContainsReturnsFalseForElementEvictedFromCache() {
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		cache.add(3);
		cache.add(4);
		assertFalse(cache.contains(1));
	}

	public void testContainsCausesElementToBeProtectedFromEviction() {
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		cache.add(3);
		assertTrue(cache.contains(1));
		cache.add(4);
		assertTrue(cache.contains(1));
		assertFalse(cache.contains(2));
	}

	// --------------------------------- add

	public void testAddAddsElementToSet() {
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		assertTrue(cache.contains(1));
	}

	public void testAddReturnsCorrectResult() {
		LRUSet<Integer> cache = LRUSet.create(3);
		assertTrue(cache.add(1));
		assertTrue(cache.add(2));
		assertFalse(cache.add(1));
	}

	public void testAddAddsElementAtFrontOfLruQueue() {
		// fill cache
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		cache.add(3);

		// add new element at front of queue
		cache.add(4);

		// move element 4 back in queue
		cache.contains(2);
		cache.contains(3);

		// check that element 4 has not been evicted
		assertTrue(cache.contains(4));
	}

	public void testAddOfExistingElementReturnsFalse() {
		LRUSet<Integer> cache = LRUSet.create(3);
		assertTrue(cache.add(1));
		assertFalse(cache.add(1));
	}

	public void testAddOfExistingElementDoesNotChangeItsOrder() {
		// fill cache
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		cache.add(3);

		// add element 1 again (the element pending eviction)
		cache.add(1);

		// add new element at front of queue
		cache.add(4);

		// check that element 1 was evicted
		assertFalse(cache.contains(1));
	}

	// --------------------------------- remove

	public void testRemoveRemovesElementFromSet() {
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.remove(1);
		assertFalse(cache.contains(1));
	}

	public void testRemoveReturnsCorrectResult() {
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		assertTrue(cache.remove(1));
		assertFalse(cache.remove(1));
		assertFalse(cache.remove(3));
		assertTrue(cache.remove(2));
	}

	public void testRemoveMakesRoomForAdditionalElement() {
		// fill cache
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		cache.add(3);

		// remove an element
		cache.remove(1);

		// add a new element
		cache.add(4);

		// check that neither of the other 2 was evicted
		assertTrue(cache.contains(2));
		assertTrue(cache.contains(3));
	}

	// --------------------------------- clear

	public void testClearDoesNothingWhenCacheIsEmpty() {
		LRUSet<Integer> cache = LRUSet.create(1);
		cache.clear();
		assertEquals(0, cache.size());
	}

	public void testClearRemovesAllElementsFromCache() {
		// fill cache
		LRUSet<Integer> cache = LRUSet.create(3);
		cache.add(1);
		cache.add(2);
		cache.add(3);

		// clear the cache
		cache.clear();

		// add a new element
		cache.add(4);

		// check that the first three elements are no longer in the cache
		assertFalse(cache.contains(1));
		assertFalse(cache.contains(2));
		assertFalse(cache.contains(3));
		// check that the new element is in the cache
		assertTrue(cache.contains(4));
		assertEquals(1, cache.size());

	}

	// --------------------------------- load test

	public void testLruCacheUnderLoad() {
		final int W = 1000; // working set size
		final int N = 10000; // total # of elements
		final List<Integer> workingSet = new ArrayList<Integer>(W);

		// fill cache with working set elements
		LRUSet<Integer> cache = LRUSet.create(W + 1);
		for (int i = 0; i < W; i++) {
			assertTrue(cache.add(i));
			workingSet.add(i);
		}

		// loop over all remaining elements
		for (int i = W; i < N; i++) {
			// access all working set elts in random order
			Collections.shuffle(workingSet);
			for (Integer wsElt: workingSet) {
				assertTrue(cache.contains(wsElt));
			}

			// add element i
			assertTrue(cache.add(i));

			// check that element i-1 was evicted
			if (i > W) {
				assertFalse(cache.contains(i-1));
			}
		}
	}
}
