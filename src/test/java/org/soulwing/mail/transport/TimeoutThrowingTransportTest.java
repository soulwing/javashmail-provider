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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.junit.After;
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

    ConnectionEvent event = listener.awaitEvent();
    assertThat(event, is(not(nullValue())));
    assertThat(event.getType(), is(equalTo(ConnectionEvent.OPENED)));
    listener.reset();
    transport.close();
    event = listener.awaitEvent();
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

  private static abstract class AbstractEventListener<T> {

    private final Lock lock = new ReentrantLock();
    private final Condition readyCondition = lock.newCondition();

    private T event;

    void reset() {
      event = null;
    }

    T awaitEvent() throws InterruptedException {
      lock.lock();
      try {
        final long start = System.currentTimeMillis();
        long now = start;
        while (event == null && (now - start) < MAX_LISTENER_WAIT) {
          readyCondition.await(LISTENER_WAIT, TimeUnit.MILLISECONDS);
          now = System.currentTimeMillis();
        }
        return event;
      }
      finally {
        lock.unlock();
      }
    }

    void receiveEvent(T event) {
      lock.lock();
      try {
        this.event = event;
        readyCondition.signalAll();
      }
      finally {
        lock.unlock();
      }
    }

  }

  private static class MockConnectionListener
      extends AbstractEventListener<ConnectionEvent>
      implements ConnectionListener {

    @Override
    public void opened(ConnectionEvent connectionEvent) {
      receiveEvent(connectionEvent);
    }

    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
      receiveEvent(connectionEvent);
    }

    @Override
    public void closed(ConnectionEvent connectionEvent) {
      receiveEvent(connectionEvent);
    }

  }

  private static class MockTransportListener
      extends AbstractEventListener<TransportEvent>
      implements TransportListener {

    @Override
    public void messageDelivered(TransportEvent transportEvent) {
      receiveEvent(transportEvent);
    }

    @Override
    public void messageNotDelivered(TransportEvent transportEvent) {
      receiveEvent(transportEvent);
    }

    @Override
    public void messagePartiallyDelivered(TransportEvent transportEvent) {
      receiveEvent(transportEvent);
    }

  }

}


