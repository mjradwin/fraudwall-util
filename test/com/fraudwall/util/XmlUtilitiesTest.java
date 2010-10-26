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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.fraudwall.util.XmlUtilities.UrlInputStream;
import com.fraudwall.util.XmlUtilities.UrlInputStreamResult;

public class XmlUtilitiesTest extends AbstractAnchorTest {

	private static final String TAG_NAME = "tagName";
	private static final String NODE_NAME_CHILD = "test";
	private static final String NODE_NAME_ROOT = "root";
	private static final String ELEMENT = "element";
	private static final String REQUEST_URL = "foo";

	// ------------------------------------- findElements

	public void testFindElementsThrowsExceptionOnNullRoot() throws Exception {
		try {
			XmlUtilities.findElements(null, null);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotNullException(NODE_NAME_ROOT, e);
		}
	}

	public void testFindElementsThrowsExceptionOnNullOrEmptyChildName() throws Exception {
		checkFindElementsThrowsOn(null);
		checkFindElementsThrowsOn("    ");
	}

	private void checkFindElementsThrowsOn(String childName) throws Exception {
		try {
			XmlUtilities.findElements(constructXmlElement(NODE_NAME_ROOT), childName);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotBlankException(TAG_NAME, e);
		}
	}

	public void testFindElementsReturnsEmptyListWhenNoChildren() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT);
		NodeList list = XmlUtilities.findElements(root, NODE_NAME_CHILD);
		assertEquals(0, list.getLength());
	}

	public void testFindElementsReturnsListWithChildren() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, "child", 5, "test value");
		NodeList list = XmlUtilities.findElements(root, "child");
		assertEquals(5, list.getLength());
	}

	public void testFindElementsReturnsListWithDescendants() throws Exception {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElement(NODE_NAME_ROOT);
		for (int i=0; i < 3; i++) {
			Element child = createElement(doc, i, "child1", "test value");
			for (int j=0; j < 2; j++) {
				Element subchild = createElement(doc, i+j, "child2", "test value2");
				child.appendChild(subchild);
			}
			root.appendChild(child);
		}
		NodeList list = XmlUtilities.findElements(root, "child2");
		assertEquals(6, list.getLength());
	}

	// ------------------------------------- findFirstElement

	public void testFindFirstElementThrowsExceptionOnNullRoot() throws Exception {
		try {
			XmlUtilities.findFirstElement(null, null);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotNullException(NODE_NAME_ROOT, e);
		}
	}

	public void testFindFirstElementThrowsExceptionOnNullOrEmptyChildName() throws Exception {
		checkFindFirstElementThrowsOn(null);
		checkFindFirstElementThrowsOn("    ");
	}

	private void checkFindFirstElementThrowsOn(String childName) throws Exception {
		try {
			XmlUtilities.findFirstElement(constructXmlElement(NODE_NAME_ROOT), childName);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotBlankException(TAG_NAME, e);
		}
	}

	public void testFindFirstElementReturnsNullWhenNoChildren() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT);
		Element node = XmlUtilities.findFirstElement(root, NODE_NAME_CHILD);
		assertNull(node);
	}

	public void testFindFirstElementReturnsChild() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, "child", 5, "test value");
		Element node = XmlUtilities.findFirstElement(root, "child");
		assertNotNull(node);
		assertEquals("0", node.getAttribute("num"));
	}

	// ------------------------------------- findFirstElementAsInteger

	public void testFindFirstElementAsIntegerThrowsExceptionOnNullRoot() throws Exception {
		try {
			XmlUtilities.findFirstElementAsInteger(null, null);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotNullException(NODE_NAME_ROOT, e);
		}
	}

	public void testFindFirstElementAsIntegerThrowsExceptionOnNullOrEmptyChildName() throws Exception {
		checkFindFirstElementAsIntegerThrowsOn(null);
		checkFindFirstElementAsIntegerThrowsOn("    ");
	}

	private void checkFindFirstElementAsIntegerThrowsOn(String childName) throws Exception {
		try {
			XmlUtilities.findFirstElementAsInteger(constructXmlElement(NODE_NAME_ROOT), childName);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotBlankException(TAG_NAME, e);
		}
	}

	public void testFindFirstElementAsIntegerReturnsUnknownIntWhenNoChildren() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT);
		int value = XmlUtilities.findFirstElementAsInteger(root, NODE_NAME_CHILD);
		assertEquals(XmlUtilities.UNKNOWN_INT, value);
	}

	public void testFindFirstElementAsIntegerReturnsUnknownIntWhenEmptyValue() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 2, "  ");
		int value = XmlUtilities.findFirstElementAsInteger(root, NODE_NAME_CHILD);
		assertEquals(XmlUtilities.UNKNOWN_INT, value);
	}

	public void testFindFirstElementAsIntegerThrowsExceptionWhenNonIntValue() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 5, "abc");
		try {
			XmlUtilities.findFirstElementAsInteger(root, NODE_NAME_CHILD);
			fail();
		} catch (NumberFormatException e) {
			// expected condition
			assertEquals("For input string: \"abc\"", e.getMessage());
		}
	}

	public void testFindFirstElementAsIntegerReturnsChild() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 5, "123");
		int value = XmlUtilities.findFirstElementAsInteger(root, NODE_NAME_CHILD);
		assertEquals(123, value);
	}

	// ------------------------------------- findFirstElementAsDateTime

	public void testFindFirstElementAsDateTimeThrowsExceptionOnNullRoot() throws Exception {
		try {
			XmlUtilities.findFirstElementAsDateTime(null, null);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotNullException(NODE_NAME_ROOT, e);
		}
	}

	public void testFindFirstElementAsDateTimeThrowsExceptionOnNullOrEmptyChildName() throws Exception {
		checkFindFirstElementAsDateTimeThrowsOn(null);
		checkFindFirstElementAsDateTimeThrowsOn("    ");
	}

	private void checkFindFirstElementAsDateTimeThrowsOn(String childName) throws Exception {
		try {
			XmlUtilities.findFirstElementAsDateTime(constructXmlElement(NODE_NAME_ROOT), childName);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotBlankException(TAG_NAME, e);
		}
	}

	public void testFindFirstElementAsDateTimeReturnsUnknownIntWhenNoChildren() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT);
		long ts = XmlUtilities.findFirstElementAsDateTime(root, NODE_NAME_CHILD);
		assertEquals(XmlUtilities.UNKNOWN_TIME, ts);
	}

	public void testFindFirstElementAsDateTimeReturnsUnknownIntWhenEmptyValue() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 2, "  ");
		long ts = XmlUtilities.findFirstElementAsDateTime(root, NODE_NAME_CHILD);
		assertEquals(XmlUtilities.UNKNOWN_TIME, ts);
	}

	public void testFindFirstElementAsDateTimeReturnsChild() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 5, "2008-12-04");
		long ts = XmlUtilities.findFirstElementAsDateTime(root, NODE_NAME_CHILD);
		assertEquals(1228348800000L, ts);
	}

	public void testFindFirstElementAsDateTimeReturnsUnknownTimeWhenInvalidDate() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 5, "abc");
		long res = findFirstElementAsDateTimeUsingLog(new SmartDateParserTest.NoErrorLog(), root, NODE_NAME_CHILD);
		assertEquals(XmlUtilities.UNKNOWN_TIME, res);
	}

	public void testFindFirstElementAsDateTimeLogsErrorWhenInvalidDate() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 5, "abc");
		SmartDateParserTest.NoErrorLog log = new SmartDateParserTest.NoErrorLog();
		findFirstElementAsDateTimeUsingLog(log, root, NODE_NAME_CHILD);
		assertEquals("Unparseable date: abc", log.errMsgArg);
	}

	private long findFirstElementAsDateTimeUsingLog(Log log, Element root, String tagName) {
		Log logOrig = SmartDateParser.log;
		SmartDateParser.log = log;
		try {
			return XmlUtilities.findFirstElementAsDateTime(root, tagName);
		} finally {
			SmartDateParser.log = logOrig;
		}
	}

	// ------------------------------------- findFirstElementAsText

	public void testFindFirstElementAsTextThrowsExceptionOnNullRoot() throws Exception {
		try {
			XmlUtilities.findFirstElementAsText(null, null);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotNullException(NODE_NAME_ROOT, e);
		}
	}

	public void testFindFirstElementAsTextThrowsExceptionOnNullOrEmptyChildName() throws Exception {
		checkFindFirstElementAsTextThrowsOn(null);
		checkFindFirstElementAsTextThrowsOn("    ");
	}

	private void checkFindFirstElementAsTextThrowsOn(String childName) throws Exception {
		try {
			XmlUtilities.findFirstElementAsText(constructXmlElement(NODE_NAME_ROOT), childName);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotBlankException(TAG_NAME, e);
		}
	}

	public void testFindFirstElementAsTextReturnsUnknownStringWhenNoChildren() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT);
		String value = XmlUtilities.findFirstElementAsText(root, NODE_NAME_CHILD);
		assertEquals(XmlUtilities.UNKNOWN_STRING, value);
	}

	public void testFindFirstElementAsTextReturnsChild() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 5, "abc");
		String value = XmlUtilities.findFirstElementAsText(root, NODE_NAME_CHILD);
		assertEquals("abc", value);
	}

	// ------------------------------------- findFirstChildAsText

	public void testFindFirstChildAsTextThrowsExceptionOnNullRoot() throws Exception {
		try {
			XmlUtilities.findFirstChildAsText(null, "foo");
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotNullException(ELEMENT, e);
		}
	}

	public void testFindFirstChildtAsTextThrowsExceptionOnNullOrEmptyTagName() throws Exception {
		checkFindFirstChildAsTextThrowsOn(null);
		checkFindFirstChildAsTextThrowsOn("    ");
	}

	private void checkFindFirstChildAsTextThrowsOn(String tagName) throws Exception {
		try {
			XmlUtilities.findFirstChildAsText(constructXmlElement(NODE_NAME_ROOT), tagName);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotBlankException("tagName", e);
		}
	}

	public void testFindFirstChildAsTextReturnsUnknownStringWhenNoChildren() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT);
		String value = XmlUtilities.findFirstChildAsText(root, NODE_NAME_CHILD);
		assertEquals(XmlUtilities.UNKNOWN_STRING, value);
	}

	public void testFindFirstChildAsTextReturnsChild() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 5, "abc");
		String value = XmlUtilities.findFirstChildAsText(root, NODE_NAME_CHILD);
		assertEquals("abc", value);
	}

	// ------------------------------------- findFirstChildAsDateTime

	public void testFindFirstChildAsDateTimeThrowsExceptionOnNullRoot() throws Exception {
		try {
			XmlUtilities.findFirstChildAsDateTime(null, "foo");
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotNullException(ELEMENT, e);
		}
	}

	public void testFindFirstChildtAsDateTimeThrowsExceptionOnNullOrEmptyTagName() throws Exception {
		checkFindFirstChildAsDateTimeThrowsOn(null);
		checkFindFirstChildAsDateTimeThrowsOn("    ");
	}

	private void checkFindFirstChildAsDateTimeThrowsOn(String tagName) throws Exception {
		try {
			XmlUtilities.findFirstChildAsDateTime(constructXmlElement(NODE_NAME_ROOT), tagName);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotBlankException("tagName", e);
		}
	}

	public void testFindFirstChildAsDateTimeReturnsUnknownStringWhenNoChildren() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT);
		long value = XmlUtilities.findFirstChildAsDateTime(root, NODE_NAME_CHILD);
		assertEquals(XmlUtilities.UNKNOWN_TIME, value);
	}

	public void testFindFirstChildAsDateTimeReturnsChild() throws Exception {
		Element root = constructXmlElement(NODE_NAME_ROOT, NODE_NAME_CHILD, 5, "2008-12-04");
		long ts = XmlUtilities.findFirstChildAsDateTime(root, NODE_NAME_CHILD);
		assertEquals(1228348800000L, ts);
	}

	// ------------------------------------- fetchDocumentRootFromUrl
	public void testFetchDocumentRootFromUrlThrowsExceptionOnNullOrEmptyUrl() throws Exception {
		checkFetchDocumentRootFromUrlThrowsOn(null);
		checkFetchDocumentRootFromUrlThrowsOn("        ");
	}

	private void checkFetchDocumentRootFromUrlThrowsOn(String requestUrl)
		throws IOException, ParserConfigurationException, SAXException
	{
		try {
			XmlUtilities.fetchDocumentRootFromUrl(null, requestUrl);
			fail();
		} catch (IllegalArgumentException e) {
			// expected condition
			assertIsNotBlankException("requestUrl", e);
		}
	}

	public void testFetchDocumentRootFromUrlRequestsCorrectUrl() throws Exception {
		UrlInputStreamMock stream = new UrlInputStreamMock("<root/>");
		XmlUtilities.fetchDocumentRootFromUrl(stream, REQUEST_URL);
		assertEquals(REQUEST_URL, stream.requestUrl);
	}

	public void testFetchDocumentRootFromUrlReturnsCorrectXmlDocument() throws Exception {
		UrlInputStreamMock stream = new UrlInputStreamMock("<parent><child/></parent>");
		Element root = XmlUtilities.fetchDocumentRootFromUrl(stream, REQUEST_URL);
		assertEquals("parent", root.getNodeName());
		NodeList children = root.getChildNodes();
		assertEquals(1, children.getLength());
		Node child = children.item(0);
		assertEquals("child", child.getNodeName());
		assertEquals(0, child.getChildNodes().getLength());
	}

	// ------------------------------------- helper methods

	private Element constructXmlElement(String name) throws Exception {
		return constructXmlElement(name, null, 0, "test value");
	}

	private Element constructXmlElement(String name, String childName, int numChildren, String value) throws Exception {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElement(name);
		for (int i=0; i < numChildren; i++) {
			Element child = createElement(doc, i, childName, value);
			root.appendChild(child);
		}
		return root;
	}

	private Element createElement(Document doc, int i, String name, String value) {
		Element child = doc.createElement(name);
		child.setAttribute("num", String.valueOf(i));
		Text text = doc.createTextNode(value);
		child.appendChild(text);
		return child;
	}

	private static final class UrlInputStreamResultMock extends UrlInputStreamResult {
		public UrlInputStreamResultMock(String xmlStr) {
			super();
			is = new ByteArrayInputStream(xmlStr.getBytes());
			statusCode = HttpStatus.SC_OK;
		}

		@Override
		public void close() {
		}
	}

	private static final class UrlInputStreamMock implements UrlInputStream {

		private String xmlStr;
		private String requestUrl;

		private UrlInputStreamMock(String xmlStr) {
			this.xmlStr = xmlStr;
		}

		public UrlInputStreamResult fetchStream(String requestUrlArg) {
			this.requestUrl = requestUrlArg;
			return new UrlInputStreamResultMock(xmlStr);
		}
	}
}
