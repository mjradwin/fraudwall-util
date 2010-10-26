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


public class MathUtilitiesTest extends AbstractAnchorTest {

	// =================================================== scale

	public void testScaleThrowsIllegalArgumentExceptionIfValueBelowDomainInterval() {
		try {
			MathUtilities.scale(0.99f, 1.0f, 2.0f, 3.0f, 4.0f);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("x less than a", ex.getMessage());
		}
	}

	public void testScaleThrowsIllegalArgumentExceptionIfValueAboveDomainInterval() {
		try {
			MathUtilities.scale(2.01f, 1.0f, 2.0f, 3.0f, 4.0f);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("x greater than b", ex.getMessage());
		}
	}

	public void testScaleThrowsIllegalArgumentExceptionIfDomainIntervalIsEmpty() {
		try {
			MathUtilities.scale(1.0f, 1.0f, 1.0f, 3.0f, 4.0f);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals("domain interval is empty", ex.getMessage());
		}
	}

	public void testScaleWorksOnAVarietyOfTestInputs() {
		assertEquals(3.0f, MathUtilities.scale(1.0f, 1.0f, 2.0f, 3.0f, 5.0f));
		assertEquals(5.0f, MathUtilities.scale(2.0f, 1.0f, 2.0f, 3.0f, 5.0f));
		assertEquals(4.0f, MathUtilities.scale(1.5f, 1.0f, 2.0f, 3.0f, 5.0f));
		assertEquals(5.0f, MathUtilities.scale(0.0f, -1.0f, 1.0f, 0.0f, 10.0f));
		assertEquals(2.5f, MathUtilities.scale(-0.5f, -1.0f, 1.0f, 0.0f, 10.0f));

		// c > d
		assertEquals(5.0f, MathUtilities.scale(1.0f, 1.0f, 2.0f, 5.0f, 3.0f));
		assertEquals(3.0f, MathUtilities.scale(2.0f, 1.0f, 2.0f, 5.0f, 3.0f));
		assertEquals(4.0f, MathUtilities.scale(1.5f, 1.0f, 2.0f, 5.0f, 3.0f));
		assertEquals(3.5f, MathUtilities.scale(1.75f, 1.0f, 2.0f, 5.0f, 3.0f));
		assertEquals(5.0f, MathUtilities.scale(0.0f, -1.0f, 1.0f, 10.0f, 0.0f));
		assertEquals(7.5f, MathUtilities.scale(-0.5f, -1.0f, 1.0f, 10.0f, 0.0f));

		// c == d
		assertEquals(5.0f, MathUtilities.scale(1.75f, 1.0f, 2.0f, 5.0f, 5.0f));
	}

	// =================================================== log
	public void testLogThrowsIllegalArgumentExceptionIfBaseIsNegative() {
		try {
			MathUtilities.log(-1F,2F);
			fail();
		} catch (IllegalArgumentException ex) {
			; // expected case
		}
	}

	public void testLogThrowsIllegalArgumentExceptionIfValueIsNegative() {
		try {
			MathUtilities.log(2F,-2F);
			fail();
		} catch (IllegalArgumentException ex) {
			; // expected case
		}
	}

	public void testLogComputesCorrectValue() {
		assertEquals(3F, MathUtilities.log(3,3*3*3));
	}

	// =================================================== log2

	public void testLog2ComputesCorrectValue() {
		assertEquals(3F, MathUtilities.log2(2*2*2));
	}
}
