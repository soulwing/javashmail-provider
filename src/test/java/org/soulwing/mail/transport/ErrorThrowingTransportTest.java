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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.ConnectionEvent;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ErrorThrowingTransport}.
 *
 * @author Carl Harris
 */
public class ErrorThrowingTransportTest {

  private static final long LISTENER_WAIT = 500;
  private static final long MAX_LISTENER_WAIT = 10*LISTENER_WAIT;

  private SessionFactory sessionFactory;

  @Before
  public void setUp() throws Exception {
    sessionFactory = new SessionFactory(defaultProperties());
  }

  @Test
  public void testConnect() throws Exception {
    Session session = sessionFactory.newSession();
    Transport transport = session.getTransport();
    MockConnectionListener listener = new MockConnectionListener();
    transport.addConnectionListener(listener);

    transport.connect();

    ConnectionEvent event = listener.awaitEvent(LISTENER_WAIT, MAX_LISTENER_WAIT);
    assertThat(event, is(not(nullValue())));
    assertThat(event.getType(), is(equalTo(ConnectionEvent.OPENED)));
    listener.reset();
    transport.close();
    event = listener.awaitEvent(LISTENER_WAIT, MAX_LISTENER_WAIT);
    assertThat(event, is(not(nullValue())));
    assertThat(event.getType(), is(equalTo(ConnectionEvent.CLOSED)));
  }

  @Test(expected = MessagingException.class)
  public void testSendMessage() throws Exception {
    Session session = sessionFactory.newSession();
    Transport transport = session.getTransport();
    MockTransportListener listener = new MockTransportListener();
    transport.addTransportListener(listener);
    Message message = MessageFactory.newMessage("Test message", session);
    transport.sendMessage(message, message.getAllRecipients());
  }

  @Test
  public void testSendMessageWithCustomErrorMessage() throws Exception {
    Session session = sessionFactory.newSession();
    Transport transport = session.getTransport();
    MockTransportListener listener = new MockTransportListener();
    transport.addTransportListener(listener);
    Message message = MessageFactory.newMessage("Test message", session);
    message.setHeader(ErrorHeader.ERROR_HEADER, "Test error");
    try {
      transport.sendMessage(message, message.getAllRecipients());
      fail("expected MessagingException");
    }
    catch (MessagingException ex) {
      assertThat(ex.getMessage(), is(equalTo("Test error")));
    }
  }

  private static Properties defaultProperties() {
    final Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "error");
    return properties;
  }

}


