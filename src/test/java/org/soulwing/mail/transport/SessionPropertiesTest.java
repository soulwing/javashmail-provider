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

import java.util.Properties;

import org.junit.Test;

/**
 * Unit tests for Session properties.
 *
 * @author Carl Harris
 */
public class SessionPropertiesTest {

  private Properties props = new Properties();
  private SessionProperties properties = new SessionProperties(props);

  @Test
  public void testGetDelegateProperties() throws Exception {
    props.setProperty("mail.test.delegate.transport.protocol", "file");
    props.setProperty("mail.test.delegate.file.path", "somePath");
    Properties delegateProps = properties.getDelegateProperties("test");
    assertThat(delegateProps.getProperty("mail.transport.protocol"),
        is(equalTo("file")));
    assertThat(delegateProps.getProperty("mail.file.path"),
        is(equalTo("somePath")));
  }

}
