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
 * Implements the static function {@link #printSizeOf(Class)} which
 * takes a class and determines the memory consumption of an instance of
 * that class.  This is accomplished by allocating 10K objects of that
 * class and measuring the used memory before and after the run.
 */
public class Sizeof {
	public static final Runtime s_runtime = Runtime.getRuntime();

	@SuppressWarnings("unchecked")
	public static void printSizeOf(Class c) throws Exception {
		// Warm up all classes/methods we will use
		runGC();
		usedMemory();
		// Array to keep strong references to allocated objects
		final int count = 100000;
		Object[] objects = new Object[count];

		long heap1 = 0;
		// Allocate count+1 objects, discard the first one
		for (int i = -1; i < count; ++i) {
			Object object = null;

			object = c.newInstance();

			if (i >= 0) {
				objects[i] = object;
			} else {
				object = null; // Discard the warm up object
				runGC();
				heap1 = usedMemory(); // Take a before heap snapshot
			}
		}
		runGC();
		long heap2 = usedMemory(); // Take an after heap snapshot:

		final int size = Math.round(((float) (heap2 - heap1)) / count);
		System.out.println(c.getName());
		System.out.println("'before' heap: " + heap1 + ", 'after' heap: "
				+ heap2);
		System.out.println("heap delta: " + (heap2 - heap1) + ", {"
				+ c.getName() + "} size = " + size + " bytes");
		for (int i = 0; i < count; ++i)
			objects[i] = null;
		objects = null;
	}

	public static void runGC() throws Exception {
		// It helps to call Runtime.gc()
		// using several method calls:
		for (int r = 0; r < 4; ++r)
			_runGC();
	}

	public static void _runGC() throws Exception {
		long usedMem1 = usedMemory(), usedMem2 = Long.MAX_VALUE;
		for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++i) {
			s_runtime.runFinalization();
			s_runtime.gc();
			Thread.yield();

			usedMem2 = usedMem1;
			usedMem1 = usedMemory();
		}
	}

	public static long usedMemory() {
		return s_runtime.totalMemory() - s_runtime.freeMemory();
	}

	public static void main(String[] args) throws Exception {
		printSizeOf(Boolean1.class);
		printSizeOf(Boolean2.class);
		printSizeOf(Boolean3.class);
		printSizeOf(Boolean4.class);
		printSizeOf(Boolean5.class);
		printSizeOf(Boolean6.class);
		printSizeOf(Boolean7.class);
		printSizeOf(Boolean8.class);
		printSizeOf(Boolean9.class);

		System.out.println();
		printSizeOf(Byte1.class);
		printSizeOf(Byte2.class);
		printSizeOf(Byte3.class);
		printSizeOf(Byte4.class);
		printSizeOf(Byte5.class);
		printSizeOf(Byte6.class);
		printSizeOf(Byte7.class);
		printSizeOf(Byte8.class);
		printSizeOf(Byte9.class);

		System.out.println();
		printSizeOf(Char1.class);
		printSizeOf(Char2.class);
		printSizeOf(Char3.class);
		printSizeOf(Char4.class);
		printSizeOf(Char5.class);

		System.out.println();
		printSizeOf(Short1.class);
		printSizeOf(Short2.class);
		printSizeOf(Short3.class);
		printSizeOf(Short4.class);
		printSizeOf(Short5.class);

		System.out.println();
		printSizeOf(Int1.class);
		printSizeOf(Int2.class);
		printSizeOf(Int3.class);
		printSizeOf(Int4.class);
		printSizeOf(Int5.class);

		System.out.println();
		printSizeOf(Long1.class);
		printSizeOf(Long2.class);
		printSizeOf(Long3.class);
		printSizeOf(Long4.class);
		printSizeOf(Long5.class);

		System.out.println();
		printSizeOf(BooleanArray1.class);
		printSizeOf(BooleanArray2.class);
		printSizeOf(BooleanArray3.class);
		printSizeOf(BooleanArray4.class);
		printSizeOf(BooleanArray8.class);
		printSizeOf(BooleanArray16.class);
		printSizeOf(BooleanArray32.class);
		printSizeOf(BooleanArray64.class);

		System.out.println();
		printSizeOf(IpReputationRecord.class);

		System.out.println();
		printSizeOf(Empty.class);

		// Warm up all classes/methods we will use
		runGC();
		usedMemory();
		// Array to keep strong references to allocated objects
		final int count = 100000;
		Object[] objects = new Object[count];

		long heap1 = 0;
		// Allocate count+1 objects, discard the first one
		for (int i = -1; i < count; ++i) {
			Object object = null;

			object = new int[100];

			if (i >= 0) {
				objects[i] = object;
			} else {
				object = null; // Discard the warm up object
				runGC();
				heap1 = usedMemory(); // Take a before heap snapshot
			}
		}
		runGC();
		long heap2 = usedMemory(); // Take an after heap snapshot:

		final int size = Math.round(((float) (heap2 - heap1)) / count);
		System.out.println("int[100]");
		System.out.println("'before' heap: " + heap1 + ", 'after' heap: "
				+ heap2);
		System.out.println("heap delta: " + (heap2 - heap1) + ", {"
				+ "int[100]" + "} size = " + size + " bytes");
		for (int i = 0; i < count; ++i)
			objects[i] = null;
		objects = null;

	}

// Test classes for calculating information on various
@SuppressWarnings("unused")
static class Short1 {
	private short s1;
}

@SuppressWarnings("unused")
static class Short2 {
	private short s1;
	private short s2;
}

@SuppressWarnings("unused")
static class Short3 {
	private short s1;
	private short s2;
	private short s3;
}

@SuppressWarnings("unused")
static class Short4 {
	private short s1;
	private short s2;
	private short s3;
	private short s4;
}

@SuppressWarnings("unused")
static class Short5 {
	private short s1;
	private short s2;
	private short s3;
	private short s4;
	private short s5;
}

@SuppressWarnings("unused")
static class Char1 {
	private char s1;
}

@SuppressWarnings("unused")
static class Char2 {
	private char s1;
	private char s2;
}

@SuppressWarnings("unused")
static class Char3 {
	private char s1;
	private char s2;
	private char s3;
}

@SuppressWarnings("unused")
static class Char4 {
	private char s1;
	private char s2;
	private char s3;
	private char s4;
}

@SuppressWarnings("unused")
static class Char5 {
	private char s1;
	private char s2;
	private char s3;
	private char s4;
	private char s5;
}

@SuppressWarnings("unused")
static class Boolean1 {
	private boolean s1;
}

@SuppressWarnings("unused")
static class Boolean2 {
	private boolean s1;
	private boolean s2;
}

@SuppressWarnings("unused")
static class Boolean3 {
	private boolean s1;
	private boolean s2;
	private boolean s3;
}

@SuppressWarnings("unused")
static class Boolean4 {
	private boolean s1;
	private boolean s2;
	private boolean s3;
	private boolean s4;
}

@SuppressWarnings("unused")
static class Boolean5 {
	private boolean s1;
	private boolean s2;
	private boolean s3;
	private boolean s4;
	private boolean s5;
}

@SuppressWarnings("unused")
static class Boolean6 {
	private boolean s1;
	private boolean s2;
	private boolean s3;
	private boolean s4;
	private boolean s5;
	private boolean s6;
}

@SuppressWarnings("unused")
static class Boolean7 {
	private boolean s1;
	private boolean s2;
	private boolean s3;
	private boolean s4;
	private boolean s5;
	private boolean s6;
	private boolean s7;
}

@SuppressWarnings("unused")
static class Boolean8 {
	private boolean s1;
	private boolean s2;
	private boolean s3;
	private boolean s4;
	private boolean s5;
	private boolean s6;
	private boolean s7;
	private boolean s8;
}

@SuppressWarnings("unused")
static class Boolean9 {
	private boolean s1;
	private boolean s2;
	private boolean s3;
	private boolean s4;
	private boolean s5;
	private boolean s6;
	private boolean s7;
	private boolean s8;
	private boolean s9;
}

@SuppressWarnings("unused")
static class Int1 {
	private int s1;
}

@SuppressWarnings("unused")
static class Int2 {
	private int s1;
	private int s2;
}

@SuppressWarnings("unused")
static class Int3 {
	private int s1;
	private int s2;
	private int s3;
}

@SuppressWarnings("unused")
static class Int4 {
	private int s1;
	private int s2;
	private int s3;
	private int s4;
}

@SuppressWarnings("unused")
static class Int5 {
	private int s1;
	private int s2;
	private int s3;
	private int s4;
	private int s5;
}

@SuppressWarnings("unused")
static class Long1 {
	private long s1;
}

@SuppressWarnings("unused")
static class Long2 {
	private long s1;
	private long s2;
}

@SuppressWarnings("unused")
static class Long3 {
	private long s1;
	private long s2;
	private long s3;
}

@SuppressWarnings("unused")
static class Long4 {
	private long s1;
	private long s2;
	private long s3;
	private long s4;
}

@SuppressWarnings("unused")
static class Long5 {
	private long s1;
	private long s2;
	private long s3;
	private long s4;
	private long s5;
}

@SuppressWarnings("unused")
static class Byte1 {
	private byte s1;
}

@SuppressWarnings("unused")
static class Byte2 {
	private byte s1;
	private byte s2;
}

@SuppressWarnings("unused")
static class Byte3 {
	private byte s1;
	private byte s2;
	private byte s3;
}

@SuppressWarnings("unused")
static class Byte4 {
	private byte s1;
	private byte s2;
	private byte s3;
	private byte s4;
}

@SuppressWarnings("unused")
static class Byte5 {
	private byte s1;
	private byte s2;
	private byte s3;
	private byte s4;
	private byte s5;
}

@SuppressWarnings("unused")
static class Byte6 {
	private byte s1;
	private byte s2;
	private byte s3;
	private byte s4;
	private byte s5;
	private byte s6;
}

@SuppressWarnings("unused")
static class Byte7 {
	private byte s1;
	private byte s2;
	private byte s3;
	private byte s4;
	private byte s5;
	private byte s6;
	private byte s7;
}

@SuppressWarnings("unused")
static class Byte8 {
	private byte s1;
	private byte s2;
	private byte s3;
	private byte s4;
	private byte s5;
	private byte s6;
	private byte s7;
	private byte s8;
}

@SuppressWarnings("unused")
static class Byte9 {
	private byte s1;
	private byte s2;
	private byte s3;
	private byte s4;
	private byte s5;
	private byte s6;
	private byte s7;
	private byte s8;
	private byte s9;
}

@SuppressWarnings("unused")
static class BooleanArray1 {
	private boolean[] b = { true };
}

@SuppressWarnings("unused")
static class BooleanArray2 {
	private boolean[] b = { true, true };
}

@SuppressWarnings("unused")
static class BooleanArray3 {
	private boolean[] b = { true, true, true };
}

@SuppressWarnings("unused")
static class BooleanArray4 {
	private boolean[] b = { true, true, true, true };
}

@SuppressWarnings("unused")
static class BooleanArray8 {
	private boolean[] b = { true, true, true, true, true, true, true, true, };
}

@SuppressWarnings("unused")
static class BooleanArray16 {
	private boolean[] b = { true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, };
}

@SuppressWarnings("unused")
static class BooleanArray32 {
	private boolean[] b = { true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, };
}

@SuppressWarnings("unused")
static class BooleanArray64 {
	private boolean[] b = { true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, true, true, true, true,
			true, };
}


@SuppressWarnings("unused")
static class IpReputationRecord {
	private static Map<Boolean,Byte> idToDateMap = new HashMap<Boolean,Byte>();
	private static Map<Byte,Boolean> dateToIdMap = new HashMap<Byte,Boolean>();

	protected short firstSeen = -1; // -1 indicates no data
	protected short code;
	protected short reports;
}

static class Empty {
}

} // End of class
