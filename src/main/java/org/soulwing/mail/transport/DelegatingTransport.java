/*
 * File created on Oct 27, 2020
 *
 * Copyright (c) 2020 Carl Harris, Jr
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
import javax.mail.Transport;
import javax.mail.URLName;

/**
 * A {@link Transport} that delegates to the transport for another session.
 *
 * @author Carl Harris
 */
abstract class DelegatingTransport extends Transport {

  private final Session delegate;

  protected DelegatingTransport(Session session, URLName urlname) {
    super(session, urlname);
    final SessionProperties properties = new SessionProperties(session);
    final Properties delegateProperties = properties.getDelegateProperties(
        urlname.getProtocol());
    if (delegateProperties.isEmpty()) {
      throw new IllegalArgumentException("must include delegate session properties");
    }
    delegate = session(delegateProperties);
  }

  protected static Session session(Properties properties) {
    try {
      return Session.getDefaultInstance(properties);
    }
    catch (SecurityException ex) {
      return Session.getInstance(properties);
    }
  }

  @Override
  public void connect(String host, int port, String user, String password)
      throws MessagingException {
    super.connect("delegating transport", -1, null, null);
    delegate.getTransport().connect();
  }

  @Override
  protected boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
    return true;
  }

  @Override
  public void sendMessage(Message message, Address[] addresses)
      throws MessagingException {
    delegate.getTransport().sendMessage(message, addresses);
  }

}
