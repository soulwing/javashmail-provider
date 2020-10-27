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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.ConnectionEvent;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link NullTransport}.
 *
 * @author Carl Harris
 */
public class NullTransportTest {

  private Session session;
  
  @Before
  public void setUp() throws Exception {
    final Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "null");
    session = Session.getInstance(properties);
  }

  @Test
  public void testConnect() throws Exception {
    Transport transport = session.getTransport();
    transport.connect();
  }

  @Test
  public void testSendMessage() throws Exception {
    final Message message = MessageFactory.newMessage("test message", session);
   session.getTransport().sendMessage(message, message.getAllRecipients());
  }

}
