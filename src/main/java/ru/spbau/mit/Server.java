package ru.spbau.mit;

/** Server that accepts stub connections **/
public interface Server {

    /** Handle new connection **/
    void accept(Connection connection);
}
