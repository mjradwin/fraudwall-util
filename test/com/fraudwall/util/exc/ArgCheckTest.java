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
package com.fraudwall.util.exc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fraudwall.util.exc.ArgCheck;

import junit.framework.TestCase;

/**
 * Tests the {@link ArgCheck} implementation.
 *
 * @author Allan Heydon
 */
public class ArgCheckTest extends TestCase {

	private static final String ARG_NAME = "argName";
	private static final String ERROR_MESSAGE = "error message";

	// This constructor exists solely for one of the tests in UtilitiesTest
	@SuppressWarnings("unused")
	private ArgCheckTest() {
		this("ArgCheckTest");
	}

	// This constructor exists solely for one of the tests in UtilitiesTest
	public ArgCheckTest(Integer x) {
		this("ArgCheckTest" + x.toString());
	}

	// This constructor exists solely for one of the tests in UtilitiesTest
	public ArgCheckTest(String name, Integer x) {
		this(name + x.toString());
	}

	public ArgCheckTest(String name) {
		super(name);
	}

	// ============================================= isNotNull

	public void testIsNotNullIsNoOpIfArgIsNonNull() {
		ArgCheck.isNotNull(new Object(), ARG_NAME);
	}

	public void testIsNotNullThrowsIllegalArgumentExceptionIfArgIsNull() {
		try {
			ArgCheck.isNotNull(null, ARG_NAME);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNotNullException(ARG_NAME, ex);
		}
	}

	/**
	 * Checks that the exception <code>ex</code> was produced as the result of
	 * the failure of a call to {@link ArgCheck#isNotNull(Object, String)} on
	 * the argument named <code>argName</code>.
	 */
	public static void assertIsNotNullException(String argName, IllegalArgumentException ex) {
		String errMsg = "Argument '" + argName + "' expected to be non-null.";
		assertEquals(errMsg, ex.getMessage());
	}

	// ============================================= isNull

	public void testIsNullIsNoOpIfArgIsNull() {
		ArgCheck.isNull(null, ARG_NAME);
	}

	public void testIsNullThrowsIllegalArgumentExceptionIfArgIsNotNull() {
		try {
			ArgCheck.isNull(new Object(), ARG_NAME);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNullException(ARG_NAME, ex);
		}
	}

	/**
	 * Checks that the exception <code>ex</code> was produced as the result of
	 * the failure of a call to {@link ArgCheck#isNull(Object, String)} on
	 * the argument named <code>argName</code>.
	 */
	public static void assertIsNullException(String argName, IllegalArgumentException ex) {
		String errMsg = "Argument '" + argName + "' expected to be null.";
		assertEquals(errMsg, ex.getMessage());
	}

	// ============================================= isNotBlank

	public void testIsNotBlankIsNoOpIfArgIsNonNullAndNonBlank() {
		ArgCheck.isNotBlank("a", ARG_NAME);
	}

	public void testIsNotBlankThrowsIllegalArgumentExceptionIfArgIsNull() {
		checkIsNotBlankThrowsIllegalArgumentExceptionOn(null);
	}

	public void testIsNotBlankThrowsIllegalArgumentExceptionIfArgIsEmpty() {
		checkIsNotBlankThrowsIllegalArgumentExceptionOn("");
	}

	public void testIsNotBlankThrowsIllegalArgumentExceptionIfArgIsBlank() {
		checkIsNotBlankThrowsIllegalArgumentExceptionOn("  \t  ");
	}

	private void checkIsNotBlankThrowsIllegalArgumentExceptionOn(String argValue) {
		try {
			ArgCheck.isNotBlank(argValue, ARG_NAME);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNotBlankException(ARG_NAME, ex);
		}
	}

	/**
	 * Checks that the exception <code>ex</code> was produced as the result of
	 * the failure of a call to {@link ArgCheck#isNotBlank(String, String)} on
	 * the argument named <code>argName</code>.
	 */
	public static void assertIsNotBlankException(String argName, IllegalArgumentException ex) {
		String errMsg = "String argument '" + argName + "' is null, empty, or all whitespace characters.";
		assertEquals(errMsg, ex.getMessage());
	}

	// ============================================= isNotEmpty(T[], String)

	public void testIsNotEmptyArrayIsNoOpIfArgIsNonNullAndNonEmpty() {
		ArgCheck.isNotEmpty(new String[] { "foo" }, ARG_NAME);
	}

	public void testIsNotEmtpyArrayThrowsIllegalArgumentExceptionIfArgIsNull() {
		checkIsNotEmptyArrayThrowsIllegalArgumentExceptionOn(null);
	}

	public void testIsNotEmptyArrayThrowsIllegalArgumentExceptionIfArgIsEmpty() {
		checkIsNotEmptyArrayThrowsIllegalArgumentExceptionOn(new String[0]);
	}

	private static <T> void checkIsNotEmptyArrayThrowsIllegalArgumentExceptionOn(T[] array) {
		try {
			ArgCheck.isNotEmpty(array, ARG_NAME);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNotEmptyArrayException(ARG_NAME, ex);
		}
	}

	/**
	 * Checks that the exception <code>ex</code> was produced as the result of
	 * the failure of a call to {@link ArgCheck#isNotEmpty(Object[], String)} on
	 * the argument named <code>argName</code>.
	 */
	public static void assertIsNotEmptyArrayException(String argName, IllegalArgumentException ex) {
		String errMsg = "Argument array '" + argName + "' is null or empty.";
		assertEquals(errMsg, ex.getMessage());
	}

	// ============================================= isNotEmpty(Collection, String)

	public void testIsNotEmptyCollectionIsNoOpIfArgIsNonNullAndNonEmpty() {
		List<String> c = new ArrayList<String>();
		c.add("foo");
		ArgCheck.isNotEmpty(c, ARG_NAME);
	}

	public void testIsNotEmtpyCollectionThrowsIllegalArgumentExceptionIfArgIsNull() {
		checkIsNotEmptyCollectionThrowsIllegalArgumentExceptionOn(null);
	}

	public void testIsNotEmptyCollectionThrowsIllegalArgumentExceptionIfArgIsEmpty() {
		checkIsNotEmptyCollectionThrowsIllegalArgumentExceptionOn(new ArrayList<Object>());
	}

	private void checkIsNotEmptyCollectionThrowsIllegalArgumentExceptionOn(Collection<? extends Object> c) {
		try {
			ArgCheck.isNotEmpty(c, ARG_NAME);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertIsNotEmptyCollectionException(ARG_NAME, ex);
		}
	}

	/**
	 * Checks that the exception <code>ex</code> was produced as the result of
	 * the failure of a call to {@link ArgCheck#isNotEmpty(Collection, String)} on
	 * the argument named <code>argName</code>.
	 */
	public static void assertIsNotEmptyCollectionException(String argName, IllegalArgumentException ex) {
		String errMsg = "Argument Collection '" + argName + "' is null or empty.";
		assertEquals(errMsg, ex.getMessage());
	}

	// ============================================= isInInterval

	public void testIsInIntervalThrowsIfValueBelowLowerBound() {
		checkIsInIntervalThrowsIfValueBelowLowerBound(0, 1, 10);
		checkIsInIntervalThrowsIfValueBelowLowerBound(-1, 1, 10);
		checkIsInIntervalThrowsIfValueBelowLowerBound(5, 6, 10);
	}

	private void checkIsInIntervalThrowsIfValueBelowLowerBound(int val, int lo, int hi) {
		try {
			ArgCheck.isInInterval(val, "foo", lo, hi);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals(
				"Integer argument 'foo' has value " + val + " below lower bound " + lo,
				ex.getMessage());
		}
	}

	public void testIsInIntervalThrowsIfValueAtLeasetUpperBound() {
		checkIsInIntervalThrowsIfValueAtLeastUpperBound(10, 1, 10);
		checkIsInIntervalThrowsIfValueAtLeastUpperBound(11, 1, 10);
		checkIsInIntervalThrowsIfValueAtLeastUpperBound(5, 0, 5);
	}

	private void checkIsInIntervalThrowsIfValueAtLeastUpperBound(int val, int lo, int hi) {
		try {
			ArgCheck.isInInterval(val, "foo", lo, hi);
			fail();
		} catch (IllegalArgumentException ex) {
			assertEquals(
				"Integer argument 'foo' has value " + val + " at or above upper bound " + hi,
				ex.getMessage());
		}
	}

	public void testIsInIntervalIsNoOpForAllValuesInHalfOpenInterval() {
		final int LO = 2;
		final int HI = 17;
		for (int i = LO; i < HI; i++) {
			ArgCheck.isInInterval(i, "foo", LO, HI);
		}
	}

	// ============================================= isTrue

	public void testIsTrueIsNoOpIfArgIsTrue() {
		ArgCheck.isTrue(true, ERROR_MESSAGE);
	}

	public void testIsTrueThrowsIllegalArgumentExceptionIfArgIsFalse() {
		try {
			ArgCheck.isTrue(false, ERROR_MESSAGE);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals(ERROR_MESSAGE, ex.getMessage());
		}
	}

	// ============================================= isFalse

	public void testIsFalseIsNoOpIfArgIsFalse() {
		ArgCheck.isFalse(false, ERROR_MESSAGE);
	}

	public void testIsFalseThrowsIllegalArgumentExceptionIfArgIsTrue() {
		try {
			ArgCheck.isFalse(true, ERROR_MESSAGE);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			assertEquals(ERROR_MESSAGE, ex.getMessage());
		}
	}

	// ============================================= equals

	public void testEqualsIsNoOpIfArgsAreBothNull() {
		ArgCheck.equals(null, null, ERROR_MESSAGE);
	}

	public void testEqualsIsNoOpIfArgsAreSameReference() {
		Object obj = new Object();
		ArgCheck.equals(obj, obj, ERROR_MESSAGE);
	}

	public void testEqualsIsNoOpIfArgsAreEqualButDistinctObjects() {
		ArgCheck.equals(new Integer(10), new Integer(10), ERROR_MESSAGE);
	}

	public void testEqualsThrowsIllegalArgumentExceptionIfExpAndGotAreDifferent() {
		checkEqualsThrows(new Integer(1), null, "exp = non-null, got = null");
		checkEqualsThrows(null, new Integer(1), "exp = null, got = non-null");
		checkEqualsThrows(new Integer(1), new Integer(2), "exp = non-null, got = non-null");
	}

	private void checkEqualsThrows(Object exp, Object got, String msg) {
		try {
			ArgCheck.equals(exp, got, ERROR_MESSAGE);
			fail(msg);
		} catch (IllegalArgumentException ex) {
			assertEquals(makeEqualsErrorMessage(exp, got, ERROR_MESSAGE), ex.getMessage());
		}
	}

	private String makeEqualsErrorMessage(Object exp, Object got, String msg) {
		return msg + "; expected " + exp + ", got " + got;
	}
}
