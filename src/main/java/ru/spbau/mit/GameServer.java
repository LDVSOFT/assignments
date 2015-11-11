package ru.spbau.mit;


public interface GameServer extends Server {
    /** Send message to a particular client **/
    void sendTo(String id, String message);

    /** Send message to all connected clients **/
    void broadcast(String message);
}
