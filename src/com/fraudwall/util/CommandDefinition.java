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

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.fraudwall.util.exc.IllegalUsageException;
import com.fraudwall.util.exc.Require;

/**
 * Simple wrapper around some Apache Commons CLI (command-line interface) classes.
 */
public class CommandDefinition {
	CommandLine line;
	Options options;
	String usage;

	/**
	 * Parses the command line arguments <code>args</code> according to the
	 * specified <code>options</code>, and returns a {@link CommandLine}
	 * object encapsulating the results.
	 * @param args
	 *            Command line arguments to parse
	 * @param options
	 *            Valid options
	 * @param usage
	 *            The usage string to display in the event of an error.
	 * @param helpOption
	 *            Value of the option that, if present, should generate help output.
	 */
	public CommandDefinition(String[] args, Options options, String usage, String helpOption) throws IllegalUsageException {
		this(args, options, usage, helpOption, /*stopAtNonOption=*/ false);
	}

	public CommandDefinition(String[] args, Options options, String usage, String helpOption, boolean stopAtNonOption)
		throws IllegalUsageException
	{
		this.options = options;
		this.usage = usage;
		CommandLineParser parser = new GnuParser();
		try {
			this.line = parser.parse(options, args, stopAtNonOption);
		} catch (org.apache.commons.cli.ParseException e) {
			throw new IllegalUsageException("Error: " + e.getMessage(), usage, options, 1);
		}
		if (helpOption != null && line.hasOption(helpOption)) {
			throw new IllegalUsageException(this);
		}
	}

	public CommandLine getCommandLine() {
		return line;
	}

	public Options getOptions() {
		return options;
	}

	public String getUsage() {
		return usage;
	}

	/** Returns true iff the given {@code option} was given on the command line. */
	public Boolean hasOption(String option) {
		return line.hasOption(option);
	}

	/** Returns the value of the given {@code option} if it is set; otherwise {@code null}. */
	public String getOptionValue(String option) {
		return line.getOptionValue(option);
	}

	/** Returns the value of the given {@code option} if it is set; otherwise {@code defaultValue}. */
	public String getOptionValue(String option, String defaultValue) {
		return line.getOptionValue(option, defaultValue);
	}

	/** Returns the value of the given {@code option} if it is set; otherwise throws {@link IllegalUsageException}. */
	public String getRequiredOption(String option) throws IllegalUsageException {
		String res = line.getOptionValue(option);
		if (res == null) {
			String message = "No -" + option + " option specified\n" + usage;
			throw new IllegalUsageException(message, this, (short)1);
		}
		return res;
	}

	public File getDirectoryOption(String optionName) throws IllegalUsageException {
		String dirName = getRequiredOption(optionName);
		File dir = new File(dirName);
		Require.isTrue(dir.isDirectory(),
			"Value for -" + optionName + " option does not name an existing directory: " + dirName);
		return dir;
	}

	public String[] getArgs() {
		return line.getArgs();
	}
}
