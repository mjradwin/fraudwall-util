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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.fraudwall.util.db.AnchorResultSetTest;
import com.fraudwall.util.db.ConnectionPoolTest;
import com.fraudwall.util.db.DBConnectionUtilsTest;
import com.fraudwall.util.db.DBUtilsTest;
import com.fraudwall.util.db.SqlStatementIteratorTest;
import com.fraudwall.util.fp.FP64SaveInputTest;
import com.fraudwall.util.fp.FP64Test;
import com.fraudwall.util.io.AnchorCsvWriterTest;
import com.fraudwall.util.io.AnchorLineNumberReaderTest;
import com.fraudwall.util.io.IOUtilsTest;
import com.fraudwall.util.io.MultiThreadedLineNumberReaderTest;
import com.fraudwall.util.io.TimeGrainRotatingWriterTest;

@RunWith(Suite.class)
@SuiteClasses( {
	// fp/ sub-package
	FP64Test.class,
	FP64SaveInputTest.class,

	// io/ sub-package
	AnchorCsvWriterTest.class,
	AnchorLineNumberReaderTest.class,
	MultiThreadedLineNumberReaderTest.class,

	AnchorCollectionUtilsTest.class,
	AnchorResultSetTest.class,
	AnchorThreadTest.class,
	ApplicationRunnerTest.class,
	ArgCheckTest.class,
	RequireTest.class,
	BrowscapUserAgentTest.class,
	CanonicalUrlTest.class,
	ConnectionPoolTest.class,
	DateTimeUtilsTest.class,
	DBUtilsTest.class,
	DBConnectionUtilsTest.class,
	DecayVariableCollectionTest.class,
	EmailUtilsTest.class,
	EstCountTest.class,
	ExpiringLRUMapTest.class,
	HttpQueryTest.class,
	FWPropsTest.class,
	IndexedPriorityQueueTest.class,
	IOUtilsTest.class,
	IpAddressUtilsTest.class,
	IpAddressRangeTest.class,
	LRUMapTest.class,
	LRUSetTest.class,
	MathUtilitiesTest.class,
	PrimitiveUtilsTest.class,
	RangeTest.class,
	ReflectUtilsTest.class,
	ShellCommandTest.class,
	SmartDateParserTest.class,
	SqlStatementIteratorTest.class,
	TemplateExpanderTest.class,
	TimeGrainRotatingWriterTest.class,
	UtilitiesTest.class,
	XmlUtilitiesTest.class
})
public class UtilAllTests {

}
