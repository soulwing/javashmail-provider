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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

/**
 * A {@link Transport} that appends messages to a file.
 *
 * @author Carl Harris
 */
public class FileTransport extends Transport {

  private static final String FILE_PROVIDER = "mail.file";
  public static final String FILE_PATH = FILE_PROVIDER + ".path";
  public static final String APPEND = FILE_PROVIDER + ".append";
  
  private final SessionProperties properties;
  
  /**
   * Constructs a new instance.
   * @param session session to associate with this transport
   * @param urlname url name to associate with this transport
   */
  public FileTransport(Session session, URLName urlname) {
    super(session, urlname);
    this.properties = new SessionProperties(session);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendMessage(Message message, Address[] recipients)
      throws MessagingException {
    String path = properties.getRequiredProperty(FILE_PATH);
    boolean append = properties.getBooleanProperty(APPEND, true);
    try {
      File file = new File(path);
      OutputStream outputStream = new FileOutputStream(file, append);
      try {
        message.writeTo(outputStream);
      }
      finally {
        try {
          outputStream.close();
        }
        catch (IOException ex) {
          ex.printStackTrace(System.err);
        }
      }
    }
    catch (IOException ex) {
      throw new MessagingException("error writing message to file", ex);
    }
  }

}
