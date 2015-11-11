package ru.spbau.mit;


public abstract class AbstractConnection implements Connection {
    @Override
    public synchronized void send(String message) {
        assertConnectionOpened();
    }

    @Override
    public synchronized String receive(long timeout) throws InterruptedException {
        assertConnectionOpened();
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void close() {
        isClosed = true;
        notify();
    }

    @Override
    public synchronized boolean isClosed() {
        return isClosed;
    }

    protected synchronized void assertConnectionOpened() {
        if (isClosed()) {
            throw new IllegalStateException("Connection is closed");
        }
    }

    private boolean isClosed = false;
}
