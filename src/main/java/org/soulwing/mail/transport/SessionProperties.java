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

import javax.mail.MessagingException;
import javax.mail.Session;

/**
 * A utility class that provides more convenient access to session properties.
 *
 * @author Carl Harris
 */
public class SessionProperties {

  private final Session session;

  /**
   * Constructs a new instance.
   * @param session
   */
  public SessionProperties(Session session) {
    this.session = session;
  }
  
  /**
   * Gets a property value.
   * @param name name of the property
   * @return property value or {@code null} if no value is set for the given
   *    name
   */
  public String getProperty(String name) {
    return session.getProperty(name);
  }
  
  /**
   * Gets a required property value.
   * @param name name of the property
   * @return property value
   * @throws MessagingException if the property is not set
   */
  public String getRequiredProperty(String name) 
      throws MessagingException {
    String value = getProperty(name);
    if (value == null) {
      throw new MessagingException(
          "property " + name + " is required in the session"); 
    }
    return value;
  }

  /**
   * Gets a boolean property value.
   * @param name name of the property
   * @param defaultValue value to return if none is set
   * @return property value
   * @throws MessagingException if the property is not set to a legitimate
   *    boolean value
   */
  public boolean getBooleanProperty(String name, boolean defaultValue) 
      throws MessagingException {
    String value = getProperty(name);
    if (value == null) return defaultValue;
    value = value.trim().toLowerCase();
    if ("yes".equals(value) || "true".equals(value)) return true;
    if ("no".equals(value) || "false".equals(value)) return false;
    throw new MessagingException("property " + name 
        + " allows either 'true' or 'false'");
  }

  
}
