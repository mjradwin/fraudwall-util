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

import java.util.HashMap;
import java.util.Map;

/**
 * A bounded cache that evicts the element in the cache that was
 * least recently added or accessed.<p>
 *
 * This class is not thread safe. It is the client's responsibility
 * to guarantee single-threaded access.<p>
 *
 * This class is similar to Apache's {@link org.apache.commons.collections.map.LRUMap},
 * but it is generic, so clients can get extra type safety by using the Anchor
 * Intelligence version.
 *
 * @param <T> The type of elements stored in the set.
 * @author Allan Heydon
 */
public class LRUSet<T> {
	/** Upper bound on the number of elements in the cache. */
	private final int maxSize;

	/** Sentinel representing both the start and end of the list. */
	private final LL<T> sentinel;

	/** Maps a set value to its node in the linked list. */
	private final Map<T,LL<T>> map;

	/**
	 * Creates a new LRU cache that holds at most <code>maxSize</code>
	 * elements.
	 */
	public static <T> LRUSet<T> create(int maxSize) {
		return new LRUSet<T>(maxSize);
	}

	private LRUSet(int maxSize) {
		this.maxSize = maxSize;
		map = new HashMap<T,LL<T>>(maxSize);
		sentinel = new LL<T>(null, null, null);
		sentinel.prev = sentinel;
		sentinel.next = sentinel;
	}

	/**
	 * Returns the number of elements in the cache, which will
	 * be between 0 and the <code>maxSize</code> value passed
	 * to this instance's constructor (inclusive).
	 */
	public int size() {
		return map.size();
	}

	/**
	 * Returns true if and only if the given <code>value</code>
	 * is in the set. If the element is present, this method also
	 * has the side-effect of temporarily protecting the given
	 * value from eviction.
	 */
	public boolean contains(T value) {
		LL<T> node = map.get(value);
		if (node != null) {
			moveToFront(node);
			return true;
		}
		return false;
	}

	/**
	 * Adds the given <code>value</code> to this cache, possibly
	 * evicting some other element (if the cache is full). If the
	 * element is already in the cache, this method is a complete
	 * no-op; it does not count as an "access" of the element.
	 *
	 * @return True if and only if the value was not already
	 * in the cache (i.e., if the cache was modified).
	 */
	public boolean add(T value) {
		LL<T> node = map.get(value);
		if (node == null) {
			if (map.size() == maxSize) {
				// remove last element from list and map
				LL<T> removed = removeLast();
				map.remove(removed.val);
			}
			node = new LL<T>(value, null, null);
			map.put(value, node);
			addToFront(node);
			return true;
		}
		return false;
	}

	/**
	 * Removes the given <code>value</code> from this cache if
	 * it is present; if not present, this method is a no-op.
	 *
	 * @return True if and only if the value was present and
	 * was therefore removed (i.e., if the cache was modified).
	 */
	public boolean remove(T value) {
		LL<T> node = map.remove(value);
		if (node != null) {
			remove(node);
			return true;
		}
		return false;
	}

	/**
	 * Removes all mappings from this cache.
	 */
	public void clear() {
		map.clear();
		sentinel.prev = sentinel;
		sentinel.next = sentinel;
	}

	// ====== list manipulation helpers =======

	private LL<T> remove(LL<T> node) {
		node.next.prev = node.prev;
		node.prev.next = node.next;
		node.prev = null;
		node.next = null;
		return node;
	}

	private void addToFront(LL<T> node) {
		node.next = sentinel.next;
		node.next.prev = node;
		node.prev = sentinel;
		sentinel.next = node;
	}

	private LL<T> removeLast() {
		assert sentinel.prev != sentinel;
		return remove(sentinel.prev);
	}

	private void moveToFront(LL<T> node) {
		remove(node);
		addToFront(node);
	}

	// ====== doubly-linked list node ======

	private static class LL<E> {
		public E val;
		public LL<E> prev, next;

		public LL(E val, LL<E> prev, LL<E> next) {
			this.val = val;
			this.prev = prev;
			this.next = next;
		}
	}
}
