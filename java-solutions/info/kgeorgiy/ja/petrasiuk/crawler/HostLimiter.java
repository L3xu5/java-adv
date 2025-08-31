package info.kgeorgiy.ja.petrasiuk.crawler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

class HostLimiter {
    final Semaphore semaphore;
    final AtomicInteger users = new AtomicInteger();

    HostLimiter(int permits) {
        this.semaphore = new Semaphore(permits);
    }
}

