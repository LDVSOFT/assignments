package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class GameServerImpl implements GameServer {
    protected static final int TIMEOUT = 100;
    protected final Game game;
    protected final Map<String, ConnectionHandler> clients = new Hashtable<>();
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

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
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    protected String getSetterName(String propName) {
        return "set" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
    }

    protected static Integer tryParseInt(String str) {
        try {
            return Integer.decode(str);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    public GameServerImpl(String gameClassName, Properties properties)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, InstantiationException {
        Class<?> gameClass = Class.forName(gameClassName);
        game = (Game) gameClass.getConstructor(GameServer.class).newInstance(this);
        for (String propName : properties.stringPropertyNames()) {
            String value = properties.getProperty(propName);
            String setterName = getSetterName(propName);
            Integer intValue = tryParseInt(value);
            if (intValue != null) {
                gameClass.getMethod(setterName, Integer.TYPE).invoke(game, intValue);
            } else {
                gameClass.getMethod(setterName, String.class).invoke(game, value);
            }
        }
    }

    @Override
    public void accept(final Connection connection) {
        lock.writeLock().lock();
        try {
            String id = Integer.toString(clients.size());
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
