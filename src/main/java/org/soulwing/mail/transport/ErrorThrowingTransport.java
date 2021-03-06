/*
 * File created on Nov 5, 2018
 *
 * Copyright (c) 2018 Carl Harris, Jr
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

import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

/**
 * A JavaMail {@link javax.mail.Transport} that throws an exception on an
 * attempt to send a message.
 *
 * @author Carl Harris
 */
public class ErrorThrowingTransport extends DelegatingTransport {

  private static final String ERROR_PROVIDER = "mail.error";
  public static final String DEFAULT_ERROR = ERROR_PROVIDER + ".message";

  private final SessionProperties properties;

  public ErrorThrowingTransport(Session session, URLName urlname) {
    super(session, urlname);
    properties = new SessionProperties(session);
  }

  @Override
  public void sendMessage(Message message, Address[] addresses)
      throws MessagingException {
    MessagingException mex = ErrorHeader.getErrorToThrow(message);
    if (mex == null && properties.getProperty(DEFAULT_ERROR) != null) {
      mex = new MessagingException(properties.getProperty(DEFAULT_ERROR));
    }
    if (mex != null) throw mex;
    super.sendMessage(message, addresses);
  }

}
