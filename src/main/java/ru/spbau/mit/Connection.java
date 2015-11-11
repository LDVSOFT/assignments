package ru.spbau.mit;


public interface Connection {

    /** Send message to the client. **/
    void send(String message);

    /**
     * Receive message from the client
     * Block until timeout is expired if timeout greater than 0
     * Block until message is available if timeout equals 0
     * Throw IllegalArgumentException if timeout less than 0
     * Return `null` if timeout is expired before new message arrived.
     **/
    String receive(long timeout) throws InterruptedException;

    /** Close connection **/
    void close();

    /** Check whether connection is closed **/
    boolean isClosed();
}
