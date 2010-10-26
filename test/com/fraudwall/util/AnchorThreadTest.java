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


public class AnchorThreadTest extends AbstractAnchorTest {

	private static final String THREAD_NAME = "test thread";

	// --------------------------------------------------------- run

	public void testRunLogsCaughtExceptions() throws Exception {
		AnchorThreadNoLogFatals th = new AnchorThreadNoLogFatals() {
			@Override public void Run() throws Exception {
				throw new AnchorFatalError("hit fatal error");
			}
		};
		th.start();
		th.join(5000L);
		assertEquals("AnchorThread \"" + THREAD_NAME + "\" caught Throwable", th.fatalMsgArg);
		assertEquals(AnchorFatalError.class, th.fatalThrowableArg.getClass());
		assertEquals("hit fatal error", th.fatalThrowableArg.getMessage());
		assertTrue(th.systemExitCalled);
	}

	// --------------------------------------------------------- AnchorThreadMock

	private static abstract class AnchorThreadNoLogFatals extends AnchorThread {
		private String fatalMsgArg;
		private Throwable fatalThrowableArg;
		private boolean systemExitCalled = false;

		private AnchorThreadNoLogFatals() {
			super(THREAD_NAME);
		}

		@Override
		protected void logFatal(String msg, Throwable e) {
			fatalMsgArg = msg;
			fatalThrowableArg = e;
		}

		@Override
		void systemExit() {
			systemExitCalled = true;
		}
	}
}
