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

import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 * A factory that produces JavaMail {@link Session} objects.
 *
 * @author Carl Harris
 */
class SessionFactory {

  private Properties defaultProperties;

  SessionFactory(Properties defaultProperties) {
    this.defaultProperties = defaultProperties;
  }

  Session newSession(String... pairs)
      throws MessagingException {
    Properties properties = (Properties) defaultProperties.clone();
    for (int i = 0; i < pairs.length / 2; i++) {
      properties.setProperty(pairs[2*i], pairs[2*i + 1]);
    }
    return Session.getInstance(properties);
  }

}
