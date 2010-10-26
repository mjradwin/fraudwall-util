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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A bounded cache mapping keys to values that evicts the key/value mapping
 * in the cache that was least recently added or accessed.<p>
 *
 * This class is not thread safe. It is the client's responsibility
 * to guarantee single-threaded access.<p>
 *
 * This class is similar to Apache's {@link org.apache.commons.collections.map.LRUMap},
 * but it is generic, so clients can get extra type safety by using the Anchor
 * Intelligence version.
 *
 * @param <K> The type of keys stored in the map.
 * @param <V> The type of values stored in the map.
 * @author Allan Heydon
 */
public class LRUMap<K,V> extends AbstractMap<K,V> implements FWObjectCache<K,V> {
	/** Upper bound on the number of elements in the cache. */
	private final int maxSize;

	/** Sentinel representing both the start and end of the list. */
	private final LL<K,V> sentinel;

	/** Maps a key to its node in the linked list. */
	private final Map<K,LL<K,V>> map;

	/**
	 * Creates a new LRU cache that holds at most <code>maxSize</code>
	 * elements.
	 */
	public static <K,V> LRUMap<K,V> create(int maxSize) {
		return new LRUMap<K,V>(maxSize);
	}

	public LRUMap(int maxSize) {
		this.maxSize = maxSize;
		map = new HashMap<K,LL<K,V>>(maxSize);
		sentinel = new LL<K,V>(null, null, null, null);
		sentinel.prev = sentinel;
		sentinel.next = sentinel;
	}

	/**
	 * Returns the number of elements in the cache, which will
	 * be between 0 and the <code>maxSize</code> value passed
	 * to this instance's constructor (inclusive).
	 */
	@Override
	public int size() {
		return map.size();
	}

	/**
	 * Returns true if and only if the given <code>key</code>
	 * is in the set. This method has no effect on protecting
	 * the accessed key/value pair from eviction.
	 */
	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/**
	 * Returns the value associated with the given <code>key</code>,
	 * or <code>null</code> if the cache does not contain the key.
	 * If the key is present, this method also has the side-effect
	 * of temporarily protecting the given value from eviction.
	 */
	@Override
	public V get(Object key) {
		LL<K,V> node = map.get(key);
		if (node != null) {
			moveToFront(node);
			return node.val;
		}
		return null;
	}

	/**
	 * Adds a mapping from the given <code>key</code> to the given
	 * <code>value</code> to this cache, possibly evicting some other
	 * element (if the cache is full and does not already contain a
	 * mapping for the given key).<p>
	 *
	 * If the map already contained an entry for the given <code>key</code>,
	 * the value to which it is mapped is changed to <code>value</code>. Whether
	 * a mapping for the key pre-existed or not, the key/value mapping is
	 * temporarily protected against eviction from the cache.
	 *
	 * @return The value to which the given <code>key</code> was
	 * mapped prior to this call, or <code>null</code> if the cache
	 * did not previously contain a mapping for the key.
	 */
	@Override
	public V put(K key, V value) {
		LL<K,V> node = map.get(key);
		if (node == null) {
			if (map.size() == maxSize) {
				// remove last element from list and map
				LL<K,V> removed = removeLast();
				map.remove(removed.key);
			}
			node = new LL<K,V>(key, value, null, null);
			map.put(key, node);
			addToFront(node);
			return null;
		} else {
			V res = node.val;
			node.val = value;
			moveToFront(node);
			return res;
		}
	}

	/**
	 * Removes all mappings from this cache.
	 */
	@Override
	public void clear() {
		map.clear();
		sentinel.prev = sentinel;
		sentinel.next = sentinel;
	}

	// ====== list manipulation helpers =======

	/**
	 * Removes the given <code>value</code> from this cache if
	 * it is present; if not present, this method is a no-op.
	 *
	 * @return True if and only if the value was present and
	 * was therefore removed (i.e., if the cache was modified).
	 */
	@Override
	public V remove(Object key) {
		LL<K,V> node = map.remove(key);
		if (node != null) {
			remove(node);
			return node.val;
		}
		return null;
	}

	private LL<K,V> remove(LL<K,V> node) {
		node.next.prev = node.prev;
		node.prev.next = node.next;
		node.prev = null;
		node.next = null;
		return node;
	}

	private void addToFront(LL<K,V> node) {
		node.next = sentinel.next;
		node.next.prev = node;
		node.prev = sentinel;
		sentinel.next = node;
	}

	private LL<K,V> removeLast() {
		assert sentinel.prev != sentinel;
		return remove(sentinel.prev);
	}

	private void moveToFront(LL<K,V> node) {
		remove(node);
		addToFront(node);
	}

	// ====== doubly-linked list node ======

	private static class LL<K,V> {
		public K key;
		public V val;
		public LL<K,V> prev, next;

		public LL(K key, V val, LL<K,V> prev, LL<K,V> next) {
			this.key = key;
			this.val = val;
			this.prev = prev;
			this.next = next;
		}
	}

	/**
	 * Returns a {@link Set} view of the mappings contained in this map.
	 *
	 * @return a set view of the mappings contained in this map
	 */
	@Override
	public Set<Map.Entry<K,V>> entrySet() {
		Set<Map.Entry<K,V>> set = new HashSet<Map.Entry<K,V>>(size());
		for (Map.Entry<K,LL<K,V>> entry : map.entrySet()) {
			set.add(new SimpleImmutableEntry<K,V>(entry.getKey(), entry.getValue().val));
		}
		return set;
	}

	static class SimpleImmutableEntry<K,V> implements Map.Entry<K,V> {
		final K key;
		final V value;

		public SimpleImmutableEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object o) {
			SimpleImmutableEntry<K,V> e2 = (SimpleImmutableEntry<K,V>)o;
			return (getKey()==null ?
					e2.getKey()==null :
					getKey().equals(e2.getKey()))
				&&
				(getValue()==null ?
				e2.getValue()==null :
				getValue().equals(e2.getValue()));
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			return (getKey()==null ? 0 : getKey().hashCode()) ^
				(getValue()==null ? 0 : getValue().hashCode());
		}

		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}
	}

	/**
	 * There is no need to flush anything to disk, so this is a no-op.
	 */
	public void finish() {
		// no-op
	}
}
