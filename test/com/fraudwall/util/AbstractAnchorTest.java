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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fraudwall.util.exc.ArgCheck;
import com.fraudwall.util.exc.ArgCheckTest;
import com.fraudwall.util.io.AnchorLineNumberReader;
import com.fraudwall.util.io.IOUtils;

/**
 * Abstract base class for all Anchor Intelligence unit tests.
 * Defines protected methods useful in unit tests not defined
 * by the base JUnit framework.
 *
 * @author Allan Heydon
 */
public abstract class AbstractAnchorTest extends TestCase {
	/**
	 * Name of a customer to be passed by unit tests for the "customer"
	 * parameter to {@link AbstractPropsTest#setUp(String, String)}. This
	 * constant should <em>not</em> be set to the name of a real customer,
	 * since we do not want any tests depending on production files.
	 */
	protected static final String TEST_CUSTOMER = "AdGuys";

	protected static final String TEST_CUSTOMER_CODE = "ag";

	public static final String DEFAULT = "default";

	/**
	 * Creates a test with the name of the class as the test name.
	 */
	public AbstractAnchorTest() {
		super();
		setName(this.getClass().getSimpleName());
	}

	/**
	 * Initial default TimeZone, which is saved by {@link #setUp()} and restored by
	 * {@link #tearDown()} in case any test calls {@link TimeZone#setDefault(TimeZone)}.
	 */
	private TimeZone initDefaultTimeZone;

	private static boolean tearDownPending = false;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initDefaultTimeZone = TimeZone.getDefault();
		if (tearDownPending) {
			tearDownPending = false;
			fail("super.tearDown() call missing from some tearDown() method implementation");
		}
		tearDownPending = true;
	}

	@Override
	protected void tearDown() throws Exception {
		TimeZone.setDefault(initDefaultTimeZone);
		initDefaultTimeZone = null;
		if (!tearDownPending) {
			fail("super.setUp() call missing from some setUp() method implementation");
		}
		tearDownPending = false;
		super.tearDown();
	}

	/**
	 * Throws {@link AssertionFailedError} if the two objects are
	 * equal according to {@link ObjectUtils#equals(Object, Object)}.
	 *
	 * @see Assert#assertNotSame(Object, Object)
	 * @see Assert#assertEquals(Object, Object)
	 * @see #assertNotEquals(Object, Object, String)
	 */
	protected void assertNotEquals(Object obj1, Object obj2) {
		if (ObjectUtils.equals(obj1, obj2)) {
			throw new AssertionFailedError("Objects are equal: " + obj1.toString());
		}
	}

	/**
	 * Throws {@link AssertionFailedError} if the two long values
	 * <code>l1</code> and <code>l2</code> are equal.
	 *
	 * @see Assert#assertEquals(long, long)
	 */
	protected void assertNotEquals(long l1, long l2) {
		if (l1 == l2) {
			throw new AssertionFailedError("Long values are equal: " + l1);
		}
	}

	/**
	 * Throws {@link AssertionFailedError} if the two long values
	 * <code>l1</code> and <code>l2</code> are equal.
	 *
	 * @see Assert#assertEquals(long, long)
	 */
	protected void assertNotEquals(long l1, long l2, String errMsg) {
		if (l1 == l2) {
			throw new AssertionFailedError(errMsg);
		}
	}

	/**
	 * Throws {@link AssertionFailedError} if the two objects are
	 * equal according to {@link ObjectUtils#equals(Object, Object)}.
	 *
	 * @see Assert#assertNotSame(String, Object, Object)
	 * @see Assert#assertEquals(String, Object, Object)
	 * @see #assertNotEquals(Object, Object)
	 */
	protected void assertNotEquals(Object obj1, Object obj2, String message) {
		if (ObjectUtils.equals(obj1, obj2)) {
			throw new AssertionFailedError(message);
		}
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> is
	 * not identical to the array <code>got</code>.
	 *
	 * @param exp The array of expected values.
	 * @param got The array of actual values encountered by the unit test.
	 */
	protected void assertArrayEquals(boolean[] exp, boolean[] got) {
		assertEquals("boolean array has incorrect length", exp.length, got.length);
		for (int i = 0; i < exp.length; i++) {
			assertEquals("boolean arrays differ at index " + i, exp[i], got[i]);
		}
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> is
	 * not identical to the array <code>got</code>.
	 *
	 * @param exp The array of expected values.
	 * @param got The array of actual values encountered by the unit test.
	 */
	protected void assertArrayEquals(byte[] exp, byte[] got) {
		assertEquals("byte array has incorrect length", exp.length, got.length);
		for (int i = 0; i < exp.length; i++) {
			assertEquals("byte arrays differ at index " + i, exp[i], got[i]);
		}
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> is
	 * not identical to the array <code>got</code>.
	 *
	 * @param exp The array of expected values.
	 * @param got The array of actual values encountered by the unit test.
	 */
	protected void assertArrayEquals(int[] exp, int[] got) {
		assertEquals("int array has incorrect length", exp.length, got.length);
		for (int i = 0; i < exp.length; i++) {
			assertEquals("int arrays differ at index " + i, exp[i], got[i]);
		}
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> is
	 * not identical to the array <code>got</code>.
	 *
	 * @param exp The array of expected values.
	 * @param got The array of actual values encountered by the unit test.
	 */
	protected void assertArrayEquals(long[] exp, long[] got) {
		assertEquals("long array has incorrect length", exp.length, got.length);
		for (int i = 0; i < exp.length; i++) {
			assertEquals("long arrays differ at index " + i, exp[i], got[i]);
		}
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> is
	 * not identical to the array <code>got</code>.
	 *
	 * @param exp The array of expected values.
	 * @param got The array of actual values encountered by the unit test.
	 */
	protected <T> void assertArrayEquals(T[] exp, T[] got) {
		for (int i = 0; i < Math.min(exp.length, got.length); i++) {
			assertEquals("Object arrays differ at index " + i, exp[i], got[i]);
		}
		assertEquals("Object array has incorrect length", exp.length, got.length);
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> does not contain
	 * elements that are equal (using {@link Object#equals(Object)}) to the elements of
	 * the list <code>got</code>, in order.
	 *
	 * @param exp The array of expected values.
	 * @param got The list of actual values encountered by the unit test.
	 */
	protected <T> void assertListEquals(T[] exp, List<T> got) {
		Iterator<T> it = got.iterator();
		for (int i = 0; i < Math.min(exp.length, got.size()); i++) {
			assertEquals("Incorrect list value at index " + i, exp[i], it.next());
		}
		assertEquals("Actual list has incorrect length", exp.length, got.size());
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> does not contain
	 * elements that are equal (using {@link Object#equals(Object)}) to the elements of
	 * the list <code>got</code>, in order.
	 *
	 * @param exp The array of expected values.
	 * @param got The list of actual values encountered by the unit test.
	 */
	protected <T> void assertListEquals(List<T> got, T... exp) {
		assertListEquals(exp, got);
	}

	/**
	 * Causes a Junit assertion failure if the list <code>exp</code> does not contain
	 * elements that are equal (using {@link Object#equals(Object)}) to the elements of
	 * the list <code>got</code>, in order.
	 *
	 * @param exp The list of expected values.
	 * @param got The list of actual values encountered by the unit test.
	 */
	protected <T> void assertListEquals(List<T> exp, List<T> got) {
		assertEquals("Object list has incorrect length", exp.size(), got.size());
		Iterator<T> it = got.iterator();
		for (int i = 0; i < exp.size(); i++) {
			assertEquals("Incorrect list value at index " + i, exp.get(i), it.next());
		}
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> does not contain
	 * elements that are equal (using reference equality) to the elements of the list
	 * <code>got</code>, in order.
	 *
	 * @param exp The array of expected values.
	 * @param got The list of actual values encountered by the unit test.
	 */
	protected <T> void assertListSame(T[] exp, List<T> got) {
		assertEquals("Object list has incorrect length", exp.length, got.size());
		Iterator<T> it = got.iterator();
		for (int i = 0; i < exp.length; i++) {
			assertSame("Incorrect list value at index " + i, exp[i], it.next());
		}
	}

	/**
	 * Causes a Junit assertion failure if the list <code>exp</code> does not contain
	 * elements that are equal (using reference equality) to the elements of the list
	 * <code>got</code>, in order.
	 *
	 * @param exp The list of expected values.
	 * @param got The list of actual values encountered by the unit test.
	 */
	protected <T> void assertListSame(List<T> exp, List<T> got) {
		assertEquals("Object list has incorrect length", exp.size(), got.size());
		Iterator<T> it = got.iterator();
		for (int i = 0; i < exp.size(); i++) {
			assertSame("Incorrect list value at index " + i, exp.get(i), it.next());
		}
	}

	/**
	 * Causes a Junit assertion failure if the array <code>exp</code> is not
	 * identical to the set <code>got</code>.
	 *
	 * @param exp
	 *            The array of expected values. It is an error for this argument
	 *            to contain any duplicates.
	 * @param got
	 *            The set of actual values encountered by the unit test.
	 */
	protected static <T> void assertSetEquals(T[] exp, Set<T> got) {
		assertSetEquals(Arrays.asList(exp), got);
	}

	/**
	 * Causes a Junit assertion failure if the list <code>exp</code> is not
	 * identical to the set <code>got</code>.
	 *
	 * @param exp
	 *            The list of expected values. It is an error for this argument
	 *            to contain any duplicates.
	 * @param got
	 *            The set of actual values encountered by the unit test.
	 */
	protected static <T> void assertSetEquals(List<T> exp, Set<T> got) {
		assertSetEquals(new HashSet<T>(exp), got);
	}

	/**
	 * Causes a Junit assertion failure if the set <code>exp</code> is
	 * not identical to the set <code>got</code>.
	 *
	 * @param exp The set of expected values.
	 * @param got The set of actual values encountered by the unit test.
	 */
	protected static <T> void assertSetEquals(Set<T> exp, Set<T> got) {
		assertNotNull("Actual results set is null", got);
		for (T elt: exp) {
			assertTrue("Expected element not present in actual results: " + elt.toString(), got.contains(elt));
		}
		if (exp.size() != got.size()) {
			for (T elt: got) {
				assertTrue("Actual results set contains unexpected element: " + elt.toString(), exp.contains(elt));
			}
			fail(); // should never get here
		}
	}

	/**
	 * Causes a JUnit assertion failure if the set <code>s</code> is
	 * not a singleton set containing the element <code>elt</code>.
	 */
	protected <T> void assertSingletonSetEquals(Set<T> s, T elt) {
		assertEquals("set has incorrect size", 1, s.size());
		assertEquals(elt, s.iterator().next());
	}

	/**
	 * Causes a JUnit assertion failure if the collection <code>c</code>
	 * is not empty.
	 */
	protected <T> void assertIsEmpty(Collection<T> c) {
		assertTrue("collection is not empty", c.isEmpty());
	}

	/**
	 * Causes a Junit assertion failure if the map <code>exp</code> does not contain
	 * key/value pairs that are equal (using {@link Object#equals(Object)}) to the key/value
	 * pairs of the map <code>got</code>.
	 *
	 * @param exp The map of expected values.
	 * @param got The map of actual values encountered by the unit test.
	 */
	protected <K,V> void assertMapEquals(Map<K,V> exp, Map<K,V> got) {
		for (Map.Entry<K, V> expEntry: exp.entrySet()) {
			K key = expEntry.getKey();
			assertTrue("Object map does not contain key: " + key, got.containsKey(key));
			assertEquals("Object map has incorrect value for key: " + key, exp.get(key), got.get(key));
		}
		assertEquals("Object map has incorrect size", exp.size(), got.size());
	}

	/**
	 * Causes a JUnit assertion failure if the set <code>s</code> is
	 * not a singleton set containing the element <code>elt</code>.
	 */
	protected <K,V> void assertSingletonMapEquals(Map<K,V> m, K key, V value) {
		assertEquals("map has incorrect size", 1, m.size());
		assertEquals(key, m.keySet().iterator().next());
		assertEquals(value, m.values().iterator().next());
	}

	/**
	 * Causes a Junit assertion failure if the given <code>file</code> does not contain
	 * exactly <code>expLines</code> lines.
	 */
	protected void checkFileLineCount(File file, int expLines) throws IOException {
		assertTrue("File '" + file.getPath() + "' does not exist", file.exists());
		BufferedReader rd = new BufferedReader(new FileReader(file));
		try {
			assertEquals(file.getPath() + " linecount", expLines, getLineCount(rd));
		} finally {
			rd.close();
		}
	}

	/**
	 * Returns the number of lines remaining in the reader <code>rd</code>.
	 */
	protected int getLineCount(BufferedReader rd) throws IOException {
		int res = 0;
		while (rd.readLine() != null) {
			res++;
		}
		return res;
	}

	/** Returns the byte array containing the characters of the String <code>s</code>. */
	protected byte[] toByteArray(String s) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new PrintStream(baos).print(s);
		return baos.toByteArray();
	}

	/**
	 * Returns the date (represented as the number of milliseconds since the
	 * epoch) with the given year, month, day, hour, minute, and second relative
	 * to the given time zone. The date is aligned on a second boundary (i.e.,
	 * the number of milliseconds within the second is 0).
	 *
	 * @param year The date's year (e.g., 2008).
	 * @param month The date's 1-based month (i.e., January is month 1).
	 * @param day The date's day of the month (also 1-based).
	 * @param hour The date's hour of the day (in the range 0-23 inclusive).
	 * @param min The date's minute of the hour (in the range 0-59 inclusive).
	 * @param sec The date's second of the minute (in the range 0-59 inclusive).
	 * @param timeZoneName The name of the time zone relative to which the
	 * given <code>date</code> is interpreted (e.g., "UTC" or "PST").
	 *
	 * @see #makeDate
	 * @see #makeTimeInMillis(int, int, int, int, int, int, int, String)
	 * @see #assertDateEquals(int, int, int, int, int, int, String, Date)
	 */
	protected static long makeTimeInMillis(
		int year, int month, int day,
		int hour, int min, int sec, String timeZoneName)
	{
			return makeTimeInMillis(year, month, day, hour, min, sec, 0, timeZoneName);
	}

	/**
	 * Returns the date (represented as the number of milliseconds since the
	 * epoch) with the given year, month, day, hour, minute, and second relative
	 * to the given time zone. The date is aligned on a second boundary (i.e.,
	 * the number of milliseconds within the second is 0).
	 *
	 * @param year The date's year (e.g., 2008).
	 * @param month The date's 1-based month (i.e., January is month 1).
	 * @param day The date's day of the month (also 1-based).
	 * @param hour The date's hour of the day (in the range 0-23 inclusive).
	 * @param min The date's minute of the hour (in the range 0-59 inclusive).
	 * @param sec The date's second of the minute (in the range 0-59 inclusive).
	 * @param millis The date's millisecond.
	 * @param timeZoneName The name of the time zone relative to which the
	 * given <code>date</code> is interpreted (e.g., "UTC" or "PST").
	 *
	 * @see #makeDate
	 * @see #makeTimeInMillis(int, int, int, int, int, int, String)
	 * @see #assertDateEquals(int, int, int, int, int, int, int, String, Date)
	 */
	protected static long makeTimeInMillis(
		int year, int month, int day,
		int hour, int min, int sec, int millis, String timeZoneName)
	{
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneName));
			cal.set(year, month - 1, day, hour, min, sec);
			cal.set(Calendar.MILLISECOND, millis);
			return cal.getTimeInMillis();
	}

	/**
	 * Returns the date with the given year, month, day, hour, minute, and second
	 * relative to the given time zone. The date is aligned on a second boundary (i.e.,
	 * the number of milliseconds within the second is 0).
	 *
	 * @param year The date's year (e.g., 2008).
	 * @param month The date's 1-based month (i.e., January is month 1).
	 * @param day The date's day of the month (also 1-based).
	 * @param hour The date's hour of the day (in the range 0-23 inclusive).
	 * @param min The date's minute of the hour (in the range 0-59 inclusive).
	 * @param sec The date's second of the minute (in the range 0-59 inclusive).
	 * @param timeZoneName The name of the time zone relative to which the
	 * given <code>date</code> is interpreted (e.g., "UTC" or "PST").
	 *
	 * @see #makeTimeInMillis(int, int, int, int, int, int, String)
	 * @see #assertDateEquals(int, int, int, int, int, int, String, Date)
	 */
	protected static Date makeDate(
		int year, int month, int day,
		int hour, int min, int sec, String timeZoneName)
	{
		return new Date(makeTimeInMillis(year, month, day, hour, min, sec, 0, timeZoneName));
	}

	/**
	 * Returns a Calendar for the given <code>date</code> (interpreted relative
	 * to the given <code>timeZoneName</code>).
	 *
	 * @param date The date to set on the calendar.
	 * @param timeZoneName The name of the time zone, such as "UTC" or "PST".
	 */
	protected static Calendar dateToCalendar(Date date, String timeZoneName) {
		assertNotNull(date);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneName));
		cal.setTime(date);
		return cal;
	}

	/**
	 * Causes a Junit assertion failure if the given <code>date</code> does not have the
	 * given calendar fields (interpreted relative to the given time zone). The milliseconds
	 * within the second are required to be 0.
	 * @param year The date's year (e.g., 2008).
	 * @param month The date's 1-based month (i.e., January is month 1).
	 * @param day The date's day of the month (also 1-based).
	 * @param hour The date's hour of the day (in the range 0-23 inclusive).
	 * @param min The date's minute of the hour (in the range 0-59 inclusive).
	 * @param sec The date's second of the minute (in the range 0-59 inclusive).
	 * @param timeZoneName The name of the time zone relative to which the
	 * given <code>date</code> is interpreted (e.g., "UTC" or "PST").
	 * @param date The date to check.
	 *
	 * @see #makeTimeInMillis(int, int, int, int, int, int, String)
	 * @see #makeDate(int, int, int, int, int, int, String)
	 */
	protected static void assertDateEquals(
		int year, int month, int day, int hour,
		int min, int sec, String timeZoneName, Date date)
	{
		assertDateEquals(year, month, day, hour, min, sec, 0, timeZoneName, date);
	}

	/**
	 * Causes a Junit assertion failure if the given <code>date</code> does not have the
	 * given calendar fields (interpreted relative to the given time zone). The milliseconds
	 * within the second are required to be 0.
	 * @param year The date's year (e.g., 2008).
	 * @param month The date's 1-based month (i.e., January is month 1).
	 * @param day The date's day of the month (also 1-based).
	 * @param hour The date's hour of the day (in the range 0-23 inclusive).
	 * @param min The date's minute of the hour (in the range 0-59 inclusive).
	 * @param sec The date's second of the minute (in the range 0-59 inclusive).
	 * @param millis The dates' milliseconds.
	 * @param timeZoneName The name of the time zone relative to which the
	 * given <code>date</code> is interpreted (e.g., "UTC" or "PST").
	 * @param date The date to check.
	 *
	 * @see #makeTimeInMillis(int, int, int, int, int, int, int, String)
	 * @see #makeDate(int, int, int, int, int, int, String)
	 */
	protected static void assertDateEquals(
		int year, int month, int day, int hour,
		int min, int sec, int millis, String timeZoneName, Date date)
	{
		long ts = makeTimeInMillis(year, month, day, hour, min, sec, millis, timeZoneName);
		assertEquals(ts, date.getTime());
	}

	/**
	 * Returns the result of replacing all whitespace characters with their
	 * corresponding two-character escape sequence. In particular, space is
	 * replaced by "\b", tab by "\t", newline by "\n", and carriage return
	 * by "\r".
	 */
	protected static String showWhiteSpace(String s) {
		return s.replace(" ", "\\b").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
	}

	/**
	 * Returns the top-level element that results from parsing the given
	 * <code>xmlString</code> as an XML document with XML validation enabled.
	 *
	 * @see #parseXmlDocument(String, boolean)
	 */
	protected static Element parseXmlDocument(String xmlString)
		throws ParserConfigurationException, SAXException, IOException
	{
		return parseXmlDocument(xmlString, true);
	}

	/**
	 * Returns the top-level element that results from parsing the given
	 * <code>xmlString</code> as an XML document.
	 *
	 * @param doValidation
	 *            Specifies whether or not the given XML should be validated
	 *            against a DTD.
	 * @see #parseXmlDocument(String)
	 */
	protected static Element parseXmlDocument(String xmlString, boolean doValidation)
		throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilder db = XmlUtilities.createXmlDocumentBuilder(doValidation);
		assertEquals(doValidation, db.isValidating());
		ByteArrayInputStream is = new ByteArrayInputStream(xmlString.getBytes());
		return db.parse(is).getDocumentElement();
	}

	/**
	 * Returns a new XML node whose node type is {@link Node#TEXT_NODE} and whose
	 * node value is <code>nodeText</code>.
	 */
	protected Node makeTextNode(String nodeText)
		throws ParserConfigurationException, SAXException, IOException
	{
		String xmlString = "<foo>" + nodeText + "</foo>"; // any wrapping tags will do
		Element node = parseXmlDocument(xmlString, /*doValidation=*/ false);
		NodeList children = node.getChildNodes();
		assertEquals(1, children.getLength());
		Node res = children.item(0);
		assertEquals(Node.TEXT_NODE, res.getNodeType());
		assertEquals(nodeText, res.getNodeValue());
		return res;
	}

	/**
	 * Causes a Junit assertion failure if the <code>actual</code> List of String arrays
	 * does not equal the 2-dimensional array of <code>expected</code> values.
	 *
	 * @param expected
	 * @param actual
	 */
	protected void checkStringGrid(String[][] expected, List<String[]> actual) {
		assertEquals("incorrect number of rows", expected.length, actual.size());
		for (int i=0; i < expected.length; i++) {
			assertEquals("incorrect number of columns", expected[i].length, actual.get(i).length);
			for (int j=0; j < expected[i].length; j++) {
				assertEquals("row " + i + ", column " + j, expected[i][j], actual.get(i)[j]);
			}
		}
	}

	/**
	 * Causes a Junit assertion failure if the exception <code>ex</code> was not produced
	 * as the result of the failure of a call to {@link ArgCheck#isNotBlank(String, String)}
	 * on the argument named <code>argName</code>.
	 */
	protected void assertIsNotBlankException(String argName, IllegalArgumentException ex) {
		ArgCheckTest.assertIsNotBlankException(argName, ex);
	}

	/**
	 * Causes a Junit assertion failure if the exception <code>ex</code> was not produced
	 * as the result of the failure of a call to {@link ArgCheck#isNotNull(Object, String)}
	 * on the argument named <code>argName</code>.
	 */
	protected void assertIsNotNullException(String argName, IllegalArgumentException ex) {
		ArgCheckTest.assertIsNotNullException(argName, ex);
	}

	/**
	 * Causes a Junit assertion failure if the exception <code>ex</code> was not produced
	 * as the result of the failure of a call to {@link ArgCheck#isNotEmpty(Object[], String)}
	 * on the argument named <code>argName</code>.
	 */
	protected void assertIsNotEmptyArrayException(String argName, IllegalArgumentException ex) {
		ArgCheckTest.assertIsNotEmptyArrayException(argName, ex);
	}

	/**
	 * Causes a Junit assertion failure if the exception <code>ex</code> was not produced
	 * as the result of the failure of a call to {@link ArgCheck#isNotEmpty(Collection, String)}
	 * on the argument named <code>argName</code>.
	 */
	protected void assertIsNotEmptyCollectionException(String argName, IllegalArgumentException ex) {
		ArgCheckTest.assertIsNotEmptyCollectionException(argName, ex);
	}

	/**
	 * Causes a Junit assertion failure if the exception <code>ex</code> was not produced
	 * as the result of the failure of a call to {@link ArgCheck#isNull(Object, String)} on
	 * the argument named <code>argName</code>.
	 */
	protected void assertIsNullException(String argName, IllegalArgumentException ex) {
		ArgCheckTest.assertIsNullException(argName, ex);
	}

	/**
	 * Causes a Junit assertion failure if the object <code>gotObject</code> is null or is
	 * not an instance of the class or interface <code>expClass</code>.
	 */
	protected void assertInstanceOf(Object gotObject, Class<?> expClass) {
		assertNotNull(gotObject);
		String errMsg =
			"The Object of type " + gotObject.getClass().getName()
			+ " is not an instance of the expected class " + expClass.getName() + ".";
		assertTrue(errMsg, expClass.isInstance(gotObject));
	}

	/**
	 * Returns true if and only if the test is being run on a machine running
	 * the Windows operating system.
	 */
	protected boolean isWindowsOS() {
		return Utilities.isWindowsOS();
	}

	/**
	 * Causes a Junit assertion failure if the two strings with the whitespace removed are different.
	 */
	protected void assertStringsEqualsIgnoreWhiteSpace(String expected, String actual) {
		if (expected == null || actual == null) {
			assertEquals(expected, actual);
			return;
		}
		assertEquals(expected.replace(" ", ""), actual.replace(" ", ""));
	}

	/**
	 * Causes a Junit assertion failure if the a file named <code>name</code> does
	 * not exist in the directory <code>dir</code>.
	 */
	protected void assertFileExistsInDir(File dir, String name) {
		File file = new File(dir, name);
		assertTrue("File does not exist: " + file, file.exists());
	}

	/**
	 * Causes a Junit assertion failure if the a file <code>files</code> does not exist.
	 */
	protected void assertFileExists(File file) {
		assertTrue("File does not exist: " + file, file.exists());
	}

	/**
	 * Causes a Junit assertion failure if the a file <code>files</code> exist.
	 */
	protected void assertFileDoesNotExist(File file) {
		assertTrue("File does exist: " + file, ! file.exists());
	}

	/**
	 * Causes a Junit assertion failure if the a file named <code>name</code>
	 * exists in the directory <code>dir</code>.
	 */
	protected void assertFileDoesNotExistsInDir(File dir, String name) {
		File file = new File(dir, name);
		assertFalse("File exists: " + file, file.exists());
	}

	protected static final String join(String sep, Object ...objs){
		return StringUtils.join(objs, sep);
	}

	public static Integer toInteger(Number n) {
		return n == null ? null : new Integer(n.intValue());
	}

	public static Long toLong(Number n) {
		return n == null ? null : new Long(n.longValue());
	}

	/**
	 * Randomly permutes the given <code>array</code> in place; this is a destructive operation.
	 */
	protected static <T> T[] shuffleArray(/*INOUT*/ T[] array) {
		Collections.shuffle(Arrays.asList(array));
		return array;
	}

	/**
	 * Causes a Junit assertion failure if the bytes in the file <code>actual</code> do
	 * not match the bytes in the file <code>expected</code>.
	 * <p>
	 * Either or both of the files may have a name ending in ".gz", in which
	 * case those file(s) are gunzipped on-the-fly before doing the byte-by-byte comparison.
	 *
	 * @see #assertTextFilesEqual(File, File)
	 * @see #assertFileEquals(File, String...)
	 */
	protected void assertFilesEqual(File expected, File actual) throws Exception {
		InputStream is1 = getInputStream(expected);
		try {
			InputStream is2 = getInputStream(actual);
			try {
				int i = 0, c1, c2;
				for (; (c1 = is1.read()) != -1; i++) {
					c2 = is2.read();
					if (c2 == -1) {
						fail("Encountered premature EOF at character " + i + " on file " + actual.getPath());
					}
					assertEquals(
						"Files " + expected.getPath() + " and " + actual.getPath() + " differ at character " + i, c1, c2);
				}
				c2 = is2.read();
				if (c2 != -1) {
					fail("Encountered premature EOF at character " + i + " on file " + expected.getPath());
				}
			} finally {
				is2.close();
			}
		} finally {
			is1.close();
		}
	}



	/**
	 * Causes a Junit assertion failure if the text in the file
	 * <code>actual</code> does not match the text in the file
	 * <code>expected</code> (with the possible exception of OS specific line
	 * endings). Either or both of the files may have a name ending in ".gz", in
	 * which case those file(s) are gunzipped on-the-fly before doing the
	 * line-by-line comparison.
	 * <p>
	 * In the event of a failure, reports the text of the lines, and line
	 * number of the difference.
	 *
	 * @see #assertFilesEqual(File, File)
	 * @see #assertFileEquals(File, String...)
	 */
	protected void assertTextFilesEqual(File expected, File actual) throws IOException {
		BufferedReader r1 = new BufferedReader(new InputStreamReader(getInputStream(expected)));
		try {
			BufferedReader r2 = new BufferedReader(new InputStreamReader(getInputStream(actual)));
			try {
				int lineNo = 1;
				while (true) {
					String s1 = r1.readLine();
					String s2 = r2.readLine();
					if (s1 == null) {
						if (s2 == null) {
							// EOF at the same time... files are identical
							break;
						} else {
							// Extra data in f2
							fail("Actual file " + actual.getPath() + " contains too many lines.");
						}
					} else if (s2 == null) {
						// f2 is too short
						fail("Actual file " + actual.getPath() + " contains too few lines.");
					} else {
						assertEquals(
							"Files " + expected.getPath() + " and " + actual.getPath() + " differ at line " + lineNo, s1, s2);
					}
				}
			} finally {
				r2.close();
			}
		} finally {
			r1.close();
		}
	}

	/**
	 * Causes a junit assertion failure if the lines of the given {@code file} do not
	 * match the {@code expected} lines.
	 *
	 * @see #assertFilesEqual(File, File)
	 * @see #assertTextFilesEqual(File, File)
	 */
	protected void assertFileEquals(File file, String... expectedLines) throws IOException {
		assertTrue(file + " does not exist", file.exists());
		AnchorLineNumberReader rd = IOUtils.getLineNumberReader(file);
		try {
			for (String expLine: expectedLines) {
				String gotLine = rd.readLine();
				assertNotNull(
					"File contains too few lines; expected " + expectedLines.length + ", got " + rd.getLineNumber(), gotLine);
				assertEquals("Lines differ at line " + rd.getLineNumber(), expLine, gotLine);
			}
			assertNull("File contains too many lines; expected only " + expectedLines.length, rd.readLine());
		} finally {
			rd.close();
		}
	}

	/**
	 * Causes a Junit assertion failure if the contents of the json object
	 * <code>actual</code> does not match the contents of the json object
	 * <code>expected</code>
	 * <p>
	 * In the event of a failure, reports the text of the json object.
	 */
	protected void assertJsonObjectsEqual(JSONObject expected, JSONObject actual) {
		assertEquals("JSONObject " + expected.size() + " and " + actual.size() + " sizes do not match ",
			expected.size(), actual.size());
		for (Object key : expected.keySet()) {
			assertTrue("Actual JSONObject is missing the key: " + key, actual.containsKey(key));
			Object expectedValue = expected.get(key);
			Object actualValue = actual.get(key);
			if (expectedValue instanceof JSONArray) {
				JSONArray expectedArray = (JSONArray)expected.get(key);
				assertInstanceOf(actualValue, JSONArray.class);
				JSONArray actualArray = (JSONArray)actual.get(key);
				assertJsonArraysEqual(key, expectedArray, actualArray);
			} else if (expectedValue instanceof JSONObject) {
				assertInstanceOf(actualValue, JSONObject.class);
				assertJsonObjectsEqual((JSONObject)expectedValue, (JSONObject)actualValue);
			} else {
				assertEquals("JSONObjects differ for key: " + key + " ", expectedValue, actualValue);
			}
		}
	}

	/**
	 * Causes a Junit assertion failure if the contents of the xml string
	 * <code>actual</code> does not match the contents of the xml string
	 * <code>expected</code>
	 * <p>
	 * In the event of a failure, reports the text of the xml strings.
	 */
	protected void assertXmlStringsEqual(String expected, String actual) {
		try {
			Element expectedRoot = XmlUtilities.parseXmlFile(expected);
			Element actualRoot = XmlUtilities.parseXmlFile(actual);
			assertEquals("Root nodes have different names, expected: " + expectedRoot.getNodeName() +
				" but was " + actualRoot.getNodeName(), expectedRoot.getNodeName(), actualRoot.getNodeName());
			assertXmlAttributesEqual(expectedRoot.getNodeName(), expectedRoot.getAttributes(), actualRoot.getAttributes());
			assertNodeListsEqual(expectedRoot.getNodeName(), expectedRoot.getChildNodes(), actualRoot.getChildNodes());
		} catch (Exception e) {
			fail();
		}
	}

	private void assertNodeListsEqual(String nodeName, NodeList expected, NodeList actual) {
		assertEquals("For node: " + nodeName + " number of child nodes mismatch, expected " + expected.getLength() +
			" but was " + actual.getLength(), expected.getLength(), actual.getLength());
		if (expected.getLength() == 0) {
			return;
		}
		// populate a map of actual nodes mapped by the node key
		Map<String,Node> actualNodeMap = new HashMap<String,Node>();
		for (int i=0; i < actual.getLength(); i++) {
			Node node = actual.item(i);
			actualNodeMap.put(getNodeKey(node), node);
		}
		// check the expected nodes against the actual nodes map
		for (int i=0; i < expected.getLength(); i++) {
			Node expectedNode = expected.item(i);
			String expectedNodeKey = getNodeKey(expectedNode);
			Node actualNode = actualNodeMap.get(expectedNodeKey);
			assertNotNull("Missing expected node key: " + expectedNodeKey, actualNode);
			assertXmlAttributesEqual(expectedNode.getNodeName(), expectedNode.getAttributes(), actualNode.getAttributes());
			assertNodeListsEqual(expectedNode.getNodeName(), expectedNode.getChildNodes(), actualNode.getChildNodes());
		}
	}

	// Concatenates the node name with the key value pairs of the attributes for a unique node name
	private String getNodeKey(Node node) {
		StringBuilder key = new StringBuilder(node.getNodeName()).append('-');
		for (int i=0; i < node.getAttributes().getLength(); i++) {
			Node attribute = node.getAttributes().item(i);
			key.append(attribute.getNodeName()).append('=').append(attribute.getNodeValue()).append('&');
		}
		return key.toString();
	}

	private void assertXmlAttributesEqual(String nodeName, NamedNodeMap expected, NamedNodeMap actual) {
		assertEquals("For node: " + nodeName + " number of attributes mismatch, expected " + expected.getLength() +
			" but was " + actual.getLength(), expected.getLength(), actual.getLength());
		for (int i=0; i < expected.getLength(); i++) {
			Node actualNode = actual.getNamedItem(expected.item(i).getNodeName());
			assertNotNull("For node: " + nodeName + " missing expected attribute: " + expected.item(i).getNodeName(),
				actualNode);
			String actualNodeValue = actualNode.getNodeValue();
			assertEquals("For node: " + nodeName + " attribute mismatch, expected " + expected.item(i).getNodeValue() +
				" but was " + actualNodeValue, expected.item(i).getNodeValue(), actualNodeValue);
		}
	}

	/**
	 * Causes a Junit assertion failure if the contents of the json array
	 * <code>actual</code> does not match the contents of the json array
	 * <code>expected</code>
	 * <p>
	 * In the event of a failure, reports the text of the json array.
	 */
	protected void assertJsonArraysEqual(Object key, JSONArray expectedArray, JSONArray actualArray) {
		for (int i=0; i < expectedArray.size(); i++) {
			Object ev = expectedArray.get(i);
			Object av = actualArray.get(i);
			if (ev instanceof BigDecimal) {
				assertInstanceOf(av, BigDecimal.class);
				assertBigDecimalEquals((BigDecimal)ev, (BigDecimal)av, 3);
			} else if (ev instanceof JSONArray){
				assertJsonArraysEqual(key, (JSONArray)ev, (JSONArray)av);
			} else {
				assertEquals("JSONArrays do not match for key: " + key + " at index: " + i, ev, av);
			}
		}
	}

	private InputStream getInputStream(File f) throws IOException {
		InputStream res = new FileInputStream(f);
		if (f.getName().endsWith(".gz")) {
			res = new GZIPInputStream(res);
		}
		return res;
	}

	/**
	 * Causes a Junit assertion failure if the BigDecimal <code>expected</code>
	 * is not identical to the BigDecimal <code>actual</code> using the
	 * specified scale. Uses round half up when scaling the big decimal for
	 * comparison.
	 *
	 * @param expected
	 *            The expected BigDecimal.
	 * @param actual
	 *            The actual BigDecimal.
	 * @param scale
	 *            The number of digits to scale the decimals to for comparison.
	 * @see BigDecimal#ROUND_HALF_UP
	 * @see #assertBigDecimalEquals(String, BigDecimal)
	 */
	protected void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual, int scale) {
		assertEquals(expected.setScale(scale, BigDecimal.ROUND_HALF_UP),
			actual.setScale(scale, BigDecimal.ROUND_HALF_UP));
	}

	/**
	 * Checks that the value <code>got</code> is equivalent to constructing a
	 * new {@link BigDecimal} on the String <code>expValue</code>.
	 *
	 * @see #assertBigDecimalEquals(BigDecimal, BigDecimal, int)
	 */
	protected void assertBigDecimalEquals(String expValue, BigDecimal got) {
		assertEquals(new BigDecimal(expValue), got);
	}

	/**
	 * Causes a Junit assertion failure if the cause and message of the
	 * exception <code>e</code> do not match <code>expCauseClass</code> and
	 * <code>expMessage</code>, respectively.
	 */
	protected void assertExceptionEquals(Exception e, Class<? extends Exception> expCauseClass, String expMessage) {
		assertInstanceOf(e.getCause(), expCauseClass);
		assertEquals(expMessage, e.getMessage());
	}
}
