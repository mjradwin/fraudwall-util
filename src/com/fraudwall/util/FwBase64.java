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

import org.apache.commons.codec.binary.Base64;

/**
 * A slightly-modified version of Base64 encoding that is safe to use in
 * URLs and Cookies. Replaces the following 3 pairs of characters:
 *   + becomes .
 *   / becomes _
 *   = becomes -
 * Uses Jakarta Commons Codec library for actual Base64 conversions.
 */
public class FwBase64 {
	public static byte[] decode(byte[] base64Data, int offset, int length) {
		byte[] encoded = new byte[length];
		System.arraycopy(base64Data, offset, encoded, 0, length);
	
		for (int i = 0; i < encoded.length; i++) {
			if (encoded[i] == (byte)0x2E) {
			encoded[i] = (byte)0x2B; // "." => "+"
			} else if (encoded[i] == (byte)0x5F) {
			encoded[i] = (byte)0x2F; // "_" => "/"
			} else if (encoded[i] == (byte)0x2D) {
			encoded[i] = (byte)0x3D; // "-" => "="
			}
		}
	
		return Base64.decodeBase64(encoded);
	}

	public static byte[] decode(byte[] base64Data) {
		return decode(base64Data, 0, base64Data.length);
	}

	public static byte[] encode(byte[] binaryData, int offset, int length) {
		byte[] input;
		if (offset == 0 && length == binaryData.length) {
			input = binaryData;
		} else {
			input = new byte[length];
			System.arraycopy(binaryData, offset, input, 0, length);
		}
	
		byte[] encoded = Base64.encodeBase64(input);
	
		for (int i = 0; i < encoded.length; i++) {
			if (encoded[i] == (byte)0x2B) {
			encoded[i] = (byte)0x2E; // "+" => "."
			} else if (encoded[i] == (byte)0x2F) {
			encoded[i] = (byte)0x5F; // "/" => "_"
			} else if (encoded[i] == (byte)0x3D) {
			encoded[i] = (byte)0x2D; // "=" => "-"
			}
		}
	
		return encoded;
	}

	public static byte[] encode(byte[] binaryData) {
		return encode(binaryData, 0, binaryData.length);
	}
}
