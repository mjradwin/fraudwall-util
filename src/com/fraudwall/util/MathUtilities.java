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

import com.fraudwall.util.exc.ArgCheck;

/**
 * Repository of basic computational utility functions
 */
public class MathUtilities {
	/**
	 * Scales the value "x" in the interval [a,b] linearly to the interval [c,d].
	 * Requires a &lt; b and a &lt;= x &lt;= b.  Will work if c > d (if x == a
	 * then the scaled value will be c or if x == b then the scaled value will
	 * be d).
	 */
	public static float scale(float x, float a, float b, float c, float d) {
		ArgCheck.isFalse(a >= b, "domain interval is empty");
		ArgCheck.isFalse(x < a, "x less than a");
		ArgCheck.isFalse(x > b, "x greater than b");
		return ((x-a)*(d-c)/(b-a) + c);
	}

	/**
	 * Compute the log base 2 of the the value "x"
	 */
	public static float log2(float x) {
		return log(2, x);
	}

	/**
	 * Compute the log base "base" of the the value "x"
	 */
	public static float log(float base, float value) {
		ArgCheck.isTrue(base > 0, "base must be positive");
		ArgCheck.isTrue(value > 0, "value must be positive");
		return (float)(Math.log(value)/Math.log(base));
	}
}
