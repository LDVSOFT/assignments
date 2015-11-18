package ru.spbau.mit;


public class HelloWorldServer implements Server {

    protected static final String MESSAGE = "Hello world";

    @Override
    public void accept(final Connection connection) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.send(MESSAGE);
                connection.close();
            }
        }).start();
    }
}
