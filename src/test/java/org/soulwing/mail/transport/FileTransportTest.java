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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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

  private File file;
  
  @Before  
  public void setUp() throws Exception {
    file = File.createTempFile("mail", ".txt");    
  }
  
  @After
  public void tearDown() throws Exception {
    file.delete();
  }

  @Test
  public void testSendMessage() throws Exception {
    Session session = newSession();
    Transport transport = session.getTransport();
    MimeMessage message = newMessage("Test message", session);
    transport.sendMessage(message, message.getAllRecipients());
    MimeMessage result = readMessageFromFile(file, session);
    assertThat(result.getFrom(), is(equalTo(message.getFrom())));
    assertThat(result.getRecipients(RecipientType.TO), 
        is(equalTo(message.getRecipients(RecipientType.TO))));
    assertThat(result.getSubject(), is(equalTo(message.getSubject())));
  }

  @Test
  public void testAppendMessage() throws Exception {
    Session session = newSession(FileTransport.APPEND, "true");
    Transport transport = session.getTransport();
    MimeMessage message1 = newMessage("Message 1", session);
    transport.sendMessage(message1, message1.getAllRecipients());
    MimeMessage message2 = newMessage("Message 2", session);
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
    Session session = newSession(FileTransport.APPEND, "false");
    Transport transport = session.getTransport();
    MimeMessage message1 = newMessage("Message 1", session);
    transport.sendMessage(message1, message1.getAllRecipients());
    MimeMessage message2 = newMessage("Message 2", session);
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

  private MimeMessage newMessage(String subject, Session session) 
      throws MessagingException, AddressException {
    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress("nobody@nowhere.net"));
    message.addRecipient(RecipientType.TO, 
        new InternetAddress("nobody@nowhere.net"));
    message.setSubject(subject);
    message.setText("This is a test. This is only a test.");
    return message;
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

  private Session newSession(String... pairs) throws MessagingException {
    Properties properties = defaultProperties();
    for (int i = 0; i < pairs.length / 2; i++) {
      properties.setProperty(pairs[2*i], pairs[2*i + 1]);
    }
    return Session.getInstance(properties);
  }
  
  private Properties defaultProperties() {
    Properties properties = new Properties();
    properties.setProperty(FileTransport.FILE_PATH, file.toString());
    properties.setProperty("mail.transport.protocol", "file");
    return properties;
  }
  
}


