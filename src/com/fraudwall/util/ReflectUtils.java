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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for using Java reflection.
 *
 * @author Allan Heydon
 */
public abstract class ReflectUtils {
	private static final Log log = LogFactory.getLog(ReflectUtils.class);

	static void logError(Throwable e) {
		if (!Utilities.isCalledFromUnitTest()) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Instantiates and returns an object of the specified class by invoking its
	 * zero-argument constructor.
	 *
	 * @param classname
	 *            The fully-qualify name of the class to instantiate.
	 * @return The newly-instantiated class.
	 * @throws IllegalArgumentException if the class cannot be instantiated for any reason.
	 *
	 * @see Class#newInstance()
	 */
	public static Object newInstance(String classname) {
		try {
			Class<?> clazz = Class.forName(classname);
			return clazz.newInstance();
		} catch (ClassNotFoundException e) {
			ReflectUtils.handleNewInstanceException(classname, e, null);
		} catch (InstantiationException e) {
			ReflectUtils.handleNewInstanceException(classname, e, null);
		} catch (IllegalAccessException e) {
			ReflectUtils.handleNewInstanceException(classname, e, null);
		}
		return null; // make compiler happy
	}

	/**
	 * Instantiates and returns an object of the specified class, using a
	 * one-argument constructor.
	 *
	 * @param classname
	 *            The fully-qualified name of the class to instantiate.
	 * @param argType
	 *            The type of the single constructor argument.
	 * @param argValue
	 *            The value of the single constructor argument.
	 * @return The newly-constructed object.
	 * @throws IllegalArgumentException if the class cannot be instantiated for any reason.
	 *
	 * @see Class#getConstructor(Class...)
	 * @see Constructor#newInstance(Object...)
	 */
	public static Object newInstance(String classname, Class<?> argType, Object argValue) {
		return ReflectUtils.newInstance(classname, new Class<?>[] { argType }, new Object[] { argValue });
	}

	/**
	 * Instantiates and returns an object of the specified class, using an
	 * N-argument constructor.
	 *
	 * @param classname
	 *            The fully-qualified name of the class to instantiate.
	 * @param argTypes
	 *            The types of the constructor's arguments.
	 * @param argValues
	 *            The values of the constructor's arguments.
	 * @return The newly-constructed object.
	 * @throws IllegalArgumentException if the class cannot be instantiated for any reason.
	 *
	 * @see Class#getConstructor(Class...)
	 * @see Constructor#newInstance(Object...)
	 */
	public static Object newInstance(String classname, Class<?>[] argTypes, Object[] argValues) {
		try {
			Class<?> clazz = Class.forName(classname);
			Constructor<?> constructor = clazz.getConstructor(argTypes);
			return constructor.newInstance(argValues);
		} catch (NoSuchMethodException e) {
			ReflectUtils.handleNewInstanceException(classname, e, argTypes);
		} catch (ClassNotFoundException e) {
			ReflectUtils.handleNewInstanceException(classname, e, argTypes);
		} catch (InstantiationException e) {
			ReflectUtils.handleNewInstanceException(classname, e, argTypes);
		} catch (IllegalAccessException e) {
			ReflectUtils.handleNewInstanceException(classname, e, argTypes);
		} catch (IllegalArgumentException e) {
			ReflectUtils.handleNewInstanceException(classname, e, argTypes);
		} catch (InvocationTargetException e) {
			ReflectUtils.handleNewInstanceException(classname, e, argTypes);
		}
		return null; // make compiler happy
	}

	private static void handleNewInstanceException(String classname, Exception ex, Class<?>[] argTypes) {
		logError(ex);
		String constructorType = (argTypes == null ? 0 : argTypes.length) + "-argument";
		String errMsg = "Cannot instantiate class '" + classname + "' with " + constructorType + " constructor.";
		throw new IllegalArgumentException(errMsg, ex);
	}


	private static final Class<?>[] EMPTY_ARG_TYPES = new Class<?>[0];
	private static final Object[] EMPTY_ARG_VALUES = new Object[0];

	public static Object invokeStaticMethod(String className, String methodName) {
		return invokeStaticMethod(className, methodName, EMPTY_ARG_TYPES, EMPTY_ARG_VALUES);
	}

	public static Object invokeStaticMethod(
		String className, String methodName, Class<?>[] argTypes, Object[] argValues)
	{
		ArgCheck.isNotNull(argTypes, "argTypes");
		ArgCheck.isNotNull(argValues, "argValues");
		ArgCheck.equals(argTypes.length, argValues.length,
			"Number of argument values different from number of argument types.");
 		try {
			Class<?> cls = Class.forName(className);
			Method m = cls.getDeclaredMethod(methodName, argTypes);
			return m.invoke(null, argValues);
		} catch (ClassNotFoundException ex) {
			handleInvokeStaticMethodException(className, methodName, ex);
		} catch (NoSuchMethodException ex) {
			handleInvokeStaticMethodException(className, methodName, ex);
		} catch (IllegalAccessException ex) {
			handleInvokeStaticMethodException(className, methodName, ex);
		} catch (IllegalArgumentException ex) {
			handleInvokeStaticMethodException(className, methodName, ex);
		} catch (InvocationTargetException ex) {
			handleInvokeStaticMethodException(className, methodName, ex);
 		}
		return null;
	}

	private static void handleInvokeStaticMethodException(String className, String methodName, Exception ex) {
		logError(ex);
		String errMsg = "Error invoking static method '" + methodName + "' of class '" + className + "'.";
		throw new IllegalArgumentException(errMsg, ex);
	}
}
