package org.soulwing.mail.transport;

import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

class MockTransportListener
    extends AbstractEventListener<TransportEvent>
    implements TransportListener {

  @Override
  public void messageDelivered(TransportEvent transportEvent) {
    receiveEvent(transportEvent);
  }

  @Override
  public void messageNotDelivered(TransportEvent transportEvent) {
    receiveEvent(transportEvent);
  }

  @Override
  public void messagePartiallyDelivered(TransportEvent transportEvent) {
    receiveEvent(transportEvent);
  }

}
