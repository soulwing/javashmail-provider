package org.soulwing.mail.transport;

import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

class MockConnectionListener
    extends AbstractEventListener<ConnectionEvent>
    implements ConnectionListener {

  @Override
  public void opened(ConnectionEvent connectionEvent) {
    receiveEvent(connectionEvent);
  }

  @Override
  public void disconnected(ConnectionEvent connectionEvent) {
    receiveEvent(connectionEvent);
  }

  @Override
  public void closed(ConnectionEvent connectionEvent) {
    receiveEvent(connectionEvent);
  }

}
