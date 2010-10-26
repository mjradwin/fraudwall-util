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
import java.util.HashMap;
import java.util.Map;


/**
 * Tests the {@link TemplateExpander} implementation.
 *
 * @author ryan
 */
public class TemplateExpanderTest extends AbstractPropsTest {

	private TemplateExpander expander;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TemplateExpander.setSqlTemplateRootDir(getCreatedOutputDir());
		expander = new TemplateExpander();
	}

	//---------------------------------------------- expandLine()

	public void testExpandLineExpandsParamters() {
		Map<String,String> params = createParams(
				"$TABLE", "score_report",
				"$WHERE", "WHERE cuid=1");
		String template = "SELECT * FROM $TABLE $WHERE";
		assertEquals("SELECT * FROM score_report WHERE cuid=1", expander.expandLine(template, params));
	}

	public void testExpandLineExpandsLongestMatchFirst() {
		Map<String,String> params = createParams(
				"$A", "abc",
				"$AA", "123");
		String template = "$A $AA";
		assertEquals("abc 123", expander.expandLine(template, params));
	}

	public void testExpandLineExpandsLongestMatchFirst2() {
		Map<String,String> params = createParams(
				"$WEIGHT", "abc",
				"$WEIGHT_SQL", "123");
		String template = "$WEIGHT $WEIGHT_SQL";
		assertEquals("abc 123", expander.expandLine(template, params));
	}

	public void testExpandLineDoesNotExpandsParamLines() {
		Map<String,String> params = createParams(
				"$A", "abc",
				"$AA", "123");
		String template = "-- param $A $AA";
		assertEquals(template, expander.expandLine(template, params));
	}

	//---------------------------------------------- expandTemplate()

	public void testExpandTemplateExpandsTemplate() throws Exception {
		File template = createFileFromLines("foo.tmpl", "-- param $WHERE", "$WHERE");
		Map<String, String> params = createParams(
				"$TABLE", "score_report",
				"$WHERE", "WHERE cuid=1");
		File expandedFile = expander.expandTemplate(template, params);

		String[] expected = new String[] { "-- param $WHERE", "WHERE cuid=1" };
		assertFileEquals(expandedFile, expected);
	}

	//---------------------------------------------- private helpers

	private Map<String, String> createParams(String... params) {
		if (params.length % 2 != 0) {
			throw new AnchorFatalError("Odd number of params.");
		}
		Map<String,String> paramMap = new HashMap<String,String>();
		for (int i=0; i<params.length; i += 2) {
			paramMap.put(params[i], params[i+1]);
		}
		return paramMap;
	}

}
