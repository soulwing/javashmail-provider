/*
 * File created on May 21, 2016
 *
 * Copyright (c) 2016 Carl Harris, Jr
 * and others as noted
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soulwing.mail.transport;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A factory that produces JavaMail {@link Message} objects.
 * @author Carl Harris
 */
class MessageFactory {

  static Message newMessage(String subject, Session session)
      throws MessagingException {
    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress("nobody@nowhere.net"));
    message.addRecipient(MimeMessage.RecipientType.TO,
        new InternetAddress("nobody@nowhere.net"));
    message.setSubject(subject);
    message.setText("This is a test. This is only a test.");
    return message;
  }

}
