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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.IndexedPriorityQueue.Entry;

/**
 * A DecayVariable maintains a set of key-value pairs. The value associated with
 * a key is controlled internally by this class: the value is initialized to one
 * ENERGYUNIT and decreases automatically over time (hence, its value "decays"
 * over time); at any time, the value can be "re-energized", by adding to it an
 * ENERGYUNIT. The rate at which the value decays is given by a half-life
 * constant, HALFLIFE. That is, in HALFLIFE milliseconds, a key's value will halve. A
 * value of energy, called the KILLTHRESHOLD, is a value below which the key is
 * considered by of no interest; the time at which this happens is called the
 * "killtime".
 *
 * This class is constrained to keep at most M entries. Each time a new key is
 * added, the key with smallest killtime is replaced if either M entries are
 * already being stored or if the killtime has already expired.
 *
 * Given the current time, we can compute the energy level from a known killTime,
 * and a killTime from a known energy level, using the following formulae:
 *
 *   energy = KILLTHRESHOLD * 2^((killTime-now)/HALFLIFE)
 *
 *   killtime = now + HALFLIFE * lg(energy/KILLTHRESHOLD)
 *
 * @author marc
 * @author kfox
 */
public class DecayVariableCollection<V> {

	private Log log = LogFactory.getLog(DecayVariableCollection.class);

	private static final double LOG2 = Math.log(2.0);

	/**
	 * Type of collection (for log files)
	 */
	private final String collectionType;

	/**
	 * Number of key-value pairs to support
	 */
	private final int maxSize;

	/**
	 * ENERGYUNIT - The amount of energy to give to a key initially, and on each
	 * subsequent update.
	 */
	private final double energyUnit;

	/**
	 * KILLTHRESHOLD - The level of energy at which point a key can be
	 * considered no longer of interest.
	 */
	private final double killThreshold;

	/**
	 * HALFLINE- time (in milliseconds) required for value to decay to half its
	 * initial value
	 */
	private final long halfLife;

	/**
	 * Priority Queue in order of each one's killTime
	 */
	private final IndexedPriorityQueue<V> pq;

	/**
	 * The number of collection overflow warnings that have occurred
	 */
	private int numOverflowWarnings = 0;

	/**
	 * Next number of overflow warnings that we should warn on
	 */
	private int nextNumOverflowWarnings = 1;

	/**
	 * Return the indexed priority queue. Should be accessed read-only for
	 * debugging only.
	 */
	public IndexedPriorityQueue<V> getPQ() {
		return pq;
	}

	/**
	 * Instantiate a DecayVariable object that can store up to M objects. Reads
	 * the initial parameters for the collection from the property file.
	 */
	public DecayVariableCollection(String collectionType) {
		this(collectionType,
			FWProps.getIntegerProperty("decay." + collectionType + ".maxSize"),
			FWProps.getDoubleProperty("decay." + collectionType + ".energyUnit"),
			FWProps.getDoubleProperty("decay." + collectionType + ".killThreshold"),
			FWProps.getTimeProperty("decay." + collectionType + ".halfLife"));
	}


	/**
	 * Instantiate a DecayVariable object that can store up to M objects.
	 */
	public DecayVariableCollection(String collectionType, int maxSize,
			double energyUnit, double killThreshold, long halfLife)
	{
		this.collectionType = collectionType;
		this.maxSize = maxSize;
		this.energyUnit = energyUnit;
		this.killThreshold = killThreshold;
		this.halfLife = halfLife;

		pq = IndexedPriorityQueue.create(maxSize);
	}

	/**
	 * If the collection is full, remove the item that is the next to expire.
	 * Otherwise, if there is an item that has already expired, remove it.
	 * Return the entry that has been removed, or NULL, if no entry was removed.
	 */
	private Entry<V> cleanupCollection(long now) {
		if (pq.size() > 0) {
			Entry<V> smallest = pq.peek();
			if (pq.size() == maxSize || smallest.getPriority() < now) {
				if (smallest.getPriority() >= now) {
					numOverflowWarnings++;
					if (log.isDebugEnabled()
							&& numOverflowWarnings == nextNumOverflowWarnings) {
						log.debug("DecayVariableCollection " + collectionType
								 + " (overflow #"
								 + numOverflowWarnings
								 + "):  killed item '"
								 + smallest.getKey() + "' "
								 + (smallest.getPriority() - now)
								 + " ms premature");
						nextNumOverflowWarnings <<= 1;
					}
				}
				return pq.pop();
			}
		}
		return null;
	}

	/**
	 * Update the energy associated with the specified key. If the key already
	 * exists, simply add more energy to the key and update the killtime
	 * appropriately. Otherwise add the new key, giving it the default amount of
	 * energy.
	 */
	public Entry<V> update(long key, long now) {
		Entry<V> e = pq.get(key);
		double currentEnergy = (e != null) ? findEnergyForKillTime(
				e.getPriority(), now) : 0.0;
		double newEnergy = currentEnergy + energyUnit;
		long newKillTime = findKillTimeForEnergy(newEnergy, now);

		if (e != null) {
			e.setPriority(newKillTime);
			pq.priorityHasChanged(e);
		} else {
			cleanupCollection(now);
			e = Entry.create(key, null, newKillTime);
			pq.add(e);
		}
		return e;
	}

	/**
	 * compute: energy = KILLTHRESHOLD * 2^((killTime-now)/HALFLIFE)
	 */
	public double findEnergyForKillTime(long killTime, long timeNow) {
		return killThreshold * Math.pow(2.0, ((double) killTime - timeNow) / halfLife);
	}

	/**
	 * compute: killtime = now + HALFLIFE * lg(energy/KILLTHRESHOLD)
	 */
	private long findKillTimeForEnergy(double energy, long timeNow) {
		return timeNow + (long) (halfLife * Math.log(energy / killThreshold) / LOG2);
	}

	public String toStringKillTime(long key) {
		Entry<V> e = pq.get(key);
		return e == null ? "0" : Long.toString(e.getPriority());
	}

	public String toStringPriorityQueue() {
		StringBuilder sb = new StringBuilder();
		sb.append("DecayVariable ").append(collectionType).append(":");
		sb.append(" size = ").append(pq.size());
		sb.append(", overflows = ").append(numOverflowWarnings);
		Entry<V>[] a = pq.toArray();
		Arrays.sort(a);
		for (Entry<V> e : a) {
			sb.append("\n\t").append(e.getKey()).append("\t").append(e.getPriority());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "DecayVariable " + collectionType + ":"
			+ "\n\tmaxSize = " + maxSize
			+ "\n\tenergyUnit = " + energyUnit
			+ "\n\thalfLife = " + halfLife
			+ "\n\tkillThreshold = " + killThreshold;
	}
}
