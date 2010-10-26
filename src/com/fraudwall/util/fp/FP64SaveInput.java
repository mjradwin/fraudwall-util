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
package com.fraudwall.util.fp;

import java.io.IOException;
import java.io.Reader;

/**
 * Subclass of {@link FP64} <b>intended solely for debugging</b> that saves the
 * input stream that went into producing the fingerprint. This implementation
 * introduces performance overhead in both space and time: it stores the
 * complete text that went into producing this fingerprint in memory, and also
 * is somewhat slower than the base class due to the work required to maintain
 * the in-memory buffer.
 * <p>
 * 
 * The saved input is available via the {@link #getInput} method. For
 * readability, this method inserts a newline (as defined by the
 * "line.separator" system property) between each call to most versions of the
 * <code>extend</code> method. Hence, even though the following two
 * expressions produce an identical fingerprint:
 * <pre>
 * new FP64(&quot;aa&quot;).extend(&quot;aaaaaa&quot;)
 * new FP64(&quot;aaaaaa&quot;).extend(&quot;aa&quot;)
 * </pre>
 * the result returned by {@link #getInput} will in each case will be
 * slightly different:
 * <pre>
 * &quot;aa\naaaaaa\n&quot;
 * &quot;aaaaaa\naa\n&quot;
 * </pre>
 * 
 * In particular, a newline is appended to the saved input when the
 * following methods are called:
 * <ul>
 * <li>{@link #extend(String)}</li>
 * <li>{@link #extend(char[], int, int)}</li>
 * </ul>
 * Only character data is saved by this class. In particular, the arguments to
 * the following methods are <em>not</em> saved because they manipulate data
 * in the form of integer types like <em>byte</em> and <em>int</em>:
 * <ul>
 * <li>{@link #FP64SaveInput(byte[])}</li>
 * <li>{@link #FP64SaveInput(byte[], int, int)}</li>
 * <li>{@link #FP64SaveInput(Reader)}</li>
 * <li>{@link #extend(int)}</li>
 * <li>{@link #extend(byte)}</li>
 * <li>{@link #extend(byte[], int, int)}</li>
 * <li>{@link #extend(Reader)}</li>
 * </ul>
 * 
 * @author Allan Heydon
 */
@SuppressWarnings("serial")
public class FP64SaveInput extends FP64 {
	/*package*/static String NEWLINE = System.getProperty("line.separator");

	private final transient StringBuilder input = new StringBuilder();

	/**
	 * Returns the concatenation of all arguments passed to calls
	 * of the following methods that went into forming this fingerprint:
	 * <ul>
	 * <li>{@link #FP64SaveInput(FP64)} (if the argument was itself an FP64SaveInput instance)</li>
	 * <li>{@link #FP64SaveInput(String)}</li>
	 * <li>{@link #FP64SaveInput(char[])}</li>
	 * <li>{@link #FP64SaveInput(char[], int, int)}</li>
	 * <li>{@link #extend(String)}</li>
	 * <li>{@link #extend(char)}</li>
	 * <li>{@link #extend(char[], int, int)}</li>
	 * </ul>
	 * In the returned result, the value of the "line.separator" system
	 * property is appended to each of the arugments.
	 */
	public String getInput() {
		return input.toString();
	}

	public FP64SaveInput() {
	}

	public FP64SaveInput(FP64 fp) {
		super(fp);
		if (fp instanceof FP64SaveInput) {
			input.append(((FP64SaveInput) fp).input);
		}
	}

	public FP64SaveInput(String s) {
		extend(s);
	}

	public FP64SaveInput(char[] chars) {
		extend(chars, 0, chars.length);
	}

	public FP64SaveInput(char[] chars, int start, int length) {
		extend(chars, start, length);
	}

	public FP64SaveInput(byte[] bytes) {
		super(bytes);
	}

	public FP64SaveInput(byte[] bytes, int start, int length) {
		super(bytes, start, length);
	}

	public FP64SaveInput(Reader rd) throws IOException {
		extend(rd);
	}

	@Override
	public FP64 extend(String s) {
		super.extend(s);
		input.append(s);
		input.append(NEWLINE);
		return this;
	}

	@Override
	public FP64 extend(char[] chars, int start, int len) {
		super.extend(chars, start, len);
		input.append(NEWLINE);
		return this;
	}

	@Override
	public FP64 extend(char c) {
		super.extend(c);
		input.append(c);
		return this;
	}
}
