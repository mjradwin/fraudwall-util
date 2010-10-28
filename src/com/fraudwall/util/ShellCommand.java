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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fraudwall.util.exc.AnchorFatalError;
import com.fraudwall.util.exc.ArgCheck;

/**
 * Class for running command from the system's shell.  For simply running a command:
 *  {@link #executeCommand(String...)} or {@link #executeCommand(Map, String...)}
 * For running a command and capturing the output:
 *  {@link #getCommandOutput(String...)} or {@link #getCommandOutput(Map, String...)}
 */
public abstract class ShellCommand {

	protected final Log log = LogFactory.getLog(ShellCommand.class);

	private static final String ROOT = "ROOT";

	public static void exec(String... commands) {
		try {
			int exitCode = executeCommand(commands);
			if (exitCode != 0) {
				throw new AnchorFatalError("command returned exit code=" + exitCode + ":" + StringUtils.join(commands, " "));
			}
		} catch (InterruptedException e) {
			throw new AnchorFatalError("command generated an exception:" + StringUtils.join(commands, " "), e);
		} catch (IOException e) {
			throw new AnchorFatalError("command generated an exception:" + StringUtils.join(commands, " "), e);
		}
	}

	/**
	 * Executes the shell command <code>commands</code>, and returns the exit code.
	 */
	public static int executeCommand(String ... commands) throws InterruptedException, IOException {
		return executeCommand(null, commands);
	}

	/**
	 * Executes the shell command <code>commands</code>, and returns the exit code.
	 * Each entry in <code>extraVars</code> will be added to the environment
	 * of the spawned process.
	 */
	public static int executeCommand(Map<String, String> extraVars, String ... commands)
		throws InterruptedException, IOException
	{
		return new ShellCommandExecutor(extraVars, commands).execute();
	}

	/** This is not anonymous so we can instantiate sub classes for testing. */
	/*test*/ static class ShellCommandExecutor extends ShellCommand {
		/*test*/ ShellCommandExecutor(Map<String, String> extraVars, String[] commands) {
			super(extraVars, commands);
		}

		/**
		 * Process the output line.  If this line appears to already have a log line
		 * prefix, then we will simply print it. If it appears to be a
		 * MySQL ERROR then we log.error() it. If it does not have a prefix
		 * then we will log.info() it.  This template method can be overridden
		 * by a test to record output.
		 */
		@Override
		protected void processOutput(String line) {
			if (isLogLine(line)) {
				System.err.println(line);
			} else if (line.startsWith("ERROR")) {
				logError(line);
			} else if (line.startsWith("WARNING")) {
				log.warn(line);
			} else {
				log.info(line);
			}
		}
	}

	/**
	 * Returns true if this line is already a log line (in which case we
	 * should not re-prefix the line with the log prefix).
	 */
	/*test*/ static boolean isLogLine(String line) {
		int offset = line.startsWith("[") ? 1 : 0;
		return line.length() > 17 &&
			line.charAt(offset+4) == '/' &&
			line.charAt(offset+7) == '/' &&
			line.charAt(offset+13) == ':' &&
			line.charAt(offset+16) == ':';
	}

	/**
	 * Executes the shell command <code>commands</code>, and returns a
	 * new string containing the output from STDOUT and STDERR.
	 */
	public static String getCommandOutput(String ... commands) throws InterruptedException, IOException {
		return getCommandOutput(null, commands);
	}

	/**
	 * Executes the shell command <code>commands</code>, and returns a
	 * new string containing the output from STDOUT and STDERR.
	 * Each entry in <code>extraVars</code> will be added to the environment
	 * of the spawned process.
	 */
	public static String getCommandOutput(Map<String, String> extraVars, String ... commands)
		throws InterruptedException, IOException
	{
		final StringBuilder output = new StringBuilder();
		ShellCommand cmd = new ShellCommand(extraVars, commands) {
			@Override protected void processOutput(String line) {
				if (output.length() > 0) {
					output.append("\n");
				}
				output.append(line);
			}
		};
		cmd.execute();
		return output.toString();
	}

	private final Map<String, String> extraVars;
	private final String[] commands;

	/*test*/ ShellCommand(String ... commands) {
		this(null, commands);
	}

	/*test*/ ShellCommand(Map<String, String> extraVars, String ... commands) {
		ArgCheck.isTrue(commands.length > 0, "No commands specified to ShellExecute constructor.");
		this.extraVars = extraVars;
		if (commands.length == 1) {
			this.commands = new String[] {"sh", "-c", commands[0]};
		} else {
			this.commands = commands;
		}
	}

	public int execute() throws InterruptedException, IOException {
		ProcessBuilder command = new ProcessBuilder(commands);
		command.redirectErrorStream(true);
		initProcessEnvironment(command);
		log.info("Running: " + this);
		Process process = command.start();
		drainInBackground(process.getInputStream()).join();
		int code = process.waitFor();
		if (code != 0) {
			logError("Command exited with status " + code + ": " + this);
		}
		if (log.isDebugEnabled()) {
			log.debug("Complete: " + this);
		}
		return code;
	}

	private Thread drainInBackground(final InputStream is) {
		Thread thread = new Thread(new Runnable() {
			private BufferedReader reader;
			public void run() {
				// force a small buffer size so we can get output more quickly
				reader = new BufferedReader(new InputStreamReader(is), 80);
				try {
					while (true) {
						String line = reader.readLine();
						if (line == null) {
							break;
						}
						processOutput(line);
					}
				} catch(IOException e) {
					throw new AnchorFatalError("Unable to drain output.", e);
				}
			}
		});
		thread.setName(Thread.currentThread().getName());
		thread.start();
		return thread;
	}

	protected abstract void processOutput(String line) throws IOException;

	/*test*/ void initProcessEnvironment(ProcessBuilder command) {
		Map<String, String> env = command.environment();

		// set the default "ROOT" if not set
		if (env.get(ROOT) == null) {
			env.put(ROOT, "/var/fraudwall");
		}

		// add to the PATH based on configuration property
		StringBuilder path = new StringBuilder(System.getenv("PATH"));
		String extraPath = FWProps.getStringProperty("shellTask.extraPath");
		if (StringUtils.isNotBlank(extraPath)) {
			for (String dir : extraPath.split(":")) {
				path.append(File.pathSeparator).append(dir);
			}
		}
		env.put("PATH", path.toString());
		if (extraVars != null) {
			for (String var : extraVars.keySet()) {
				env.put(var, extraVars.get(var));
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("ROOT=" + env.get(ROOT));
		}
	}


	/** Method for logging an error. Intended to be overridden by tests. */
	protected void logError(String msg) {
		log.error(msg);
	}

	@Override
	public String toString() {
		return StringUtils.join(commands, " ");
	}

}
