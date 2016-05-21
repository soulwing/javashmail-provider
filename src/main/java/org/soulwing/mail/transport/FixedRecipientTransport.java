/*
 * File created on Dec 14, 2014 
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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.ConnectionListener;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.naming.NamingException;

/**
 * A {@link Transport} that delivers mail to a configurable envelope
 * recipient, ignoring any other envelope recipients.  Delivery is delegated
 * to another {@link Transport}.
 *
 * @author Carl Harris
 */
public class FixedRecipientTransport extends Transport {
  
  private static final Logger logger = Logger.getLogger(
      FixedRecipientTransport.class.getName());

  public static final String PROTOCOL = "rcpt";
  
  private static final String PREFIX = "mail." + PROTOCOL;
  
  public static final String DELEGATE = PREFIX + ".delegate";
  public static final String ADDRESS = PREFIX + ".address";
  public static final String LOCATOR_CLASS = PREFIX + ".locatorClass";

  private final SessionProperties properties;

  private final Lock lock = new ReentrantLock();
  private final JndiObjectLocator locator;

  private volatile Transport delegate;
  
  /**
   * Constructs a new instance.
   * @param session owning session
   * @param urlname name for the transport
   */
  public FixedRecipientTransport(Session session, URLName urlname) {
    super(session, urlname);
    this.properties = new SessionProperties(session);
    this.locator = newLocator(properties);
  }

  private static JndiObjectLocator newLocator(SessionProperties properties) {
    String locatorClassName = properties.getProperty(LOCATOR_CLASS);
    if (locatorClassName == null) {
      return JdkJndiObjectLocator.getInstance();
    }
    try {
      Class<?> locatorClass = getClassLoader().loadClass(locatorClassName);
      return (JndiObjectLocator) locatorClass.newInstance();
    }
    catch (InstantiationException | IllegalAccessException ex) {
      throw new IllegalArgumentException("cannot instantiate " + locatorClassName
          + ": " + ex, ex);
    }
    catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException("locator class " + locatorClassName
          + " not found");
    }
    catch (ClassCastException ex) {
      throw new IllegalArgumentException("locator class " + locatorClassName
          + " is not an instance of " + JndiObjectLocator.class.getName());
    }
  }

  private static ClassLoader getClassLoader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = FixedRecipientTransport.class.getClassLoader();
    }
    return classLoader;
  }

  @Override
  public void connect() throws MessagingException {
    getDelegate().connect();
  }

  @Override
  public void connect(String host, String user, String password)
      throws MessagingException {
    getDelegate().connect(host, user, password);
  }

  @Override
  public void connect(String user, String password) throws MessagingException {
    getDelegate().connect(user, password);
  }

  @Override
  public synchronized void connect(String host, int port, String user,
      String password) throws MessagingException {
    getDelegate().connect(host, port, user, password);
  }

  @Override
  public synchronized boolean isConnected() {
    try {
      return getDelegate().isConnected();
    }
    catch (MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public synchronized void close() throws MessagingException {
    getDelegate().close();
  }

  @Override
  public void addConnectionListener(ConnectionListener l) {
    try {
      getDelegate().addConnectionListener(l);
    }
    catch (MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void removeConnectionListener(ConnectionListener l) {
    try {
      getDelegate().removeConnectionListener(l);
    }
    catch (MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public synchronized void addTransportListener(TransportListener l) {
    try {
      getDelegate().addTransportListener(l);
    }
    catch (MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public synchronized void removeTransportListener(TransportListener l) {
    try {
      getDelegate().removeTransportListener(l);
    }
    catch (MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void sendMessage(Message message, Address[] addresses)
      throws MessagingException {
    getDelegate().sendMessage(message,
        addresses(properties.getRequiredProperty(ADDRESS)));
  }

  private Address[] addresses(String address) throws MessagingException {
    Address[] addresses = new Address[0];
    if (address != null && !address.isEmpty()) {
      addresses = InternetAddress.parse(address, false);
    }
    if (addresses.length == 0) {
      throw new MessagingException("no recipient addresses specified in " 
          + ADDRESS);
    }
    return addresses;
  }
 
  private Transport getDelegate() throws MessagingException {
    if (delegate == null) {
      lock.lock();
      try {
        if (delegate == null) {
          final String delegateName = properties.getRequiredProperty(DELEGATE);
          try {
            final Session session = locateSession(delegateName);
            delegate = session.getTransport();
          }
          catch (NamingException ex) {
            throw new MessagingException("lookup for " + delegateName
                + " failed", ex);
          }
        }
      }
      finally {
        lock.unlock();
      }
    }
    return delegate;
  }

  private Session locateSession(String delegateName) throws NamingException,
      MessagingException {
    Session session = (Session) locator.lookup(delegateName);
    if (session == null) {
      logger.severe("cannot locate session delegate: " + delegateName);
      throw new MessagingException("session not found: " + delegateName);
    }
    return session;
  }

}
