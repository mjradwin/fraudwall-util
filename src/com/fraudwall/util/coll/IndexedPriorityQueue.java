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

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.mutable.MutableInt;

import com.fraudwall.util.exc.ArgCheck;

/**
 * A "minimum-oriented" bounded priority queue with an index that supports fast
 * lookups. Each element is a <key, value> pair, together with a priority. The
 * keys and priorities are of type <code>long</code>, and the values are of
 * type <code>V</code>. As in a {@link Map}, the keys must be unique. This
 * class supports two operations that the Java collections' priority queue
 * doesn't: {@link #get} and {@link #priorityHasChanged}. The <code>get</code>
 * method is a constant time operation, and the <code>priorityHasChanged</code>
 * method is O(log N). As a "minimum-oriented" priority queue, the root is the
 * element with the smallest priority. If two elements have the same smallest
 * priority, ties are broken by taking the element with the smallest key value.
 * <p>
 * When a new IndexedPriorityQueue is constructed, an integer upper bound on its
 * size must be passed to the constructor. Once the queue becomes full, it is an
 * error to call the {@link #add} method; the {@link #pop} method must be called
 * first to make room for the new item so the overall size of the queue does not
 * grow beyond the bound.
 * <p>
 * The class is designed to place a minimum load on the GC: The objects in the
 * queue are of type Entry containing a key (an object) and a primitive (e.g., a
 * long). When changing the priority of an {@link Entry}, don't allocate a new
 * one; rather, just change the value field and inform the class that the value
 * has changed by calling {@link #priorityHasChanged}. When replacing an item
 * in the priority queue, the caller can safely reuse the Entry returned by the
 * call to {@link #pop()}. Also, internally, a hash table is kept to index
 * where a key is stored in the priority queue; the location changes frequently
 * as the priority queue is rearranged, but the key doesn't. Rather than using
 * the immutable Integer container for the value of the index, we define a
 * mutable integer class.
 */
public class IndexedPriorityQueue<V> extends AbstractCollection<IndexedPriorityQueue.Entry<V>> {

	/* Items in priority queue, arranged as a heap. element 0 is not used. */
	private final Entry<V>[] pq; //

	private int N; // number of elements in the priority queue

	/* Index of element K in the queue. */
	private final Map<Long, MutableInt> map;

	private final int maxSize; // max size of the priority queue

	private MutableInt spare; // no need to allocate if there's a spare...

	/**
	 * Constructs a new, empty IndexedPriorityQueue that can hold at most
	 * <code>maxSize</code> elements.
	 *
	 * @param maxSize
	 *            The maximum number of items that can be stored in this
	 *            priority queue. If a new item is inserted, the item in the
	 *            queue with the smallest value is removed to make room for the
	 *            new item.
	 */
	public static <V> IndexedPriorityQueue<V> create(int maxSize) {
		return new IndexedPriorityQueue<V>(maxSize);
	}

	@SuppressWarnings("unchecked")
	protected IndexedPriorityQueue(int maxSize) {
		ArgCheck.isTrue(maxSize >= 1, "maxSize must be positive");
		this.N = 0;
		this.maxSize = maxSize;
		this.pq = new Entry[maxSize + 1];
		this.map = new HashMap<Long, MutableInt>(((maxSize * 4) / 3) + 1);
		this.spare = null;
	}

	/**
	 * Returns true if and only if this priority queue cannot hold
	 * any more elements. Before another call of the {@link #add}
	 * method can be called successfully, you'll have to first
	 * call the {@link #pop} method at least once.
	 */
	public boolean isFull() {
		return (N == maxSize);
	}

	/**
	 * Returns the number of elements in the priority queue.
	 */
	@Override
	public int size() {
		return N;
	}

	/**
	 * Returns the number of elements in the priority queue.
	 */
	public int maxSize() {
		return maxSize;
	}

	/**
	 * Returns the elements of the priority queue as an array in an unspecified order.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Entry<V>[] toArray() {
		Entry<V>[] a = new Entry[N];
		System.arraycopy(pq, 1, a, 0, N);
		return a;
	}

	/**
	 * Returns the root of the priority queue, that is, the Entry with the
	 * smallest value; returns <code>null</code> if the priority queue is
	 * empty.
	 *
	 * @see #pop
	 */
	public Entry<V> peek() {
		return (N > 0) ? pq[1] : null;
	}

	/**
	 * Removes and returns the root of the priority queue, that is, the Entry
	 * with the smallest value.
	 *
	 * @throws NoSuchElementException if the priority queue is empty.
	 * @see #peek
	 */
	public Entry<V> pop() {
		if (N < 1)
			throw new NoSuchElementException();
		exchange(1, N);
		sink(1, N - 1);
		Entry<V> item = pq[N];
		pq[N--] = null;
		spare = map.remove(item.getKey());
		return item;
	}

	/**
	 * Returns the Entry in the priority queue with the given key, or
	 * <code>null</code> it no such Entry exists.
	 */
	public Entry<V> get(long key) {
		MutableInt m = map.get(key);
		return (m != null) ? pq[m.intValue()] : null;
	}

	/**
	 * Must be called when the value of an item has been changed to restore the
	 * priority queue invariant.
	 */
	public void priorityHasChanged(Entry<V> item) {
		MutableInt m = map.get(item.getKey());
		swim(m.intValue());
		sink(m.intValue(), N);
	}

	/**
	 * Bottom-up heapify: walk up the heap as needed
	 */
	private void swim(int k) {
		while (k > 1 && more(k / 2, k)) {
			exchange(k, k / 2);
			k = k / 2;
		}
	}

	/**
	 * Top-down heapify: walk down the heap as far as needed
	 */
	private void sink(int k, int n) {
		while (2 * k <= n) {
			int j = 2 * k;
			if (j < n && more(j, j + 1))
				j++;
			if (!more(k, j))
				break;
			exchange(k, j);
			k = j;
		}
	}

	private boolean more(int i, int j) {
		return pq[i].compareTo(pq[j]) > 0;
	}

	private void exchange(int i, int j) {
		Entry<V> t = pq[i];
		pq[i] = pq[j];
		pq[j] = t;
		MutableInt m;
		m = map.get(pq[i].getKey());
		m.setValue(i);
		m = map.get(pq[j].getKey());
		m.setValue(j);
	}

	/**
	 * Adds the specified <code>entry</code> into this priority queue. It is
	 * an error to call this method if the queue is full or if an entry already
	 * exists in the queue with the same key as <code>entry</code>'s key.
	 *
	 * @throws ArrayIndexOutOfBoundsException
	 *             if this priority queue is full
	 * @throws IllegalArgumentException
	 *             if this priority queue already contains an entry with the
	 *             same key as <code>entry</code>'s key.
	 * @return true (indicating that the priority queue was modified)
	 *
	 * @see #isFull
	 * @see #pop
	 */
	@Override
	public boolean add(Entry<V> entry) {
		if (isFull()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (map.containsKey(entry.getKey())) {
			throw new IllegalArgumentException("Duplicate key");
		}
		MutableInt m = (spare != null) ? spare : new MutableInt();
		spare = null;
		pq[++N] = entry;
		m.setValue(N);
		map.put(entry.getKey(), m);
		swim(N);
		return true;
	}

	@Override
	public void clear() {
		for (int i = 1; i <= N; i++) {
			pq[i] = null;
		}
		N = 0;
		map.clear();
	}

	/**
	 * Returns true if and only if this priority queue contains an {@link Entry}
	 * whose key matches that of the supplied Object <code>o</code>, which is
	 * expected to be of type <code>Entry&lt;V&gt;</code>.
	 *
	 * @see #get(long)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean contains(Object o) {
		Entry<V> e = (Entry<V>) o;
		return map.containsKey(e.getKey());
	}

	@Override
	public Iterator<Entry<V>> iterator() {
		return new Iterator<Entry<V>>() {
			int index = -1;
			int count = 0;

			public boolean hasNext() {
				return count < N;
			}

			public Entry<V> next() {
				while (pq[++index] == null)
					;
				++count;
				return pq[index];
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * An item in the priority queue. Implemented as an abstract class, rather
	 * than parameterizing based on the type of the value, so that the value can
	 * be a Java primitive rather than an object.
	 */
	public static class Entry<V> implements Comparable<Entry<V>> {

		private final long key;
		private V value;
		private long priority;

		public static <V> Entry<V> create(long key, V value) {
			return create(key, value, -1);
		}

		public static <V> Entry<V> create(long key, V value, long priority) {
			return new Entry<V>(key, value, priority);
		}

		private Entry(long key, V value, long priority) {
			this.key = key;
			this.value = value;
			this.priority = priority;
		}

		public long getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}

		public long getPriority() {
			return priority;
		}

		public void setPriority(long priority) {
			this.priority = priority;
		}

		public int compareTo(Entry<V> other) {
			int res = compareLongs(this.priority, other.priority);
			if (res == 0) {
				res = compareLongs(this.key, other.key);
			}
			return res;
		}

		private int compareLongs(long l1, long l2) {
			return (l1 == l2) ? 0 : ((l1 < l2) ? -1 : 1);
		}
	}
}
