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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.fraudwall.util.date.SmartDateParser;
import com.fraudwall.util.exc.ArgCheck;

public class XmlUtilities {

	public static final String UNKNOWN_STRING = null;
	public static final long UNKNOWN_TIME = SmartDateParser.UNKNOWN_TIME;
	public static final long UNKNOWN_IP = 0L;
	public static final long UNKNOWN_LONG = -1L;
	public static final int UNKNOWN_INT = -1;
	public static final BigDecimal UNKNOWN_BIG_DECIMAL = new BigDecimal("-1");

	private static final Log log = LogFactory.getLog(XmlUtilities.class);

	public static String getAttribute(Element node, String attributeName) {
		return node.getAttribute(attributeName);
	}

	public static String getRequiredAttribute(Element node, String attributeName) {
		String s = node.getAttribute(attributeName);
		if (StringUtils.isNotBlank(s))
			return s;
		throw new java.lang.IllegalArgumentException("Missing attribute '"
				+ attributeName + "' in " + node);
	}

	public static boolean getBooleanAttribute(Element node, String attributeName) {
		return parseBooleanAttribute(node.getAttribute(attributeName));
	}

	public static boolean parseBooleanAttribute(String s) {
		return !StringUtils.isEmpty(s) && Boolean.parseBoolean(s);
	}

	public static int getIntegerAttribute(Element node, String attributeName,
			int defaultValue) {
		String s = node.getAttribute(attributeName);
		if (StringUtils.isEmpty(s))
			return defaultValue;
		return Integer.parseInt(s);
	}

	public static char getCharAttribute(Element node, String attributeName, char defaultValue) {
		String s = node.getAttribute(attributeName);
		if (!StringUtils.isEmpty(s) && s.equals("\\t")) {
			return '\t';
		} else if (!StringUtils.isEmpty(s) && s.equals("\\0")) {
			return '\0';
		} else if (s.length() == 1) {
			return s.charAt(0);
		} else {
			return defaultValue;
		}
	}

	/**
	 * Returns a node list with the descendents of this node whose tag name is <code>tagName</code>.
	 */
	public static NodeList findElements(Element root, String tagName) {
		ArgCheck.isNotNull(root, "root");
		ArgCheck.isNotBlank(tagName, "tagName");
		return root.getElementsByTagName(tagName);
	}

	/**
	 * Returns the integer value that is contained in the first {@link Element} in the tree rooted at
	 * <code>root</code> named <code>tagName</code>.
	 */
	public static int findFirstElementAsInteger(Element root, String tagName) {
		String intStr = findFirstElementAsText(root, tagName);
		return StringUtils.isBlank(intStr) ? UNKNOWN_INT : Integer.parseInt(intStr.trim());
	}

	/**
	 * Returns the date value that is contained in the first {@link Element} of the tree rooted at
	 * <code>root</code> named <code>tagName</code>.
	 */
	public static long findFirstElementAsDateTime(Element root, String tagName) {
		String dateStr = findFirstElementAsText(root, tagName);
		return getTimestampFromString(dateStr);
	}

	/**
	 * Returns the text value that is contained in the first {@link Element} of the tree rooted at
	 * <code>root</code> named <code>tagName</code>.
	 */
	public static String findFirstElementAsText(Element root, String tagName) {
		Element element = findFirstElement(root, tagName);
		return element == null ? UNKNOWN_STRING : element.getTextContent();
	}

	/**
	 * Returns the first {@link Element} of the tree rooted at <code>root</code> named <code>tagName</code>.
	 */
	public static Element findFirstElement(Element root, String tagName) {
		NodeList elements = findElements(root, tagName);
		return elements.getLength() == 0 ? null : (Element)elements.item(0);
	}

	/**
	 * Returns the text value that is contained in the first child {@link Element} of
	 * <code>root</code> named <code>tagName</code>.
	 */
	public static String findFirstChildAsText(Element root, String tagName) {
		Element element = findFirstChild(root, tagName);
		return element == null ? UNKNOWN_STRING : element.getTextContent();
	}

	/**
	 * Returns the date time value that is contained in the first child {@link Element} of
	 * <code>root</code> named <code>tagName</code>.
	 */
	public static long findFirstChildAsDateTime(Element root, String tagName) {
		String dateStr = findFirstChildAsText(root, tagName);
		return getTimestampFromString(dateStr);
	}

	private static long getTimestampFromString(String dateStr) {
		return StringUtils.isBlank(dateStr) ? UNKNOWN_TIME : SmartDateParser.parseDateAsLong(dateStr);
	}

	/**
	 * Returns the first child {@link Element} of <code>element</code> named <code>tagName</code>.
	 */
	public static Element findFirstChild(Element element, String tagName) {
		ArgCheck.isNotNull(element, "element");
		ArgCheck.isNotBlank(tagName, "tagName");
		NodeList nodes = element.getChildNodes();
		if (nodes != null) {
			for (int i=0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element e = (Element)node;
					if (tagName.equals(e.getNodeName())) {
						return e;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Thrown to indicate an error parsing an XML file.
	 *
	 * @see #parseXmlFile(File)
	 */
	@SuppressWarnings("serial")
	public static class XMLParseException extends Exception {
		public XMLParseException(String message, Exception e) {
			super(message, e);
		}
	}

	/**
	 * Returns the top-level {@link Element} of the {@link Document} that results
	 * from parsing the given XML <code>file</code>.
	 *
	 * @throws XMLParseException if an I/O error occurred while reading the XML file
	 * or if the XML file is invalid with respect to its DTD. This exception will
	 * wrap the underlying cause.
	 */
	public static Element parseXmlFile(File file) throws XMLParseException {
		return parseXmlFile(file, /*doValidation=*/ true);
	}

	/**
	 * Returns the top-level {@link Element} of the {@link Document} that results
	 * from parsing the given XML <code>file</code>.
	 *
	 * @throws XMLParseException if an I/O error occurred while reading the XML file
	 * or if the XML file is invalid with respect to its DTD. This exception will
	 * wrap the underlying cause.
	 */
	public static Element parseXmlFile(File file, boolean doValidation) throws XMLParseException {
		try {
			DocumentBuilder db = createXmlDocumentBuilder(doValidation);
			Document d = db.parse(file);
			return d.getDocumentElement();
		} catch (Exception e) {
			throw new XMLParseException("Error parsing XML file: " + file.getPath(), e);
		}
	}

	/**
	 * Returns the top-level {@link Element} of the {@link Document} that results
	 * from parsing the given XML <code>String</code>.  This does not do type checking against the DTD.
	 *
	 * @throws XMLParseException if an I/O error occurred while reading the XML String.
	 * This exception will wrap the underlying cause.
	 */
	public static Element parseXmlFile(String xmlString) throws XMLParseException {
		try {
			DocumentBuilder db = createXmlDocumentBuilder(false);
			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlString));
			Document d = db.parse(inStream);
			return d.getDocumentElement();
		} catch (Exception e) {
			throw new XMLParseException("Error parsing XML string: " + xmlString, e);
		}
	}

	/**
	 * Returns a {@link DocumentBuilder} configured to validate parsed XML documents against
	 * their DTD and to throw a {@link SAXParseException} in the event of an error.
	 */
	public static DocumentBuilder createXmlDocumentBuilder()
		throws ParserConfigurationException
	{
		return createXmlDocumentBuilder(true);
	}

	/**
	 * Returns a {@link DocumentBuilder} for parsing XML documents.
	 *
	 * @param doValidation indicates whether or not the resulting DocumentBuilder is
	 * configured to validate parsed XML documents against their DTD and to throw
	 * {@link SAXParseException} in the event of an error.
	 */
	public static DocumentBuilder createXmlDocumentBuilder(boolean doValidation)
		throws ParserConfigurationException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(doValidation);
		DocumentBuilder db = dbf.newDocumentBuilder();
		db.setErrorHandler(new FatalErrorHandler());
		return db;
	}

	/**
	 * Make a request to the specified Url and return the parsed document root.
	 *
	 * @param requestUrl The http url to fetch the XML document from.
	 * @return the root {@link Element} of the XML document.
	 */
	public static Element fetchDocumentRootFromUrl(String requestUrl)
		throws IOException, ParserConfigurationException, SAXException
	{
		return fetchDocumentRootFromUrl(UrlInputStreamImpl.getInstance(), requestUrl);
	}

	/*test*/ static Element fetchDocumentRootFromUrl(UrlInputStream urlInputStream, String requestUrl)
		throws IOException, ParserConfigurationException, SAXException
	{
		ArgCheck.isNotBlank(requestUrl, "requestUrl");
		UrlInputStreamResult stream = null;
		try {
			stream = urlInputStream.fetchStream(requestUrl);
			return getRootElement(stream.is);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	public static Element getRootElement(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		ArgCheck.isNotNull(in, "in");
		InputSource inputSource = new InputSource(in);
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = parser.parse(inputSource);
		return document.getDocumentElement();
	}

	private static class FatalErrorHandler implements ErrorHandler {
		public void error(SAXParseException exception) throws SAXException {
			throw exception;
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			throw exception;
		}

		public void warning(SAXParseException exception) {
			log.warn("XML parse warning", exception);
		}
	}

	/*test*/ static class UrlInputStreamResult {
		public int statusCode;
		public InputStream is;
		private HttpMethod method;

		protected UrlInputStreamResult() {
		}

		public UrlInputStreamResult(HttpClient client, String requestUrl) throws HttpException, IOException {
			method = new GetMethod(requestUrl);
			statusCode = client.executeMethod(method);
			is = method.getResponseBodyAsStream();
		}

		public void close() throws IOException {
			if (is != null) {
				is.close();
				is = null;
			}
			method.releaseConnection();
			method = null;
		}
	}

	/*test*/ static interface UrlInputStream {
		public UrlInputStreamResult fetchStream(String requestUrl) throws HttpException, IOException;
	}

	private static final class UrlInputStreamImpl implements UrlInputStream {

		private static final UrlInputStream singleton = new UrlInputStreamImpl();
		public static final int DEFAULT_TIMEOUT = 10;

		private final HttpConnectionManager hcm;
		private final HttpClient client;

		private UrlInputStreamImpl() {
			int timeToWaitForResponse = (int) (DEFAULT_TIMEOUT * DateUtils.MILLIS_PER_SECOND);
			HttpConnectionManagerParams hmcp = new HttpConnectionManagerParams();
			hmcp.setSoTimeout(timeToWaitForResponse);
			hmcp.setConnectionTimeout(timeToWaitForResponse);
			hcm = new MultiThreadedHttpConnectionManager();
			hcm.setParams(hmcp);
			client = new HttpClient(hcm);

			String proxyHost = FWProps.getStringProperty("http.proxyHost");
			int proxyPort = FWProps.getIntegerProperty("http.proxyPort");
			if (StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
				client.getHostConfiguration().setProxy(proxyHost, proxyPort);
			}
		}

		private static UrlInputStream getInstance() {
			return singleton;
		}

		public UrlInputStreamResult fetchStream(String requestUrl) throws HttpException, IOException {
			return new UrlInputStreamResult(client, requestUrl);
		}
	}

}
