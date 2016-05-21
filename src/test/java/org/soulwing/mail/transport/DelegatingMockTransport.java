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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportListener;

/**
 * A transport that delegates to a mock {@link Transport}.
 *
 * @author Carl Harris
 */
public class DelegatingMockTransport extends Transport {

  private static Transport delegateMock;

  public DelegatingMockTransport(Session session, URLName urlname) {
    super(session, urlname);
  }

  public static Transport getDelegateMock() {
    return delegateMock;
  }

  public static void setDelegateMock(Transport delegateMock) {
    DelegatingMockTransport.delegateMock = delegateMock;
  }

  @Override
  public void connect() throws MessagingException {
    delegateMock.connect();
  }

  @Override
  public void connect(String host, String user, String password)
      throws MessagingException {
    delegateMock.connect(host, user, password);
  }

  @Override
  public void connect(String user, String password) throws MessagingException {
    delegateMock.connect(user, password);
  }

  @Override
  public synchronized void connect(String host, int port, String user,
      String password) throws MessagingException {
    delegateMock.connect(host, port, user, password);
  }

  @Override
  public boolean isConnected() {
    return delegateMock.isConnected();
  }

  @Override
  public void close() throws MessagingException {
    delegateMock.close();
  }

  @Override
  public void addConnectionListener(ConnectionListener l) {
    delegateMock.addConnectionListener(l);
  }

  @Override
  public void removeConnectionListener(ConnectionListener l) {
    delegateMock.removeConnectionListener(l);
  }

  @Override
  public void addTransportListener(TransportListener l) {
    delegateMock.addTransportListener(l);
  }

  @Override
  public void removeTransportListener(TransportListener l) {
    delegateMock.removeTransportListener(l);
  }

  @Override
  public void sendMessage(Message message, Address[] addresses)
      throws MessagingException {
    delegateMock.sendMessage(message, addresses);
  }

}
