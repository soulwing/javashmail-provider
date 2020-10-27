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

import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 * A utility class that provides more convenient access to session properties.
 *
 * @author Carl Harris
 */
public class SessionProperties {

  private final Properties properties;


  /**
   * Constructs a new instance.
   * @param session
   */
  public SessionProperties(Session session) {
    this(session.getProperties());
  }

  /**
   * Constructs a new instance.
   * @param properties
   */
  public SessionProperties(Properties properties) {
    this.properties = properties;
  }

  /**
   * Gets a property value.
   * @param name name of the property
   * @return property value or {@code null} if no value is set for the given
   *    name
   */
  public String getProperty(String name) {
    return properties.getProperty(name);
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

  /**
   * Gets a boolean property value.
   * @param name name of the property
   * @param defaultValue value to return if none is set
   * @return property value
   */
  public long getLongProperty(String name, long defaultValue) {
    String value = getProperty(name);
    if (value == null) return defaultValue;
    return Long.valueOf(value);
  }

  /**
   * Gets the collection of delegate properties from this properties collection.
   * @param protocol name of the protocol for these session properties
   * @return delegate properties (which may be empty)
   */

  public Properties getDelegateProperties(String protocol) {
    final String prefix = String.format("mail.%s.delegate.", protocol);
    final Properties properties = new Properties();
    for (final Object key : this.properties.keySet()) {
      final String name = (String) key;
      if (name.startsWith(prefix)) {
        final String targetName = name.replace(prefix, "mail.");
        properties.setProperty(targetName, this.properties.getProperty(name));
      }
    }
    return properties;
  }

  
}
