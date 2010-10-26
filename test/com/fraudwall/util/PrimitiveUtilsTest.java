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


/**
 * Tests the {@link PrimitiveUtils} implementation.
 * 
 * @author Allan Heydon
 */
public class PrimitiveUtilsTest extends AbstractAnchorTest {
	
	// ---------------------------------------- compareInts
	
	public void testCompareIntsReturnsCorrectResult() {
		// equality tests
		checkCompareInts(0, Integer.MIN_VALUE, Integer.MIN_VALUE);
		checkCompareInts(0, Integer.MAX_VALUE, Integer.MAX_VALUE);
		checkCompareInts(0, -123, -123);
		checkCompareInts(0, 0, 0);
		checkCompareInts(0, 123, 123);
		
		// less-than tests
		checkCompareInts(-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
		checkCompareInts(-1, Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
		checkCompareInts(-1, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
		checkCompareInts(-1, -123, -122);
		checkCompareInts(-1, -1, 0);
		checkCompareInts(-1, 0, 1);
		checkCompareInts(-1, -1, 1);
		checkCompareInts(-1, 122, 123);
		
		// greater-than tests
		checkCompareInts(1, Integer.MAX_VALUE, Integer.MIN_VALUE);
		checkCompareInts(1, Integer.MIN_VALUE + 1, Integer.MIN_VALUE);
		checkCompareInts(1, Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
		checkCompareInts(1, -122, -123);
		checkCompareInts(1, 0, -1);
		checkCompareInts(1, 1, 0);
		checkCompareInts(1, 1, -1);
		checkCompareInts(1, 123, 122);
	}
	
	private void checkCompareInts(int expResult, int v1, int v2) {
		assertEquals(expResult, PrimitiveUtils.compareInts(v1, v2));
	}
	
	// ---------------------------------------- compareLongs
	
	public void testCompareLongsReturnsCorrectResult() {
		// equality tests
		checkCompareLongs(0, Long.MIN_VALUE, Long.MIN_VALUE);
		checkCompareLongs(0, Long.MAX_VALUE, Long.MAX_VALUE);
		checkCompareLongs(0, -123, -123);
		checkCompareLongs(0, 0, 0);
		checkCompareLongs(0, 123, 123);
		
		// less-than tests
		checkCompareLongs(-1, Long.MIN_VALUE, Long.MAX_VALUE);
		checkCompareLongs(-1, Long.MIN_VALUE, Long.MIN_VALUE + 1);
		checkCompareLongs(-1, Long.MAX_VALUE - 1, Long.MAX_VALUE);
		checkCompareLongs(-1, -123, -122);
		checkCompareLongs(-1, -1, 0);
		checkCompareLongs(-1, 0, 1);
		checkCompareLongs(-1, -1, 1);
		checkCompareLongs(-1, 122, 123);
		
		// greater-than tests
		checkCompareLongs(1, Long.MAX_VALUE, Long.MIN_VALUE);
		checkCompareLongs(1, Long.MIN_VALUE + 1, Long.MIN_VALUE);
		checkCompareLongs(1, Long.MAX_VALUE, Long.MAX_VALUE - 1);
		checkCompareLongs(1, -122, -123);
		checkCompareLongs(1, 0, -1);
		checkCompareLongs(1, 1, 0);
		checkCompareLongs(1, 1, -1);
		checkCompareLongs(1, 123, 122);
	}
	
	private void checkCompareLongs(int expResult, long v1, long v2) {
		assertEquals(expResult, PrimitiveUtils.compareLongs(v1, v2));
	}
}
