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

import java.io.File;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Defines static methods for checking properties of method
 * arguments. Generally, if the argument does not satisfy the
 * associated property, these methods throw
 * {@link IllegalArgumentException}.
 * <p>
 * Example:
 * <pre>
 * public void exampleMethod(Object obj, Object[] objs, String errorMsg) {
 *     ArgCheck.isNotNull(obj, "obj");
 *     ArgCheck.isTrue(objs.length > 0, "objs array expected to be non-empty");
 *     ArgCheck.isNotEmpty(errorMsg, "errorMsg");
 *     // rest of method body here...
 * }
 * </pre>
 *
 * @author Allan Heydon
 */
public final class ArgCheck {
	// make zero-argument constructor private so this class cannot be instantiated
	private ArgCheck() { }

	/**
	 * Throws {@link IllegalArgumentException} if <code>arg</code> is null.
	 *
	 * @param argName
	 *            Name of the argument to be used in the error message. This
	 *            argument must be non-null and non-empty.
	 */
	public static void isNotNull(Object arg, String argName) {
		isNotBlankNoArgNameCheck(argName, "argName");
		if (arg == null) {
			throw new IllegalArgumentException(
				"Argument '" + argName + "' expected to be non-null.");
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if <code>arg</code> is non-null.
	 *
	 * @param argName
	 *            Name of the argument to be used in the error message. This
	 *            argument must be non-null and non-empty.
	 */
	public static void isNull(Object arg, String argName) {
		isNotBlankNoArgNameCheck(argName, "argName");
		if (arg != null) {
			throw new IllegalArgumentException("Argument '" + argName + "' expected to be null.");
		}
	}


	/**
	 * Throws {@link IllegalArgumentException} if the array <code>arrayArg</code>
	 * is null or empty.
	 *
	 * @param argName
	 *            Name of the argument to be used in the error message. This
	 *            argument must be non-null and non-empty.
	 *
	 * @see ArrayUtils#isEmpty(Object[])
	 */
	public static <T> void isNotEmpty(T[] arrayArg, String argName) {
		isNotBlankNoArgNameCheck(argName, "argName");
		if (ArrayUtils.isEmpty(arrayArg)) {
			throw new IllegalArgumentException("Argument array '" + argName + "' is null or empty.");
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if the Collection <code>c</code>
	 * is null or empty.
	 *
	 * @param argName
	 *            Name of the argument to be used in the error message. This
	 *            argument must be non-null and non-empty.
	 *
	 * @see CollectionUtils#isEmpty
	 */
	public static void isNotEmpty(Collection<? extends Object> c, String argName) {
		isNotBlankNoArgNameCheck(argName, "argName");
		if (CollectionUtils.isEmpty(c)) {
			throw new IllegalArgumentException("Argument Collection '" + argName + "' is null or empty.");
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if the String <code>s</code>
	 * is null, empty, or contains only whitespace characters.
	 *
	 * @param argName
	 *            Name of the argument to be used in the error message. This
	 *            argument must be non-null and non-empty.
	 *
	 * @see StringUtils#isBlank
	 */
	public static void isNotBlank(String s, String argName) {
		isNotBlankNoArgNameCheck(argName, "argName");
		isNotBlankNoArgNameCheck(s, argName);
	}

	private static void isNotBlankNoArgNameCheck(String s, String argName) {
		if (StringUtils.isBlank(s)) {
			throw new IllegalArgumentException(
				"String argument '" + argName + "' is null, empty, or all whitespace characters.");
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if the integer value
	 * <code>val</code> (corresponding to the method argument named
	 * <code>argName</code>) is not contained in the half-open interval
	 * [<code>lo</code>, <code>hi</code>). That is, the exception
	 * is thrown if <code>val</code> does not satisfy:
	 * <pre>lo <= val < hi</pre>
	 *
	 * @param val The value being tested.
	 * @param lo The lower bound (inclusive).
	 * @param hi The upper bound (exclusive).
	 */
	public static void isInInterval(int val, String argName, int lo, int hi) {
		if (val < lo) {
			throw new IllegalArgumentException(
				"Integer argument '" + argName + "' has value " + val
				+ " below lower bound " + lo);
		}
		if (val >= hi) {
			throw new IllegalArgumentException(
				"Integer argument '" + argName + "' has value " + val
				+ " at or above upper bound " + hi);
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if the float value
	 * <code>val</code> (corresponding to the method argument named
	 * <code>argName</code>) is not contained in the closed interval
	 * [<code>lo</code>, <code>hi</code>]. That is, the exception
	 * is thrown if <code>val</code> does not satisfy:
	 * <pre>lo <= val <= hi</pre>
	 *
	 * @param val The value being tested.
	 * @param lo The lower bound (inclusive).
	 * @param hi The upper bound (exclusive).
	 */
	public static void isInClosedInterval(float val, String argName, float lo, float hi) {
		if (val < lo) {
			throw new IllegalArgumentException(
				"Float argument '" + argName + "' has value " + val
				+ " below lower bound " + lo);
		}
		if (val > hi) {
			throw new IllegalArgumentException(
				"Float argument '" + argName + "' has value " + val
				+ " above upper bound " + hi);
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if the boolean value
	 * <code>b</code> is false. This should generally be a predicate of one
	 * or more of the method arguments being checked.
	 *
	 * @param errorMsg
	 *            Error message supplied to the IllegalArgumentException
	 *            constructor.
	 */
	public static void isTrue(boolean b, String errorMsg) {
		isNotBlankNoArgNameCheck(errorMsg, "errorMsg");
		if (!b) {
			throw new IllegalArgumentException(errorMsg);
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if the boolean value
	 * <code>b</code> is true. This should generally be a predicate of one
	 * or more of the method arguments being checked.
	 *
	 * @param errorMsg
	 *            Error message supplied to the IllegalArgumentException
	 *            constructor.
	 */
	public static void isFalse(boolean b, String errorMsg) {
		isNotBlankNoArgNameCheck(errorMsg, "errorMsg");
		if (b) {
			throw new IllegalArgumentException(errorMsg);
		}
	}

	/**
	 * Throws {@link IllegalArgumentException} if the actual argument value
	 * <code>got</code> differs from the expected value <code>exp</code>.
	 * This method is null-tolerant; it uses {@link ObjectUtils#equals} to do
	 * the comparison.
	 *
	 * @param exp
	 *            Expected value (may be null).
	 * @param got
	 *            Actual argument value (may be null).
	 * @param msg
	 *            Message describing the problem. This message does not need to
	 *            include either the expected or actual values; those will be
	 *            automatically appended to <code>msg</code> as part of the
	 *            IllegalArgumentException error message.
	 */
	public static void equals(Object exp, Object got, String msg) {
		if (!ObjectUtils.equals(exp, got)) {
			throw new IllegalArgumentException(msg + "; expected " + exp + ", got " + got);
		}
	}


	/**
	 * Throws {@link IllegalArgumentException} if the actual argument value
	 * <code>got</code> equals the disallowed value <code>disallowed</code>.
	 * This method is null-tolerant; it uses {@link ObjectUtils#equals} to do
	 * the comparison.
	 *
	 * @param disallowed
	 *            Disallowed value (may be null).
	 * @param got
	 *            Actual argument value (may be null).
	 * @param msg
	 *            Message describing the problem. This message does not need to
	 *            include either the disallowed value; that will be
	 *            automatically appended to <code>msg</code> as part of the
	 *            IllegalArgumentException error message.
	 */
	public static void doesNotEqual(Object disallowed, Object got, String msg) {
		if (ObjectUtils.equals(disallowed, got)) {
			throw new IllegalArgumentException(msg + "; value = " + got);
		}
	}

	public static void isExistingDirectory(File file, String argName) {
		isNotBlankNoArgNameCheck(argName, "argName");
		ArgCheck.isNotNull(file, argName);
		ArgCheck.isTrue(file.exists(), "Argument '" + argName + "' does not exist " + file);
		ArgCheck.isTrue(file.isDirectory(), "Argument '" + argName + "' is not a directory " + file);
		ArgCheck.isTrue(file.canRead(), "Argument '" + argName + "' is not readable " + file);
	}
}
