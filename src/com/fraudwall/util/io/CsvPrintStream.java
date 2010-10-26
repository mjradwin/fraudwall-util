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
/*
 * Copyright (c) 2007, Fraudwall Technologies. All rights reserved.
 */

package com.fraudwall.util.io;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An alternative to open source CSV writer classes. This version
 * handles NULL properly.
 */
public class CsvPrintStream
{
	public static String quote(String s)
	{
		if (s == null) {
			return "NULL";
		} else if (s.indexOf('"') != -1) {
			return "\"" + s.replace("\"", "\"\"") + "\"";
		} else if (s.indexOf(',') != -1) {
			return "\"" + s + "\"";
		} else {
			return s;
		}
	}

	public static void println(PrintStream ps, String strs[])
	{
		print(ps, strs);
		ps.println();
	}

	public static void print(PrintStream ps, String strs[])
	{
		if (strs.length > 0) {
			ps.print(quote(strs[0]));
			for (int i = 1; i < strs.length; i++) {
				ps.print(',');
				ps.print(quote(strs[i]));
			}
		}
	}

	public static void println(PrintWriter pw, String strs[])
	{
		print(pw, strs);
		pw.println();
	}

	public static void print(PrintWriter pw, String strs[])
	{
		if (strs.length > 0) {
			pw.print(quote(strs[0]));
			for (int i = 1; i < strs.length; i++) {
				pw.print(',');
				pw.print(quote(strs[i]));
			}
		}
	}
}

/*
 * Local variables:
 * tab-width: 4
 * c-basic-offset: 4
 * End:
 * vim600: noet sw=4 ts=4 fdm=marker
 * vim<600: noet sw=4 ts=4
 */

