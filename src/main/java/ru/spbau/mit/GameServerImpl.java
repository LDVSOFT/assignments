package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class GameServerImpl implements GameServer {
    public static final int TIMEOUT = 100;
    protected Game game;
    protected Map<String, ConnectionHandler> clients = new Hashtable<>();
    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    private class ConnectionHandler implements Runnable {
        private final Queue<String> toSend = new ArrayDeque<>();
        private final String id;
        private final Connection connection;

        private ConnectionHandler(String id, Connection connection) {
            this.id = id;
            this.connection = connection;
        }

        private void send(String message) {
            synchronized (toSend) {
                toSend.add(message);
            }
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                synchronized (connection) {
                    if (connection.isClosed())
                        return;

                    synchronized (toSend) {
                        while (!toSend.isEmpty()) {
                            connection.send(toSend.poll());
                            if (connection.isClosed())
                                return;
                        }
                    }
                    try {
                        String message = connection.receive(TIMEOUT);
                        if (message != null) {
                            game.onPlayerSentMsg(id, message);
                        }
                    } catch (InterruptedException e) {
                        //Well, it sent nothing
                    }
                }
            }
        }
    }

    public GameServerImpl(String gameClassName, Properties properties) {
        try {
            Class<?> gameClass = Class.forName(gameClassName);
            game = (Game) gameClass.getConstructor(GameServer.class).newInstance(this);
            for (String propName : properties.stringPropertyNames()) {
                String value = properties.getProperty(propName);
                propName = Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
                String setterName = "set" + propName;
                if (value.matches("\\d+")) {
                    // int setter
                    gameClass.getMethod(setterName, Integer.TYPE).invoke(game, Integer.decode(value));
                } else {
                    // String setter
                    gameClass.getMethod(setterName, String.class).invoke(game, value);
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Cannot create and setup %s instance.", gameClassName), e);
        }
    }

    @Override
    public void accept(final Connection connection) {
        final String id = Integer.toString(clients.size());
        lock.writeLock().lock();
        try {
            ConnectionHandler handler = new ConnectionHandler(id, connection);
            clients.put(id, handler);
            connection.send(id);
            game.onPlayerConnected(id);
            new Thread(handler).start();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void broadcast(String message) {
        lock.readLock().lock();
        try {
            for (ConnectionHandler handler : clients.values()) {
                handler.send(message);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void sendTo(String id, String message) {
        ConnectionHandler handler;
        lock.readLock().lock();
        try {
            handler = clients.get(id);
        } finally {
            lock.readLock().unlock();
        }
        handler.send(message);
    }
}
