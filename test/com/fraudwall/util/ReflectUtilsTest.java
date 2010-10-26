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

import java.lang.reflect.InvocationTargetException;

import com.fraudwall.util.fp.FP64;

/**
 * Tests the {@link ReflectUtils} implementation.
 *
 * @author Allan Heydon
 */
public class ReflectUtilsTest extends AbstractAnchorTest {

	// =============================================== newInstance(String)

	public void testNewInstanceNoArgConstructorThrowsIfClassNotFound() {
		checkNewInstanceNoArgConstructorThrows(
			"com.fraudwall.util.FooBar", ClassNotFoundException.class);
	}

	public void testNewInstanceNoArgConstructorThrowsIfClassHasNoNoArgConstructor() {
		checkNewInstanceNoArgConstructorThrows(
			"com.fraudwall.util.IllegalUsageException", InstantiationException.class);
	}

	public void testNewInstanceNoArgConstructorThrowsIfClassHasPrivateNoArgConstructor() {
		checkNewInstanceNoArgConstructorThrows(
			"com.fraudwall.util.ArgCheckTest", IllegalAccessException.class);
	}

	private void checkNewInstanceNoArgConstructorThrows(
		String className, Class<? extends Exception> nestedExceptionClass)
	{
		try {
			ReflectUtils.newInstance(className);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			String errMsg = "Cannot instantiate class '" + className + "' with 0-argument constructor.";
			assertEquals(errMsg, ex.getMessage());
			assertTrue(nestedExceptionClass.isInstance(ex.getCause()));
		}
	}

	public void testNewInstanceNoArgConstructorCreatesNewObjectInstance() {
		assertInstanceOf(ReflectUtils.newInstance(FP64.class.getName()), FP64.class);
	}

	// =============================================== newInstance(String,Class,Object)

	public void testNewInstanceOneArgConstructorThrowsIfClassNotFound() {
		checkNewInstanceOneArgConstructorThrows(
			"com.fraudwall.util.FooBar", String.class, "", ClassNotFoundException.class);
	}

	public void testNewInstanceOneArgConstructorThrowsIfNoSuchMethod() {
		checkNewInstanceOneArgConstructorThrows(
			"com.fraudwall.util.fp.FP64", Integer.class, null, NoSuchMethodException.class);
	}

	public void testNewInstanceOneArgConstructorThrowsIfIllegalArgumentException() {
		checkNewInstanceOneArgConstructorThrows(
			"com.fraudwall.util.ArgCheckTest", String.class, new Integer(0),
			IllegalArgumentException.class);
	}

	public void testNewInstanceOneArgConstructorThrowsIfInvocationTargetException() {
		checkNewInstanceOneArgConstructorThrows(
			"com.fraudwall.util.ArgCheckTest", Integer.class, null,
			InvocationTargetException.class);
	}

	private void checkNewInstanceOneArgConstructorThrows(
		String className, Class<?> argType, Object argValue,
		Class<? extends Exception> nestedExceptionClass)
	{
		try {
			ReflectUtils.newInstance(className, argType, argValue);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			String errMsg = "Cannot instantiate class '" + className + "' with 1-argument constructor.";
			assertEquals(errMsg, ex.getMessage());
			assertEquals(nestedExceptionClass, ex.getCause().getClass());
		}
	}

	public void testNewInstanceOneArgConstructorCreatesNewObjectInstance() {
		Object obj = ReflectUtils.newInstance(FP64.class.getCanonicalName(), String.class, "abcdefg");
		assertInstanceOf(obj, FP64.class);
		assertEquals(new FP64("abcdefg"), obj);
	}

	// =============================================== newInstance(String,Class[],Object[])

	public void testNewInstanceMultiArgConstructorThrowsIfClassNotFound() {
		checkNewInstanceMultiArgConstructorThrows(
			"com.fraudwall.util.FooBar",
			new Class<?>[] { String.class, Boolean.class },
			new Object[] { "", Boolean.TRUE }, ClassNotFoundException.class);
	}

	public void testNewInstanceMultiArgConstructorThrowsIfNoSuchMethod() {
		checkNewInstanceMultiArgConstructorThrows(
			"com.fraudwall.util.fp.FP64",
			new Class<?>[] { String.class, String.class },
			new Object[] { "foo", "bar" }, NoSuchMethodException.class);
	}

	public void testNewInstanceMultiArgConstructorThrowsIfIllegalArgumentException() {
		checkNewInstanceMultiArgConstructorThrows(
			"com.fraudwall.util.ArgCheckTest",
			new Class<?>[] { String.class, Integer.class },
			new Object[] { new Integer(0), new Integer(1) },
			IllegalArgumentException.class);
	}

	public void testNewInstanceMultiArgConstructorThrowsIfInvocationTargetException() {
		checkNewInstanceMultiArgConstructorThrows(
			"com.fraudwall.util.ArgCheckTest",
			new Class<?>[] { String.class, Integer.class },
			new Object[] { "ArgCheckTest", null },
			InvocationTargetException.class);
	}

	private void checkNewInstanceMultiArgConstructorThrows(
		String className, Class<?>[] argTypes, Object[] argValues,
		Class<? extends Exception> nestedExceptionClass)
	{
		try {
			ReflectUtils.newInstance(className, argTypes, argValues);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			String errMsg = "Cannot instantiate class '" + className + "' with 2-argument constructor.";
			assertEquals(errMsg, ex.getMessage());
			assertEquals(nestedExceptionClass, ex.getCause().getClass());
		}
	}

	public void testNewInstanceMultiArgConstructorCreatesNewObjectInstance() {
		Object obj = ReflectUtils.newInstance(
			FP64.class.getCanonicalName(), new Class<?>[] { String.class }, new Object[] { "abcdefg" });
		assertInstanceOf(obj, FP64.class);
		assertEquals(new FP64("abcdefg"), obj);
	}

	// =============================================== invokeStaticMethod


	public void testInvokeStaticMethodThrowsIfClassNotFound() {
		checkInvokeStaticMethodThrows(
			"com.fraudwall.util.FooBar", "sampleStaticMethod",
			new Class<?>[] { String.class, Boolean.class },
			new Object[] { "", Boolean.TRUE }, ClassNotFoundException.class);
	}

	public void testInvokeStaticMethodThrowsIfNoSuchMethod() {
		checkInvokeStaticMethodThrows(
			this.getClass().getName(), "noSuchMethod",
			new Class<?>[] { String.class, String.class },
			new Object[] { "foo", "bar" }, NoSuchMethodException.class);
	}

	public void testInvokeStaticMethodThrowsIfIllegalArgumentException() {
		checkInvokeStaticMethodThrows(
			this.getClass().getName(), "sampleStaticMethod",
			new Class<?>[] { Integer.class, String.class },
			new Object[] { new Integer(0), new Integer(1) },
			IllegalArgumentException.class);
	}

	public void testInvokeStaticMethodThrowsIfInvocationTargetException() {
		checkInvokeStaticMethodThrows(
			this.getClass().getName(), "sampleStaticMethod",
			new Class<?>[] { Integer.class, String.class },
			new Object[] { null, "bar" },
			InvocationTargetException.class);
	}

	private void checkInvokeStaticMethodThrows(
		String className, String methodName, Class<?>[] argTypes, Object[] argValues,
		Class<? extends Exception> nestedExceptionClass)
	{
		try {
			ReflectUtils.invokeStaticMethod(className, methodName, argTypes, argValues);
			fail();
		} catch (IllegalArgumentException ex) {
			// expected case
			String errMsg = "Error invoking static method '" + methodName + "' of class '"+ className + "'.";
			assertEquals(errMsg, ex.getMessage());
			assertEquals(nestedExceptionClass, ex.getCause().getClass());
		}
	}

	public void testInvokeStaticMethodInvokesStaticMethodAsExpectedAndReturnsCorrectResult() {
		String res = (String) ReflectUtils.invokeStaticMethod(
			this.getClass().getName(), "sampleStaticMethod",
			new Class<?>[] { Integer.class, String.class },
			new Object[] { new Integer(2), "bar" });
		assertEquals("foo-2-bar", res);
	}

	public static String sampleStaticMethod(Integer x, String s) {
		return "foo-" + x.intValue() + "-" + s;
	}

}
