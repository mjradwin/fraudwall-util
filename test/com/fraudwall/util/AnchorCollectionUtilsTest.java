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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Tests the {@link AnchorCollectionUtils} implementation.
 *
 * @author Allan Heydon
 */
public class AnchorCollectionUtilsTest extends AbstractAnchorTest {

	// ---------------------------------------- concatLists

	public void testConcatListsReturnsEmptyListIfBothArgsAreNullOrEmpty() {
		List<String> empty = new ArrayList<String>();
		assertEquals(empty, AnchorCollectionUtils.concatLists(null, null));
		assertEquals(empty, AnchorCollectionUtils.concatLists(empty, null));
		assertEquals(empty, AnchorCollectionUtils.concatLists(null, empty));
		assertEquals(empty, AnchorCollectionUtils.concatLists(empty, empty));
	}

	public void testConcatListsReturnsOtherArgumentIfOneArgumentIsNullOrEmpty() {
		List<String> empty = new ArrayList<String>();
		List<String> l = Arrays.asList("foo", "bar", "baz");
		assertSame(l, AnchorCollectionUtils.concatLists(l, null));
		assertSame(l, AnchorCollectionUtils.concatLists(l, empty));
		assertSame(l, AnchorCollectionUtils.concatLists(null, l));
		assertSame(l, AnchorCollectionUtils.concatLists(empty, l));
	}

	public void testConcatListsReturnsConcatenationOfTwoLists() {
		List<String> l1 = Arrays.asList("foo", "bar", "baz");
		List<String> l2 = Arrays.asList("x", "y");
		List<String> l = Arrays.asList("foo", "bar", "baz", "x", "y");
		assertEquals(l, AnchorCollectionUtils.concatLists(l1, l2));
	}
}
