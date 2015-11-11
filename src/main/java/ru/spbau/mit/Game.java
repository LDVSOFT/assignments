package ru.spbau.mit;


public interface Game {

    /** This method is called only once when new client is connected **/
    void onPlayerConnected(String id);

    /**
     * This method is called when new message is received from
     * a client with given id
     **/
    void onPlayerSentMsg(String id, String msg);
}
