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
package com.fraudwall.util.coll;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;


public class ExpiringLRUMap<K,V> extends AbstractMap<K,V> implements ObjectCache<K,V> {
	private static class Wrapper<V> {
		public final long ts;
		public final V val;
		public Wrapper(V val) {
			this.val = val;
			ts = System.currentTimeMillis();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object other) {
			return val.equals(((Wrapper<V>)other).val);
		}

		@Override
		public int hashCode() {
			return val.hashCode();
		}
	}

	private final LRUMap<K,Wrapper<V>> map;
	private final long expireMillis;

	public static <K,V> ExpiringLRUMap<K,V> create(int maxSize) {
		return create(maxSize, DateUtils.MILLIS_PER_DAY);
	}

	public static <K,V> ExpiringLRUMap<K,V> create(int maxSize, long expireMillis) {
		return new ExpiringLRUMap<K,V>(maxSize, expireMillis);
	}

	private ExpiringLRUMap(int maxSize, long expireMillis) {
		map = LRUMap.create(maxSize);
		this.expireMillis = expireMillis;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override
	public V get(Object key) {
		return valueIfNotExpired(map.get(key));
	}

	private boolean isValid(Wrapper<V> value) {
		long now = System.currentTimeMillis();
		return (now - value.ts) < expireMillis;
	}

	private V valueIfNotExpired(Wrapper<V> value) {
		if (value != null) {
			if (isValid(value)) {
				return value.val;
			}
		}
		return null;
	}

	@Override
	public V put(K key, V value) {
		return valueIfNotExpired(map.put(key, new Wrapper<V>(value)));
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public V remove(Object key) {
		return valueIfNotExpired(map.remove(key));
	}

	@Override
	public Set<Map.Entry<K,V>> entrySet() {
		Set<Map.Entry<K,V>> set = new HashSet<Map.Entry<K,V>>(size());
		for (Map.Entry<K,Wrapper<V>> entry : map.entrySet()) {
			Wrapper<V> value = entry.getValue();
			if (isValid(value)) {
				set.add(new LRUMap.SimpleImmutableEntry<K,V>(entry.getKey(), value.val));
			}
		}
		return set;
	}

	@Override
	public Set<K> keySet() {
		Set<K> set = new HashSet<K>(size());
		for (Map.Entry<K,Wrapper<V>> entry : map.entrySet()) {
			Wrapper<V> value = entry.getValue();
			if (isValid(value)) {
				set.add(entry.getKey());
			}
		}
		return set;
	}

	/**
	 * There is no need to flush anything to disk, so this is a no-op.
	 */
	public void finish() {
		// no-op
	}
}
