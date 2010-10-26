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
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;

import com.fraudwall.util.IndexedPriorityQueue.Entry;

/**
 * Tests of the {@link IndexedPriorityQueue} implementation.
 *
 * @author Allan Heydon
 */
public class IndexedPriorityQueueTest extends TestCase {

	private static final int N = 5000;

	// ============================================================ <constructor>

	public void testConstructorCreatesEmptyQueue() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(3);
		assertEquals(0, pq.size());
	}

	// ============================================================ isFull

	public void testIsFullReturnsFalseIfSpaceExistsInTheQueue() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(3);
		assertFalse(pq.isFull());
		pq.add(Entry.create(1, "foo", 6));
		assertFalse(pq.isFull());
		pq.add(Entry.create(2, "bar", 3));
		assertFalse(pq.isFull());
	}

	public void testIsFullReturnsTrueIfQueueContainsTheMaxNumberOfEntries() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(3);
		pq.add(Entry.create(1, "foo", 6));
		pq.add(Entry.create(2, "bar", 3));
		pq.add(Entry.create(3, "baz", 5));
		assertTrue(pq.isFull());
	}

	public void testIsFullReturnsFalseAfterElementPoppedFromFullQueue() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(2);
		pq.add(Entry.create(1, "foo", 6));
		pq.add(Entry.create(2, "bar", 3));
		assertTrue(pq.isFull());
		pq.pop();
		assertFalse(pq.isFull());
	}

	public void testIsFullReturnsTrueAfterElementIsReaddedToFillQueue() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(2);
		pq.add(Entry.create(1, "foo", 6));
		pq.add(Entry.create(2, "bar", 3));
		pq.pop();
		assertFalse(pq.isFull());
		pq.add(Entry.create(3, "baz", 5));
		assertTrue(pq.isFull());
	}

	// ============================================================ toArray

	public void testToArrayReturnsContentsOfQueue() {
		IndexedPriorityQueue<String> pq = makePopulatedPQ();
		Entry<String>[] entries = pq.toArray();
		assertEquals(pq.size(), entries.length);
		for (int i = 1; i <= 4; i++) checkContainsKey(entries, i);
	}

	private void checkContainsKey(Entry<String>[] entries, long key) {
		for (Entry<String> entry : entries) {
			if (entry.getKey() == key) return;
		}
		fail("No entry found for key " + key);
	}

	// ============================================================ peek

	public void testPeekReturnsNullForEmptyQueue() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(5);
		assertNull(pq.peek());
	}

	public void testPeekReturnsElementWithSmallestPriority() {
		IndexedPriorityQueue<String> pq = makePopulatedPQ();
		Entry<String> result = pq.peek();
		assertEquals(2, result.getKey());
		assertEquals("bar", result.getValue());
		assertEquals(3, result.getPriority());
	}

	public void testPeekLeavesElementInTheQueue() {
		IndexedPriorityQueue<String> pq = makePopulatedPQ();
		assertEquals(4, pq.size());
		pq.peek();
		assertEquals(4, pq.size());
		assertNotNull(pq.get(3));
	}

	// ============================================================ pop

	public void testPopRemovesPoppedElementFromQueue() {
		IndexedPriorityQueue<String> pq = makePopulatedPQ();
		Entry<String> poppedElement = pq.pop();
		assertFalse(pq.contains(poppedElement));
		assertNull(pq.get(poppedElement.getKey()));
	}

	public void testPopDecrementsQueueSize() {
		IndexedPriorityQueue<String> pq = makePopulatedPQ();
		int startSize = pq.size();
		pq.pop();
		assertEquals(startSize - 1, pq.size());
	}

	public void testPopThrowsNoSuchElementExceptionIfQueueIsEmpty() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(10);
		try {
			pq.pop();
			fail();
		} catch (NoSuchElementException ex) {
			// expected case
		}
	}

	public void testPopRemovesPoppedElementsInIncreasingPriority() {
		final int SIZE = 5000;
		IndexedPriorityQueue<String> pq = makePQWithPermutedPriorities(SIZE);

		// check that pop() removes elements in increasing priority order
		for (int i = 0; i < SIZE; i++) {
			Entry<String> poppedItem = pq.pop();
			assertEquals(i, poppedItem.getPriority());
		}
	}

	// ============================================================ get

	public void testGetReturnsNullIfNoEntryInQueueWithGivenKey() {
		IndexedPriorityQueue<String> pq = makePopulatedPQ();
		assertNull(pq.get(9999));
	}

	public void testGetReturnsEntryWithGivenKeyInQueue() {
		final int SIZE = 1000;
		IndexedPriorityQueue<String> pq = makePQWithPermutedPriorities(SIZE);
		while (pq.size() > 0) {
			Entry<String> minElt = pq.peek();
			assertEquals(minElt, pq.get(minElt.getKey()));
			Entry<String> maxElt = pq.get(SIZE - 1);
			assertNotNull(maxElt);
			assertEquals(SIZE - 1, maxElt.getKey());
			pq.pop();
		}
	}

	// ============================================================ priorityHasChanged

	public void testPriorityHasChangedReorganizesHeapCorrectly() {
		// build random PQ and change all priorities
		final int SIZE = 1000;
		IndexedPriorityQueue<String> pq = makePQWithPermutedPriorities(SIZE);
		for (long i = 0; i < SIZE; i++) {
			Entry<String> minElt = pq.peek();
			assertEquals(i, minElt.getKey());
			assertEquals(i, minElt.getPriority());
			minElt.setPriority((2 * SIZE) - i);
			pq.priorityHasChanged(minElt);
		}

		// check that pop removes elements in order
		for (long i = 0; i < SIZE; i++) {
			Entry<String> minElt = pq.pop();
			assertEquals(SIZE - i - 1, minElt.getKey());
			assertEquals(SIZE + i + 1, minElt.getPriority());
		}
	}

	// ============================================================ add

	public void testAddAddsEntry() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(1);
		pq.add(Entry.create(5, "foo"));
		assertTrue(pq.contains(Entry.create(5, "bar")));
		assertNotNull(pq.get(5));
	}

	public void testAddIncrementsQueueSize() {
		IndexedPriorityQueue<String> pq = makePopulatedPQ();
		int startSize = pq.size();
		pq.add(Entry.create(9999, "foo"));
		assertEquals(startSize + 1, pq.size());
	}

	public void testAddThrowsArrayIndexOutOfBoundsExceptionIfSizeLimitIsHit() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(1);
		pq.add(Entry.create(1, "foo"));
		try {
			pq.add(Entry.create(2, "bar"));
			fail();
		} catch (ArrayIndexOutOfBoundsException ex) {
			// expected case
		}
	}

	public void testAddThrowsIllegalArgumentExceptionIfEntryWithSameKeyAlreadyExists() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(5);
		pq.add(Entry.create(5, "foo"));
		try {
			pq.add(Entry.create(5, "bar"));
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
		}
	}

	public void testAddReturnsTrueIfItThrowsNoException() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(5);
		assertTrue(pq.add(Entry.create(5, "foo")));
	}

	// ============================================================ clear

	public void testClearRemovesAllEntriesFromQueue() {
		IndexedPriorityQueue<String> pq = makePopulatedPQ();
		assertTrue(pq.size() > 0);
		assertNotNull(pq.get(1));
		pq.clear();
		assertEquals(0, pq.size());
		assertNull(pq.get(1));
	}

	// ============================================================ contains

	public void testContainsReturnsFalseForEmptyQueue() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(3);
		assertFalse(pq.contains(Entry.create(0, "foo")));
	}

	public void testContainsReturnsFalseForElementNotInQueue() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(3);
		pq.add(Entry.create(0, "foo"));
		assertFalse(pq.contains(Entry.create(1, "bar")));
	}

	public void testContainsReturnsTrueForElementInQueue() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(3);
		pq.add(Entry.create(0, "foo"));
		assertTrue(pq.contains(Entry.create(0, "bar")));
	}

	// ============================================================ iterator

	public void testIteratorReturnsAnIteratorOnAllQueueElements() {
		IndexedPriorityQueue<String> pq = makePQWithPermutedPriorities(N);
		for (int i = 0; i < N / 2; i++) pq.pop();

		// iterate over remaining elements, building a Set of the keys
		Set<Long> keys = new HashSet<Long>(N / 2);
		for (Entry<String> entry : pq) {
			keys.add(entry.getKey());
		}

		// verify that the correct keys were found
		for (long i = N / 2; i < N; i++) {
			assertTrue(keys.contains(i));
		}
	}

	// ============================================================ ordering

	public void testIndexedPriorityQueueBreaksPriorityTiesUsingKey() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(N);
		for (long key : permuteLongs(N)) {
			// create all entries with the same priority (1L)
			pq.add(Entry.create(key, Long.toString(key), 1L));
		}
		assertEquals(N, pq.size());
		for (long key = 0; !pq.isEmpty(); key++) {
			Entry<String> entry = pq.pop();
			assertEquals(key, entry.getKey());
		}
	}

	// ============================================================ helper methods

	private IndexedPriorityQueue<String> makePopulatedPQ() {
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(10);
		pq.add(Entry.create(1, "foo", 6));
		pq.add(Entry.create(2, "bar", 3));
		pq.add(Entry.create(3, "baz", 5));
		pq.add(Entry.create(4, "faz", 7));
		return pq;
	}

	private IndexedPriorityQueue<String> makePQWithPermutedPriorities(final int size) {
		// add elements to new queue in permuted priority order
		IndexedPriorityQueue<String> pq = IndexedPriorityQueue.create(size);
		for (long priority : permuteLongs(size)) {
			// use priority to form key and value, too
			pq.add(Entry.create(priority, Long.toString(priority), priority));
		}
		assertEquals(size, pq.size());
		return pq;
	}

	private List<Long> permuteLongs(final int size) {
		List<Long> priorities = new ArrayList<Long>(size);
		for (long i = 0; i < size; i++) priorities.add(i);
		Collections.shuffle(priorities);
		return priorities;
	}
}
