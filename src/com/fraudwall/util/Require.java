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

import org.apache.commons.lang.StringUtils;

/**
 * Defines static methods for checking properties of program state.
 * Generally, if the argument does not satisfy the associated property,
 * these methods throw {@link IllegalStateException}.
 */
public final class Require {

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is null.
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isNotNull(Object arg, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (arg == null) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is non-null.
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isNull(Object arg, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (arg != null) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is false.
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isTrue(boolean arg, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (!arg) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is true.
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isFalse(boolean arg, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (arg) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is not empty.
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isEmpty(Collection<?> arg, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (!arg.isEmpty()) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is empty.
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isNotEmpty(Collection<?> arg, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (arg.isEmpty()) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is not blank.
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isBlank(String arg, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (!StringUtils.isBlank(arg)) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is blank.
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isNotBlank(String arg, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (StringUtils.isBlank(arg)) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Throws {@link IllegalStateException} if <code>arg</code> is
	 * not an instance of <code>expClass</code>
	 *
	 * @param msg
	 *            String to be used for the exception message.
	 */
	public static void isInstanceOf(Object arg, Class<?> expClass, String msg) {
		ArgCheck.isNotBlank(msg, "msg");
		if (! expClass.isInstance(arg)) {
			throw new IllegalStateException(msg);
		}
	}
}

