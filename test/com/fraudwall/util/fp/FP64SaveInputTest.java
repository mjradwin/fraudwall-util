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
import java.io.StringReader;

import com.fraudwall.util.AbstractAnchorTest;

/**
 * Tests the {@link FP64SaveInput} implementation.
 *
 * @author Allan Heydon
 */
public class FP64SaveInputTest extends AbstractAnchorTest {

	private static String NEWLINE = FP64SaveInput.NEWLINE;
	protected static final char[] CHAR_ARRAY = new char[] { 'a', 'b', 'c', 'd', 'e' };
	protected static final byte[] BYTE_ARRAY = new byte[] { 50, 51, 52, 53, 54, 55 };

	public void testNewlineIsNonEmpty() {
		assert (NEWLINE.length() > 0);
	}

	// ------------------------------------------------------------------ constructor

	public void testConstructorNoArgsHasSameValueAsBaseClass() {
		assertEquals(new FP64().getValue(), new FP64SaveInput().getValue());
	}

	public void testConstructorNoArgsHasEmptyInput() {
		assertEquals("", new FP64SaveInput().getInput());
	}

	public void testConstructorOnOtherFPCopiesInput() {
		FP64SaveInput fp = new FP64SaveInput("test");
		fp.extend("another").extend("string");
		assertEquals(fp.getInput(), new FP64SaveInput(fp).getInput());
	}

	public void testConstructorOnStringSavesStringInputWithNewline() {
		assertEquals("abcde" + NEWLINE, new FP64SaveInput("abcde").getInput());
	}

	public void testConstructorOnCharArraySavesCharsInputWithNewline() {
		assertEquals("abcde" + NEWLINE, new FP64SaveInput(CHAR_ARRAY).getInput());
	}

	public void testConstructorOnCharSubarraySavesCharsInputWithNewline() {
		assertEquals("bcd" + NEWLINE, new FP64SaveInput(CHAR_ARRAY, 1, 3).getInput());
	}

	public void testConstructorOnByteArraySavesNothing() {
		assertEquals("", new FP64SaveInput(BYTE_ARRAY).getInput());
	}

	public void testConstructorOnByteSubarraySavesNothing() {
		assertEquals("", new FP64SaveInput(BYTE_ARRAY, 1, 3).getInput());
	}

	public void testConstructorOnReaaderSavesNothing() throws Exception {
		Reader rd = new StringReader("abcde");
		assertEquals("", new FP64SaveInput(rd).getInput());
	}

	// ------------------------------------------------------------------ extend

	public void testExtendOnCharSavesChar() {
		FP64SaveInput fp64 = (FP64SaveInput) new FP64SaveInput().extend('a');
		assertEquals("a", fp64.getInput());
	}

	public void testExtendOnStringSavesStringInputWithNewline() {
		FP64SaveInput fp64 = new FP64SaveInput();
		fp64.extend("abcde");
		assertEquals("abcde" + NEWLINE, fp64.getInput());
	}

	public void testExtendOnCharSubarraySavesCharsInputWithNewline() {
		FP64SaveInput fp64 = new FP64SaveInput();
		fp64.extend(CHAR_ARRAY, 1, 3);
		assertEquals("bcd" + NEWLINE, fp64.getInput());
	}

	public void testExtendOnByteSavesNothing() {
		FP64SaveInput fp64 = new FP64SaveInput();
		fp64.extend(BYTE_ARRAY[0]);
		assertEquals("", fp64.getInput());
	}

	public void testExtendOnByteSubarraySavesNothing() {
		FP64SaveInput fp64 = new FP64SaveInput();
		fp64.extend(BYTE_ARRAY, 1, 3);
		assertEquals("", fp64.getInput());
	}

	public void testExtendOnIntSavesNothing() {
		FP64SaveInput fp64 = new FP64SaveInput();
		fp64.extend(2040);
		assertEquals("", fp64.getInput());
	}

	public void testExtendOnReaderSavesNothing() throws IOException {
		Reader rd = new StringReader("abcde");
		FP64SaveInput fp64 = new FP64SaveInput();
		fp64.extend(rd);
		assertEquals("", fp64.getInput());
	}
}
