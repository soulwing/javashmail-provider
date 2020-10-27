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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ErrorThrowingTransport}.
 *
 * @author Carl Harris
 */
public class TimeoutThrowingTransportTest {

  private static final long LISTENER_WAIT = 500;
  private static final long MAX_LISTENER_WAIT = 10*LISTENER_WAIT;

  @Test(expected = MessagingException.class)
  public void testConnect() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "timeout");
    properties.setProperty("timeout.connectionTimeout", "10");
    SessionFactory sessionFactory = new SessionFactory(properties);

    Session session = sessionFactory.newSession();
    Transport transport = session.getTransport();
    MockConnectionListener listener = new MockConnectionListener();
    transport.addConnectionListener(listener);

    transport.connect();
  }

  @Test(expected = MessagingException.class)
  public void testSendMessage() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "timeout");
    properties.setProperty("timeout.connectionTimeout", "-1");
    properties.setProperty("timeout.messageTimeout", "10");
    SessionFactory sessionFactory = new SessionFactory(properties);

    Session session = null;
    try {
      session = sessionFactory.newSession();
    } catch (MessagingException mX) {
      Assert.fail("Exception should NOT have been thrown on connection");
    }
    Transport transport = session.getTransport();
    org.soulwing.mail.transport.MockTransportListener listener = new org.soulwing.mail.transport.MockTransportListener();
    transport.addTransportListener(listener);
    Message message = MessageFactory.newMessage("Test message", session);
    transport.sendMessage(message, message.getAllRecipients());
  }

}


