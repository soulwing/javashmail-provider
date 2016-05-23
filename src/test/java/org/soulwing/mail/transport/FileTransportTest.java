/*
 * File created on Aug 25, 2014 
 *
 * Copyright (c) 2014 Virginia Polytechnic Institute and State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.soulwing.mail.transport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

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
 * Tests for {@link FileTransport}.
 *
 * @author Carl Harris
 */
public class FileTransportTest {

  private static final long LISTENER_WAIT = 500;
  private static final long MAX_LISTENER_WAIT = 10*LISTENER_WAIT;

  private SessionFactory sessionFactory;

  private File file;
  
  @Before  
  public void setUp() throws Exception {
    file = File.createTempFile("mail", ".txt");
    sessionFactory = new SessionFactory(defaultProperties(file));
  }
  
  @After
  public void tearDown() throws Exception {
    assertThat(file.delete(), is(true));
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

  @Test
  public void testSendMessage() throws Exception {
    Session session = sessionFactory.newSession();
    Transport transport = session.getTransport();
    MockTransportListener listener = new MockTransportListener();
    transport.addTransportListener(listener);
    Message message = MessageFactory.newMessage("Test message", session);
    transport.sendMessage(message, message.getAllRecipients());
    TransportEvent event = listener.awaitEvent();
    assertThat(event, is(not(nullValue())));
    assertThat(event.getType(), is(equalTo(TransportEvent.MESSAGE_DELIVERED)));
    MimeMessage result = readMessageFromFile(file, session);
    assertThat(result.getFrom(), is(equalTo(message.getFrom())));
    assertThat(result.getRecipients(RecipientType.TO), 
        is(equalTo(message.getRecipients(RecipientType.TO))));
    assertThat(result.getSubject(), is(equalTo(message.getSubject())));
  }

  @Test
  public void testAppendMessage() throws Exception {
    Session session = sessionFactory.newSession(FileTransport.APPEND, "true");
    Transport transport = session.getTransport();
    Message message1 = MessageFactory.newMessage("Message 1", session);
    transport.sendMessage(message1, message1.getAllRecipients());
    Message message2 = MessageFactory.newMessage("Message 2", session);
    transport.sendMessage(message2, message2.getAllRecipients());
    
    BufferedReader reader = new BufferedReader(new FileReader(file));
    try {
      assertThat(findMessageId(reader), is(true));
      assertThat(findMessageId(reader), is(true));
    }
    finally {
      reader.close();
    }
  }

  @Test
  public void testOverwriteMessage() throws Exception {
    Session session = sessionFactory.newSession(FileTransport.APPEND, "false");
    Transport transport = session.getTransport();
    Message message1 = MessageFactory.newMessage("Message 1", session);
    transport.sendMessage(message1, message1.getAllRecipients());
    Message message2 = MessageFactory.newMessage("Message 2", session);
    transport.sendMessage(message2, message2.getAllRecipients());
    
    BufferedReader reader = new BufferedReader(new FileReader(file));
    try {
      assertThat(findMessageId(reader), is(true));
      assertThat(findMessageId(reader), is(false));
    }
    finally {
      reader.close();
    }
  }

  private MimeMessage readMessageFromFile(File file, Session session)
      throws IOException, MessagingException {
    InputStream inputStream = new FileInputStream(file);
    try {
      return new MimeMessage(session, inputStream);
    }
    finally {
      try {
        inputStream.close();        
      }
      catch (IOException ex) {
        assert true;  // ignore it
      }
    }
  }
  
  private boolean findMessageId(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    while (line != null) {
      if (line.startsWith("Message-ID")) return true;
      line = reader.readLine();
    }
    return false;
  }

  private static Properties defaultProperties(File file) {
    Properties properties = new Properties();
    properties.setProperty(FileTransport.FILE_PATH, file.toString());
    properties.setProperty("mail.transport.protocol", "file");
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


