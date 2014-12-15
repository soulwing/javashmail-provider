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

import java.util.Arrays;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
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

  private final SessionProperties properties;
  
  /**
   * Constructs a new instance.
   * @param session
   * @param urlname
   */
  public FixedRecipientTransport(Session session, URLName urlname) {
    super(session, urlname);
    this.properties = new SessionProperties(session);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendMessage(Message message, Address[] addresses)
      throws MessagingException {
    
    String delegateName = properties.getRequiredProperty(DELEGATE);
    String address = properties.getRequiredProperty(ADDRESS);
    
    try {
      Address[] recipients = addresses(address);

      Session session = locateSession(delegateName);
      
      logger.info("delivering message with envelope recipients: "
          + Arrays.asList(recipients));

      Transport delegate = session.getTransport();
      delegate.sendMessage(message, recipients);
      
    }
    catch (NamingException ex) {
      throw new MessagingException("lookup for " + delegateName + " failed", ex);
    }
  }

  private Session locateSession(String delegateName) throws NamingException,
      MessagingException {
    Session session = (Session) 
        JndiObjectLocator.getInstance().lookup(delegateName);      
    if (session == null) {
      throw new MessagingException("session not found: " + delegateName);
    }
    return session;
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
 
  
}
