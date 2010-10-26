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

import java.util.Collection;
import java.util.HashSet;


/**
 * Tests the {@link Require} implementation.
 */
public class RequireTest extends AbstractAnchorTest {

	private static final String MSG = "This is an error message";

	/** {@link Require#isNull(Object, String)} *****************************************/

	public void testIsNullDoesNotThrowIfArgumentIsNull() {
		Require.isNull(null, MSG);
	}

	public void testIsNullThrowsIfArgumentIsNotNull() {
		try {
			Require.isNull("", MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

	/** {@link Require#isNotNull(Object, String)} *****************************************/

	public void testIsNotNullThrowsIfArgumentIsNull() {
		try {
			Require.isNotNull(null, MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

	public void testIsNotNullDoesNotThrowIfArgumentIsNotNull() {
		Require.isNotNull("", MSG);
	}

	/** {@link Require#isTrue(boolean, String)} *****************************************/

	public void testIsTrueDoesNotThrowIfArgumentIsTrue() {
		Require.isTrue(true, MSG);
	}

	public void testIsTrueThrowsIfArgumentIsFalse() {
		try {
			Require.isTrue(false, MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

	/** {@link Require#isFalse(boolean, String)} *****************************************/

	public void testIsFalseDoesNotThrowIfArgumentIsFalse() {
		Require.isFalse(false, MSG);
	}

	public void testIsFalseThrowsIfArgumentIsTrue() {
		try {
			Require.isFalse(true, MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

	/** {@link Require#isEmpty(Collection, String)} *****************************************/

	public void testIsEmptyDoesNotThrowIfArgumentIsEmpty() {
		Require.isEmpty(new HashSet<String>(), MSG);
	}

	public void testIsEmptyThrowsIfArgumentIsNotEmpty() {
		try {
			HashSet<String> set = new HashSet<String>();
			set.add("");
			Require.isEmpty(set, MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

	/** {@link Require#isNotEmpty(Collection, String)} *****************************************/

	public void testIsNotEmptyDoesNotThrowIfArgumentIsNotEmpty() {
		HashSet<String> set = new HashSet<String>();
		set.add("");
		Require.isNotEmpty(set, MSG);
	}

	public void testIsNotEmptyThrowsIfArgumentIsEmpty() {
		try {
			Require.isNotEmpty(new HashSet<String>(), MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

	/** {@link Require#isBlank(String, String)} *****************************************/

	public void testIsBlankDoesNotThrowIfArgumentIsBlank() {
		Require.isBlank("", MSG);
	}

	public void testIsBlankThrowsIfArgumentIsNotBlank() {
		try {
			Require.isBlank("foo", MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

	/** {@link Require#isNotBlank(String, String)} *****************************************/

	public void testIsNotBlankDoesNotThrowIfArgumentIsNotBlank() {
		Require.isNotBlank("foo", MSG);
	}

	public void testIsNotBlankThrowsIfArgumentIsTrue() {
		try {
			Require.isNotBlank("", MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

	/** {@link Require#isInstanceOf(Object, Class, String)} *****************************************/

	public void testIsInstanceOfDoesNotThrowIfArgumentIsIntanceOfClass() {
		Require.isInstanceOf("", String.class, MSG);
	}

	public void testIsInstanceOfThrowsIfArgumentIsNotInstanceOfClass() {
		try {
			Require.isInstanceOf("", Boolean.class, MSG);
			fail();
		} catch (IllegalStateException e) {
			assertSame(MSG, e.getMessage());
		}
	}

}
