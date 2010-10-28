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

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.fraudwall.util.FWProps;
import com.fraudwall.util.exc.ArgCheck;

/**
 * Utility class for sending email.
 *
 * @author aheydon, kfox
 */
public class EmailUtils {

	private static final String MAIL_SMTP_HOST = "mail.smtp.host";
	private static final String MAIL_DEBUG = "mail.debug";

	public static void send(
			String[] to, String[] cc, String[] bcc, String from, String replyTo,
			String subject, String msgBody, String mimeType)
			throws MessagingException
	{
			Transport.send(makeMessage(to, cc, bcc, from, replyTo, subject, msgBody, mimeType,
				/*highPriorityEmail=*/ false));
	}

	public static void send(
		String[] to, String[] cc, String[] bcc, String from, String replyTo,
		String subject, String msgBody, String mimeType, boolean highPriorityEmail)
		throws MessagingException
	{
		Transport.send(makeMessage(to, cc, bcc, from, replyTo, subject, msgBody, mimeType, highPriorityEmail));
	}

	/*test*/ static Message makeMessage(
		String[] to, String[] cc, String[] bcc, String from, String replyTo,
		String subject, String msgBody, String mimeType, boolean highPriorityEmail)
		throws MessagingException, AddressException
	{
		ArgCheck.isNotBlank(from, "from");
		ArgCheck.isNotBlank(subject, "subject");
		ArgCheck.isNotBlank(msgBody, "msgBody");
		ArgCheck.isNotBlank(mimeType, "mimeType");

		// set the host smtp address
		Properties props = new Properties();
		props.put(MAIL_SMTP_HOST, FWProps.getProperty(MAIL_SMTP_HOST));
		props.put(MAIL_DEBUG, FWProps.getBooleanProperty(MAIL_DEBUG));

		// create the default session
		Session session = Session.getDefaultInstance(props);

		// create a message
		Message msg = new MimeMessage(session);

		if (highPriorityEmail) {
			msg.addHeader("X-Priority", "1");
			msg.addHeader("X-MSMail-Priority", "High");
			msg.addHeader("Importance", "High");
		}

		// set the addresses
		msg.setFrom(new InternetAddress(from));
		addRecipients(msg, RecipientType.TO, to);
		addRecipients(msg, RecipientType.CC, cc);
		addRecipients(msg, RecipientType.BCC, bcc);

		if (replyTo != null) {
			msg.setReplyTo(new Address[]{new InternetAddress(replyTo)});
		}

		// set subject, message and content type
		msg.setSubject(subject);
		msg.setContent(msgBody, mimeType);
		return msg;
	}

	private static void addRecipients(Message msg, RecipientType recipientType, String[] recipients)
		throws MessagingException {
		if (recipients != null) {
			for (String recipient : recipients) {
				msg.addRecipient(recipientType, new InternetAddress(recipient));
			}
		}
	}
}
