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
package com.fraudwall.util.fp;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.fraudwall.util.ArgCheck;

/**
 * A special kind of 64-bit checksum, called a <em>fingerprint</em>. This
 * class provides methods for computing 64-bit fingerprints of strings,
 * character arrays, byte arrays, and input streams. It also provides methods
 * for <i>extending</i> an existing fingerprint by more bytes or characters.
 * Extending the fingerprint of one string by another string produces a
 * fingerprint equivalent to the fingerprint of the concatenation of the two
 * strings:
 * <pre>
 * new FP64(s1 + s2).equals(new FP64(s1).extend(s2))
 * </pre>
 * <p>
 * All operations for extending a fingerprint are destructive; that is, they
 * modify the fingerprint in place. All operations return the resulting FP64
 * object, so method calls can be chained together (e.g., <i>new
 * FP64("x").extend(foo).extend(92)</i>). If you want to make a copy of a
 * fingerprint, the {@link #FP64(FP64)} constructor can be used.
 * <p>
 * The implementation is based on an original idea of Michael O. Rabin, with
 * further refinements by Andrei Broder. Fingerprints provide a probabilistic
 * guarantee that defines a mathematical upper bound on the probability of a
 * collision (a collision occurs if two different strings have the same
 * fingerprint). Using 64-bit fingerprints, the odds of a collision are
 * <i>extremely</i> small: the odds of a collision between two randomly chosen
 * texts a million characters long are less than 1 in a trillion. In particular,
 * if you have a set <i>S</i> of <i>n</i> distinct strings each of which is at
 * most <i>m</i> characters long, then the odds of any two different strings in
 * <i>S</i> having the same fingerprint is at most <i>(n * m^2) / 2^k</i>,
 * where <i>k</i> is the number of bits in the fingerprint.
 * <p>
 * Therefore, for all intents and purposes fingerprints can be treated as
 * uniquely identifying the bytes that produced them (hence, their name). In
 * mathematical notation:
 * <pre>
 * new FP64(s1).equals(new FP64(s2)) ==&gt; s1.equals(s2)
 * </pre>
 * <p>
 * The underlying value of a fingerprint (as produced by the {@link #getValue()}
 * and {@link #toHexString()} methods) should never be fingerprinted. Doing so
 * voids the probabilistic guarantee, which may lead to unexpected collisions.
 * <p>
 * This class overrides the {@link #equals} and {@link #hashCode} methods, so
 * FP64 objects may be used as keys in hash tables.
 * 
 * @author Allan Heydon
 */
@SuppressWarnings("serial")
public class FP64 implements Serializable {
	private long fp;

	/** Initializes this object to the fingerprint of the empty string. */
	public FP64() {
		this.fp = IrredPoly;
	}

	/**
	 * Initializes this fingerprint to a <em>copy</em> of <code>fp</code>,
	 * which must be non-null.
	 */
	public FP64(FP64 fp) {
		ArgCheck.isNotNull(fp, "fp");
		this.fp = fp.fp;
	}

	/**
	 * Initializes this object to the fingerprint of the String
	 * <code>s</code>, which must be non-null.
	 */
	public FP64(String s) {
		this();
		extend(s);
	}

	/**
	 * Initializes this object to the fingerprint of the character
	 * array <code>chars</code>, which must be non-null.
	 */
	public FP64(char[] chars) {
		this();
		ArgCheck.isNotNull(chars, "chars");
		extend(chars, 0, chars.length);
	}

	/**
	 * Initializes this object to the fingerprint of the characters
	 * <code>chars[start]..chars[start+length-1]</code>.
	 */
	public FP64(char[] chars, int start, int length) {
		this();
		extend(chars, start, length);
	}

	/**
	 * Initializes this object to the fingerprint of the byte
	 * array <code>bytes</code>, which must be non-null.
	 */
	public FP64(byte[] bytes) {
		this();
		ArgCheck.isNotNull(bytes, "bytes");
		extend(bytes, 0, bytes.length);
	}

	/**
	 * Initializes this object to the fingerprint of the bytes
	 * <code>bytes[start]..bytes[start+length-1]</code>.
	 */
	public FP64(byte[] bytes, int start, int length) {
		this();
		extend(bytes, start, length);
	}

	/**
	 * Initializes this object to the fingerprint of the bytes
	 * in the reader <code>rd</code>, which must be non-null.
	 *
	 * @throws IOException
	 * if an error is encountered reading <code>stream</code>.
	 */
	public FP64(Reader rd) throws IOException {
		this();
		extend(rd);
	}

	/**
	 * Returns the underlying value of this fingerprint as a long.<p>
	 *
	 * <b>Important:</b> If the output of this function is subsequently
	 * fingerprinted, the probabilistic guarantee is lost. That is,
	 * there is a much higher likelihood of fingerprint collisions if
	 * fingerprint values are themselves fingerprinted in any way.
	 */
	public long getValue() {
		return fp;
	}

	/**
	 * Returns the value of this fingerprint as an unsigned integer encoded
	 * in base 16 (hexidecimal), padded with leading zeros to a total length
	 * of 16 characters.<p>
	 *
	 * <b>Important:</b> If the output of this function is subsequently
	 * fingerprinted, the probabilistic guarantee is lost. That is,
	 * there is a much higher likelihood of fingerprint collisions if
	 * fingerprint values are themselves fingerprinted in any way.
	 */
	public String toHexString() {
		return String.format("%016x", fp);
	}

	/**
	 * Extends this fingerprint by the characters of the String
	 * <code>s</code>, which may be non-null.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(String s) {
		fp = extend(fp, s);
		return this;
	}

	/**
	 * Extends this fingerprint by the characters
	 * <code>chars[start]..chars[start+length-1]</code>.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(char[] chars, int start, int len) {
		ArgCheck.isNotNull(chars, "chars");
		ArgCheck.isInInterval(start, "start", 0, chars.length);
		ArgCheck.isInInterval(len, "len", 0, chars.length + 1 - start);
		int end = start + len;
		for (int i = start; i < end; i++) {
			extend(chars[i]);
		}
		return this;
	}

	/**
	 * Extends this fingerprint by the given <code>chars</code>.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(char[] chars) {
		ArgCheck.isNotNull(chars, "chars");
		extend(chars, 0, chars.length);
		return this;
	}

	/**
	 * Extends this fingerprint by the bytes
	 * <code>bytes[offset]..bytes[offset+length-1]</code>.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(byte[] bytes, int start, int len) {
		ArgCheck.isNotNull(bytes, "bytes");
		ArgCheck.isInInterval(start, "start", 0, bytes.length);
		ArgCheck.isInInterval(len, "len", 0, bytes.length + 1 - start);
		final int end = start + len;
		for (int i = start; i < end; i++) {
			extend(bytes[i]);
		}
		return this;
	}

	/**
	 * Extends this fingerprint by the given <code>bytes</code>.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(byte[] bytes) {
		ArgCheck.isNotNull(bytes, "bytes");
		extend(bytes, 0, bytes.length);
		return this;
	}

	/**
	 * Extends this fingerprint by the integer <code>i</code>.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(int i) {
		extend((byte) ((i >>> 24) & 0xFF));
		extend((byte) ((i >>> 16) & 0xFF));
		extend((byte) ((i >>> 8) & 0xFF));
		extend((byte) ((i) & 0xFF));
		return this;
	}

	/**
	 * Extends this fingerprint by the long <code>l</code>.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(long l) {
		extend((int) ((l >>> 32) & 0xFFFFFFFF));
		extend((int) ((l) & 0xFFFFFFFF));
		return this;
	}

	/**
	 * Extends this fingerprint by the character <code>c</code>.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(char c) {
		fp = extend(fp, c);
		return this;
	}

	/**
	 * Extends this fingerprint by the byte <code>b</code>.
	 *
	 * @return
	 * the resulting fingerprint.
	 */
	public FP64 extend(byte b) {
		fp = extend(fp, b);
		return this;
	}

	/**
	 * Extends this fingerprint by the bytes of the reader
	 * <code>rd</code>, which must be non-null.
	 *
	 * @return
	 * the resulting fingerprint.
	 *
	 * @throws IOException
	 * if an error is encountered reading <code>rd</code>.
	 */
	public FP64 extend(Reader rd) throws IOException {
		ArgCheck.isNotNull(rd, "rd");
		int b;
		while ((b = rd.read()) != -1) {
			extend((byte) (b & 0xff));
			b >>= 8;
			if (b != 0) {
				extend((byte) (b & 0xff));
			}
		}
		return this;
	}
	
	/**
	 * Returns the fingerprint of the string <code>s</code>. The result
	 * of this method is equivalent to "new FP64(s).getValue()", except that
	 * if <code>s</code> is null or the empty string, it returns 0. The
	 * advantage to using this method is that it does not require an FP64
	 * object to be allocated.
	 * 
	 * @see #FP64(String)
	 */
	public static long fp(String s) {
		return StringUtils.isEmpty(s) ? 0L : extend(IrredPoly, s);
	}

	private static long extend(long fp, String s) {
		if (s != null) {
			final int len = s.length();
			for (int i = 0; i < len; i++) {
				fp = extend(fp, s.charAt(i));
			}
		}
		return fp;
	}

	private static long extend(long fp, char c) {
		byte b1 = (byte) (c & 0xff);
		fp = extend(fp, b1);
		byte b2 = (byte) (c >>> 8);
		if (b2 != 0) {
			fp = extend(fp, b2);
		}
		return fp;
	}

	private static long extend(long fp, byte b) {
		return (fp >>> 8) ^ ByteModTable[(b ^ (int) fp) & 0xFF];
	}

	@Override
	public int hashCode() {
		return ((int) fp) ^ ((int) (fp >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FP64)) {
			return false;
		}
		return fp == ((FP64) obj).fp;
	}

	/* This class provides methods that construct fingerprints of
	 * strings of bytes via operations in GF[2^64].  GF[2^64] is represented
	 * as the set polynomials of degree 64 with coefficients in Z(2),
	 * modulo an irreducible polynomial P of degree 64.  The internal
	 * representation is a 64-bit Java value of type "long".
	 *
	 * Let g(S) be the string obtained from S by prepending the byte 0x80
	 * and appending eight 0x00 bytes.  Let f(S) be the polynomial
	 * associated to the string g(S) viewed as a polynomial with
	 * coefficients in the field Z(2). The fingerprint of S is simply
	 * the value f(S) modulo P.
	 *
	 * The irreducible polynomial p used as a modulus is
	 *
	 *        3    7    11    13    16    19    20    24    26    28
	 *   1 + x  + x  + x   + x   + x   + x   + x   + x   + x   + x
	 *
	 *      29    30    36    37    38    41    42    45    46    48
	 *   + x   + x   + x   + x   + x   + x   + x   + x   + x   + x
	 *
	 *      50    51    52    54    56    57    59    61    62    64
	 *   + x   + x   + x   + x   + x   + x   + x   + x   + x   + x
	 *
	 * IrredPoly is its representation.
	 */

	// implementation constants
	// polynomials are represented with the coefficient for x^0
	// in the most significant bit
	private static final long Zero = 0L;
	private static final long One = 0x8000000000000000L;
	private static final long IrredPoly = 0x911498AE0E66BAD6L;
	private static final long X63 = 0x1L; // coefficient of x^63

	/* This is the table used for extending fingerprints.  The
	 * value ByteModTable[i] is the value to XOR into the finger-
	 * print value when the byte with value "i" is shifted from
	 * the top-most byte in the fingerprint. */
	private static final long[] ByteModTable;

	// Initialization code
	static {
		// Maximum power needed == 64 + 8
		int plength = 72;
		long[] powerTable = new long[plength];

		long t = One;
		for (int i = 0; i < plength; i++) {
			powerTable[i] = t;
			//System.out.println("pow[" + i + "] = " + Long.toHexString(t));

			// t = t * x
			long mask = ((t & X63) != 0) ? IrredPoly : 0;
			t = (t >>> 1) ^ mask;
		}

		// group bit-wise overflows into bytes
		ByteModTable = new long[256];
		for (int j = 0; j < ByteModTable.length; j++) {
			long v = Zero;
			for (int k = 0; k < 9; k++) {
				if ((j & (1L << k)) != 0) {
					v ^= powerTable[(plength - 1) - k];
				}
			}
			ByteModTable[j] = v;
			//System.out.println("ByteModTable[" + j + "] = " + Long.toHexString(v));
		}
	}

	public static void main(String[] args) {
		for (String arg : args) {
			System.out.println(String.format("%40s -> %016x", "\"" + arg + "\"", fp(arg)));
		}
	}
}
