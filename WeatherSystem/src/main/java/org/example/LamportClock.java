package org.example;

class LamportClock {
    private int clock;

    public LamportClock() {
        this.clock = 0;
    }

    public synchronized void increment() {
        clock++;
    }

    public synchronized void update(int receivedClock) {
        clock = Math.max(clock, receivedClock) + 1;
    }

    public synchronized int getClock() {
        return clock;
    }
}

