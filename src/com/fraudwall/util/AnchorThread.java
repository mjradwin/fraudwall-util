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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for Anchor Intelligence threads that provides support for
 * catching any thrown {@link Throwable}s, logging them, and shutting
 * down the system.
 *
 * @author Ryan Hamilton
 */
public abstract class AnchorThread extends Thread {

	private static final Log log = LogFactory.getLog(AnchorThread.class);

	private static final String EXIT_ON_EXCEPTION_PROP_NAME = "anchorThread.exitOnException";

	private /*final*/ boolean exitOnException;

	/**
	 * Constructs a new thread with the given thread name.
	 */
	public AnchorThread(String threadName) {
		super(threadName);
		init();
	}

	/**
	 * Constructs a new thread that uses this class's name for
	 * the thread name. This is sufficient to distinguish the thread
	 * if only one instance of it is being created. If you are creating
	 * multiple instances, you probably want to set the name explicitly
	 * using the other constructor.
	 *
	 * @see #AnchorThread(String)
	 */
	public AnchorThread() {
		super();
		setName(getClass().getSimpleName());
		init();
	}

	private void init() {
		exitOnException = FWProps.getBooleanProperty(EXIT_ON_EXCEPTION_PROP_NAME);
	}

	/**
	 * This method is called from {@link Thread#start()} and it in turn invokes
	 * {@link #Run()}, catching any thrown exceptions. If it encounters any
	 * exceptions (whether checked or unchecked), it will log them and then exit
	 * the system with a non-zero status code. This allows non-main threads that
	 * throw RuntimeExceptions (or any other type of {@link Throwable}) to
	 * terminate the application instead of simply the thread.
	 *
	 * @see #Run
	 */
	@Override
	public final void run() {
		try {
			Run();
		} catch (Throwable e) {
			String msg = "AnchorThread \"" + getName() + "\" caught Throwable";
			handleException(msg, e);
		}
	}

	/**
	 * If {@link #EXIT_ON_EXCEPTION_PROP_NAME} is true this logs a fatal error
	 * and exits; otherwise logs an error and returns.
	 */
	public void handleException(String msg, Throwable e) {
		if (exitOnException) {
			logFatal(msg, e);
			systemExit();
		} else {
			log.error(msg, e);
		}
	}

	/** Template method for invoking System.exit(1) than can be overridden from the unit test. */
	/*test*/ void systemExit() {
		// DO NOT CHANGE THIS METHOD BODY!
		System.exit(1);
	}

	/**
	 * Template method that can be overridden from unit tests to prevent any actual
	 * logging.
	 */
	protected void logFatal(String msg, Throwable e) {
		log.fatal(msg, e);
	}

	/**
	 * Abstract method to be overridden by sub-classes to do the actual work of
	 * this thread. Any exception thrown by this method is caught by the
	 * {@link #run} method, logged as fatal, has its stack trace written to the
	 * standard error output, and causes the program to immediately exit with a
	 * non-zero status code.
	 *
	 * @see #run
	 */
	public abstract void Run() throws Exception;

	/**
	 * Causes the calling thread to sleep for <code>millis</code>,
	 * even if it is interrupted.
	 */
	public static void sleepThroughInterrupts(long millis) {
		long start = System.currentTimeMillis();
		long sleptMillis = 0;
		do {
			try {
				Thread.sleep(millis - sleptMillis);
			} catch (InterruptedException ex) {
				// continue
			}
			sleptMillis = System.currentTimeMillis() - start;
		} while (sleptMillis < millis);
	}
}
