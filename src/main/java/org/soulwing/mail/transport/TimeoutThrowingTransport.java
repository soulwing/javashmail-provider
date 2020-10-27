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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

/**
 * A JavaMail {@link Transport} that times out when connecting or sending
 * a message
 *
 * @author Chris Beckey
 */
public class TimeoutThrowingTransport extends DelegatingTransport {

  private final long connectionTimeout;
  private final long messageTimeout;

  public TimeoutThrowingTransport(Session session, URLName urlname) {
    super(session, urlname);

    SessionProperties properties = new SessionProperties(session);
    connectionTimeout = properties.getLongProperty("timeout.connectionTimeout", -1);
    messageTimeout = properties.getLongProperty("timeout.messageTimeout", -1);
  }

  @Override
  public void connect(String host, String user, String password) throws MessagingException {
    if (connectionTimeout > 0) {
      try {
        Thread.sleep(connectionTimeout);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      throw new MessagingException("Timeout exception connecting to host");
    }

    super.connect(host, -1, user, password);
  }

  @Override
  public void sendMessage(Message message, Address[] addresses)
      throws MessagingException {
    if (messageTimeout > 0) {
      try {
        Thread.sleep(messageTimeout);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      throw new MessagingException("Timeout exception sending message");
    }
    super.sendMessage(message, addresses);
  }

}
