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
package com.fraudwall.util.net;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;

import com.fraudwall.util.AbstractPropsTest;
import com.fraudwall.util.FWProps;
import com.fraudwall.util.net.EmailUtils;


public class EmailUtilsTest extends AbstractPropsTest {

	private static final String TEXT_PLAIN = "text/plain";
	private static final String MESSAGE_BODY = "testing sending email";
	private static final String[] TO = {"to1@foo.org", "to2@foo.org", "to3@foo.org"};
	private static final String FROM = "from@foo.org";
	private static final String SUBJECT = "test";

	@Override
	public void setUp() throws Exception {
		super.setUp(null, TEST_CUSTOMER);
	}

	// =========================================== makeMessage

	public void testMakeMessageThrowsExceptionWhenFromIsNullOrBlank() throws Exception {
		checkMakeMessageThrowsForFrom(null);
		checkMakeMessageThrowsForFrom("");
		checkMakeMessageThrowsForFrom(" ");
	}

	private void checkMakeMessageThrowsForFrom(String from) throws MessagingException, AddressException {
		try {
			EmailUtils.makeMessage(TO, null, null, from, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
				/*highPriorityEmail=*/ false);
			fail();
		} catch (IllegalArgumentException e) {
			assertIsNotBlankException("from", e);
		}
	}

	public void testMakeMessageThrowsExceptionWhenSubjectIsNullOrBlank() throws Exception {
		checkMakeMessageThrowsForSubject(null);
		checkMakeMessageThrowsForSubject("");
		checkMakeMessageThrowsForSubject(" ");
	}

	private void checkMakeMessageThrowsForSubject(String subject) throws MessagingException, AddressException {
		try {
			EmailUtils.makeMessage(TO, null, null, FROM, null, subject, MESSAGE_BODY, TEXT_PLAIN,
				/*highPriorityEmail=*/ false);
			fail();
		} catch (IllegalArgumentException e) {
			assertIsNotBlankException("subject", e);
		}
	}

	public void testMakeMessageThrowsExceptionWhenMessageBodyIsNullOrBlank() throws Exception {
		checkMakeMessageThrowsForMessageBody(null);
		checkMakeMessageThrowsForMessageBody("");
		checkMakeMessageThrowsForMessageBody(" ");
	}

	private void checkMakeMessageThrowsForMessageBody(String msgBody) throws MessagingException, AddressException {
		try {
			EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, msgBody, TEXT_PLAIN, /*highPriorityEmail=*/ false);
			fail();
		} catch (IllegalArgumentException e) {
			assertIsNotBlankException("msgBody", e);
		}
	}

	public void testMakeMessageThrowsExceptionWhenMimeTypeIsNullOrBlank() throws Exception {
		checkMakeMessageThrowsForMimeType(null);
		checkMakeMessageThrowsForMimeType("");
		checkMakeMessageThrowsForMimeType(" ");
	}

	private void checkMakeMessageThrowsForMimeType(String mimeType) throws MessagingException, AddressException {
		try {
			EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, mimeType,
				/*highPriorityEmail=*/ false);
			fail();
		} catch (IllegalArgumentException e) {
			assertIsNotBlankException("mimeType", e);
		}
	}

	public void testMakeMessageSetsCorrectToRecipients() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		checkRecipients(msg, RecipientType.TO, TO);
	}

	public void testMakeMessageSetsCorrectCCRecipients() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		checkRecipients(msg, RecipientType.CC, null);
		String[] cc = {"cc@foo.net", "cc2@foo.net"};
		msg = EmailUtils.makeMessage(TO, cc, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		checkRecipients(msg, RecipientType.CC, cc);
	}

	public void testMakeMessageSetsCorrectBCCRecipients() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		checkRecipients(msg, RecipientType.BCC, null);
		String[] bcc = {"bcc@foo.net", "bcc2@foo.net"};
		msg = EmailUtils.makeMessage(TO, null, bcc, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		checkRecipients(msg, RecipientType.BCC, bcc);
	}

	private void checkRecipients(Message msg, RecipientType type, String[] expected) throws Exception {
		Address[] recipients = msg.getRecipients(type);
		if (expected == null) {
			assertNull(recipients);
		} else {
			assertEquals(expected.length, recipients.length);
			for (int i=0; i < recipients.length; i++) {
				assertEquals(expected[i], recipients[i].toString());
			}
		}
	}

	public void testMakeMessageSetsCorrectSender() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		assertSingleAddress(FROM, msg.getFrom());
	}

	private void assertSingleAddress(String expected, Address[] addresses) {
		assertEquals(1, addresses.length);
		assertEquals(expected, addresses[0].toString());
	}

	public void testMakeMessageSetsCorrectSubject() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		assertEquals(SUBJECT, msg.getSubject());
	}

	public void testMakeMessageSetsCorrectMessageBody() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		Object obj = msg.getContent();
		assertInstanceOf(obj, String.class);
		assertEquals(MESSAGE_BODY, obj);
	}

	public void testMakeMessageSetsCorrectMimeType() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		assertEquals(TEXT_PLAIN, msg.getContentType());
	}

	public void testMakeMessageSetsCorrectReplyTo() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		assertSingleAddress(FROM, msg.getReplyTo());
		String replyTo = "reply@foo.org";
		msg = EmailUtils.makeMessage(TO, null, null, FROM, replyTo, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		assertSingleAddress(replyTo, msg.getReplyTo());
	}

	public void testMakeMessageSetsHighPriorityEmailWhenHighPriorityEmailIsTrue() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ true);
		assertHeaderEquals(msg, "X-Priority", "1");
		assertHeaderEquals(msg, "X-MSMail-Priority", "High");
		assertHeaderEquals(msg, "Importance", "High");
	}

	public void testMakeMessageDoesNotSetHighPriorityEmailWhenHighPriorityEmailIsFalse() throws Exception {
		Message msg = EmailUtils.makeMessage(TO, null, null, FROM, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN,
			/*highPriorityEmail=*/ false);
		assertHeaderNotSet(msg, "X-Priority");
		assertHeaderNotSet(msg, "X-MSMail-Priority");
		assertHeaderNotSet(msg, "Importance");
	}

	private void assertHeaderNotSet(Message msg, String headerName) throws MessagingException {
		assertNull(msg.getHeader(headerName));
	}

	private void assertHeaderEquals(Message msg, String headerName, String headerValue) throws MessagingException {
		assertEquals(headerValue, msg.getHeader(headerName)[0]);
	}

	public static void main(String[] args) throws Exception {
		FWProps.initialize(null, TEST_CUSTOMER);
		String email = "kfox@anchorintelligence.com";
		String[] to = {email};
		EmailUtils.send(to, null, null, email, null, SUBJECT, MESSAGE_BODY, TEXT_PLAIN);
		System.out.println("Email sent successfully");
	}
}
