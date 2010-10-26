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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fraudwall.util.io.IOUtils;

public class ShellCommandTest extends AbstractPropsTest {

	/** {@link ShellCommand#initProcessEnvironment(ProcessBuilder)} ***********************************************/

	public void testInitProcessEnvironmentUsesRootEnvironmentVariableIfSet() {
		ProcessBuilder proc = new ProcessBuilder();
		Map<String, String> env = proc.environment();
		String rootDir = new File(new File(IOUtils.getRootTmpDir(), "test"), "root").getPath();
		env.put("ROOT", rootDir);
		new ShellCommandMock(env, "ls").initProcessEnvironment(proc);
		assertEquals(rootDir, proc.environment().get("ROOT"));
	}

	public void testInitProcessEnvironmentSetsDefaultRootIfRootEnvironmentVariableNotSet() {
		ProcessBuilder proc = new ProcessBuilder();
		Map<String, String> env = proc.environment();
		env.remove("ROOT");
		new ShellCommandMock(env, "ls").initProcessEnvironment(proc);
		assertEquals("/var/fraudwall", proc.environment().get("ROOT"));
	}

	public void testInitProcessEnvironmentAugmentsPathFromConfigurationProperty() {
		String initPath = System.getenv("PATH");
		ProcessBuilder proc = new ProcessBuilder();
		new ShellCommandMock(null, "ls").initProcessEnvironment(proc);
		String newPath = proc.environment().get("PATH");
		assertTrue(newPath.startsWith(initPath));
//		assertEquals(File.pathSeparatorChar, newPath.charAt(initPath.length()));
//		String extraPathPropVal = FWProps.getStringProperty("shellTask.extraPath");
//		String expExtraPath = extraPathPropVal.replace(":", File.pathSeparator);
//		assertEquals(expExtraPath, newPath.substring(initPath.length() + 1));
	}

	public void testInitProcessEnvironmentSetsExtraEnvironmentVars() {
		ProcessBuilder proc = new ProcessBuilder();
		Map<String,String> envVars = new HashMap<String, String>();
		envVars.put("MANPATH", "/man:/usr/man");
		envVars.put("EDITOR", "emacs");
		ShellCommand task = new ShellCommandMock(envVars, "date");
		task.initProcessEnvironment(proc);
		Map<String, String> env = proc.environment();
		assertEquals("/man:/usr/man", env.get("MANPATH"));
		assertEquals("emacs", env.get("EDITOR"));
	}

	/** {@link ShellCommand#isLogLine(String)} ***********************************************/

	private static final String SIMPLE_LOG_LINE = "2008/12/10 22:06:14 [INFO] ShellTask - ....";
	public void testIsLogLineReturnsCorrectValuesForSimpleLog() {
		assertTrue(ShellCommand.isLogLine(SIMPLE_LOG_LINE));
		assertFalse(ShellCommand.isLogLine(" " + SIMPLE_LOG_LINE));
	}

	private static final String LOG4J_LOG_LINE = "[2008/12/10 22:06:14 INFO ShellTask.main] ....";
	public void testIsLogLineReturnsCorrectValuesForLog4j() {
		assertTrue(ShellCommand.isLogLine(LOG4J_LOG_LINE));
		assertFalse(ShellCommand.isLogLine(" " + LOG4J_LOG_LINE));
	}

	/** {@link ShellCommand#execute()} ***********************************************/

	public void testExecuteLogsErrorIfCommandReturnsNonZeroExitCode() throws Exception {
		ShellCommandExecutorMock command = new ShellCommandExecutorMock(null, "false");
		command.execute();
		String errMsg = "Command exited with status 1: sh -c false";
		assertListEquals(new String[] { errMsg }, command.messages);
	}

	public void testExecuteWritesOutputToStdOutStream() throws Exception {
		ShellCommandExecutorMock command = new ShellCommandExecutorMock(null, "echo", "hello world.");
		command.execute();
		assertListEquals(new String[] { "hello world." }, command.stdOutLines);
	}

	/** {@link ShellCommand#executeCommand(String...)} ***********************************************/

	public void testExecuteCommandReturnsExitCode() throws Exception {
		assertEquals(0, ShellCommand.executeCommand("true"));
		assertEquals(1, ShellCommand.executeCommand("false"));
	}

	// -------------------------------------- private helpers

	private static class ShellCommandExecutorMock extends ShellCommand.ShellCommandExecutor {
		private final List<String> stdOutLines = new ArrayList<String>();
		private final List<String> messages = new ArrayList<String>();

		public ShellCommandExecutorMock(Map<String, String> extraVars, String... commands) {
			super(extraVars, commands);
		}

		@Override
		protected void logError(String msg) {
			messages.add(msg);
		}

		@Override
		protected void processOutput(String line) {
			stdOutLines.add(line);
		}
	}


	/** {@link ShellCommand#getCommandOutput(String...)} ***********************************************/

	public void testExecuteCommandReturnsShellCommandOutput() throws Exception {
		assertEquals("hi", ShellCommand.getCommandOutput("echo hi"));
	}

	public void testExecuteCommandWhenOutputOnMultiLinesReturnsOutputOnMultiLines() throws Exception {
		assertEquals("hi\nthere", ShellCommand.getCommandOutput("echo \"hi\nthere\""));
	}

	// -------------------------------------- private helpers

	private static class ShellCommandMock extends ShellCommand {
		private final List<String> messages = new ArrayList<String>();

		public ShellCommandMock(Map<String, String> extraVars, String... commands) {
			super(extraVars, commands);
		}

		@Override
		protected void processOutput(String line) {
			// TODO Auto-generated method stub
		}

		@Override
		protected void logError(String msg) {
			messages.add(msg);
		}
	}
}
