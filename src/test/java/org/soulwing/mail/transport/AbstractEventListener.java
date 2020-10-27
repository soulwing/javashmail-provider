package org.soulwing.mail.transport;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

abstract class AbstractEventListener<T> {

  private final Lock lock = new ReentrantLock();
  private final Condition readyCondition = lock.newCondition();

  private T event;

  void reset() {
    event = null;
  }

  T awaitEvent(final long wait, final long maxWait) throws InterruptedException {
    lock.lock();
    try {
      final long start = System.currentTimeMillis();
      long now = start;
      while (event == null && (now - start) < maxWait) {
        readyCondition.await(wait, TimeUnit.MILLISECONDS);
        now = System.currentTimeMillis();
      }
      return event;
    }
    finally {
      lock.unlock();
    }
  }

  void receiveEvent(T event) {
    lock.lock();
    try {
      this.event = event;
      readyCondition.signalAll();
    }
    finally {
      lock.unlock();
    }
  }

}
