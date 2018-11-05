/*
 * File created on Nov 5, 2018
 *
 * Copyright (c) 2018 Carl Harris, Jr
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

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * A utility for locating a header used to single to a transport that
 * instead of attempting delivery, it should throw an exception.
 *
 * @author Carl Harris
 */
class ErrorHeader {

  static final String ERROR_HEADER = "X-Throw-Error";

  static MessagingException getErrorToThrow(Message message)
      throws MessagingException {
    final String[] values = message.getHeader(ERROR_HEADER);
    if (values == null || values.length == 0) return null;
    throw new MessagingException(values[0]);
  }

}
