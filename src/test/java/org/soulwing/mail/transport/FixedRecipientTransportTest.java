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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit tests for {@link FixedRecipientTransport}.
 *
 * @author Carl Harris
 */
public class FixedRecipientTransportTest {

  private static final String HOST = "host";
  private static final int PORT = 1;
  private static final String USER = "user";
  private static final String PASSWORD = "password";

  private static final URLName URL_NAME =
      new URLName(FixedRecipientTransport.PROTOCOL, HOST, PORT, "file",
          USER, PASSWORD);

  private static final String DELEGATE_NAME = "some delegate name";
  private static final String RECIPIENT_ADDRESS = "nobody@nowhere.net";

  @Rule
  public final JUnitRuleMockery context =
      new JUnitRuleClassImposterizingMockery();

  @Mock
  ConnectionListener connectionListener;

  @Mock
  TransportListener transportListener;

  @Mock
  Transport delegateTransport;

  private SessionFactory sessionFactory;
  private Session delegateSession;

  @Before
  public void setUp() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", FixedRecipientTransport.PROTOCOL);
    properties.setProperty(FixedRecipientTransport.DELEGATE, DELEGATE_NAME);
    properties.setProperty(FixedRecipientTransport.LOCATOR_CLASS,
        MockJndiObjectLocator.class.getName());
    sessionFactory = new SessionFactory(properties);
    DelegatingMockTransport.setDelegateMock(delegateTransport);
    delegateSession = newDelegateSession();
    MockJndiObjectLocator.setObject(delegateSession);
  }

  private static Session newDelegateSession() {
    final Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "mock");
    return Session.getInstance(properties);
  }

  @Test
  public void testConnect() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).connect();
      }
    });

    getTransport().connect();
  }

  @Test
  public void testConnectUserPassword() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).connect(USER, PASSWORD);
      }
    });

    getTransport().connect(USER, PASSWORD);
  }

  @Test
  public void testConnectHostUserPassword() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).connect(HOST, USER, PASSWORD);
      }
    });

    getTransport().connect(HOST, USER, PASSWORD);
  }

  @Test
  public void testConnectHostPortUserPassword() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).connect(HOST, PORT, USER, PASSWORD);
      }
    });

    getTransport().connect(HOST, PORT, USER, PASSWORD);
  }

  @Test
  public void testIsConnected() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).isConnected();
        will(returnValue(true));
      }
    });

    assertThat(getTransport().isConnected(), is(true));
  }

  @Test
  public void testClose() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).close();
      }
    });

    getTransport().close();
  }

  @Test
  public void testAddConnectionListener() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).addConnectionListener(connectionListener);
      }
    });

    getTransport().addConnectionListener(connectionListener);
  }

  @Test
  public void testRemoveConnectionListener() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).removeConnectionListener(connectionListener);
      }
    });

    getTransport().removeConnectionListener(connectionListener);
  }

  @Test
  public void testAddTransportListener() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).addTransportListener(transportListener);
      }
    });

    getTransport().addTransportListener(transportListener);
  }

  @Test
  public void testRemoveTransportListener() throws Exception {
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).removeTransportListener(transportListener);
      }
    });

    getTransport().removeTransportListener(transportListener);
  }

  @Test
  public void testSendMessage() throws Exception {
    final Address address = new InternetAddress(RECIPIENT_ADDRESS);
    final Message message = context.mock(Message.class);
    context.checking(new Expectations() {
      {
        oneOf(delegateTransport).sendMessage(with(message),
            with(arrayContaining(address)));
      }
    });


    getTransport(FixedRecipientTransport.ADDRESS, RECIPIENT_ADDRESS)
        .sendMessage(message, new Address[0]);
  }

  @Test(expected = MessagingException.class)
  public void testSendMessageWhenNoRecipientAddress() throws Exception {
    final Message message = context.mock(Message.class);
    getTransport().sendMessage(message, new Address[0]);
  }

  private Transport getTransport(String... sessionProperties)
      throws MessagingException {
    final Session session = sessionFactory.newSession(sessionProperties);
    final Transport transport = session.getTransport();
    assertThat(transport, is(instanceOf(FixedRecipientTransport.class)));
    return transport;
  }

}
