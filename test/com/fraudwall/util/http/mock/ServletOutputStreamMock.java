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
package com.fraudwall.util.http.mock;

import javax.servlet.ServletOutputStream;

public class ServletOutputStreamMock extends ServletOutputStream {

	public StringBuilder buffer = new StringBuilder();

	@Override
	public void print(boolean b) {
		buffer.append(b);
	}

	@Override
	public void print(char c) {
		buffer.append(c);
	}

	@Override
	public void print(double d) {
		buffer.append(d);
	}

	@Override
	public void print(float f) {
		buffer.append(f);
	}

	@Override
	public void print(int i) {
		buffer.append(i);
	}

	@Override
	public void print(long l) {
		buffer.append(l);
	}

	@Override
	public void print(String s) {
		buffer.append(s);
	}

	@Override
	public void println() {
		buffer.append("\n");
	}

	@Override
	public void println(boolean b) {
		buffer.append(b);
	}

	@Override
	public void println(char c) {
		buffer.append(c);
	}

	@Override
	public void println(double d) {
		buffer.append(d);
	}

	@Override
	public void println(float f) {
		buffer.append(f);
	}

	@Override
	public void println(int i) {
		buffer.append(i);
	}

	@Override
	public void println(long l) {
		buffer.append(l);
	}

	@Override
	public void println(String s) {
		buffer.append(s);
	}

	@Override
	public void write(int b) {
	}
}
