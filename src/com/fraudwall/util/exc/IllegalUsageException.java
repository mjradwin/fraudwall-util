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
package com.fraudwall.util.exc;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.fraudwall.util.CommandDefinition;

public class IllegalUsageException extends Exception {
	private static final long serialVersionUID = 1L;

	public static final int EXIT_SUCCESS = 0;
	public static final int EXIT_ERROR = 1;

	private Options options;
	private String msg;
	private int code;

	public IllegalUsageException(CommandDefinition line) {
		this(line, EXIT_ERROR);
	}

	public IllegalUsageException(CommandDefinition line, int code) {
		this(line.getUsage(), line.getOptions(), code);
	}

	public IllegalUsageException(String msg, String usage, Options options, int code) {
		this(msg + "\n" + usage, options, code);
	}

	public IllegalUsageException(String msg, CommandDefinition line, int code) {
		this(msg, line.getUsage(), line.getOptions(), code);
	}

	public IllegalUsageException(String msg, Options options, int code) {
		this.options = options;
		this.msg = msg;
		this.code = code;
	}

	/**
	 * Print a useful help/usage message based on the specified options
	 *
	 * @param exit Calls System.exit() if exit is true
	 *
	 */
	public void printUsage(Boolean exit) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(msg, options);
		if (exit) {
			System.exit(code);
		}
	}

	/***
	 * Print a useful help/usage message based on the specified options and exits
	 */
	public void printUsageAndExit() {
		printUsage(true);
	}
}
