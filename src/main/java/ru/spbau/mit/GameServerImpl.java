package ru.spbau.mit;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class GameServerImpl implements GameServer {
    protected Game game;
    protected Map<String, Connection> clients = new Hashtable<>();
    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    public GameServerImpl(String gameClassName, Properties properties) {
        try {
            Class<?> gameClass = Class.forName(gameClassName);
            game = (Game) gameClass.getConstructor(GameServer.class).newInstance(this);
            for (String propName: properties.stringPropertyNames()) {
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
            clients.put(id, connection);
        } finally {
            lock.writeLock().unlock();
        }
        connection.send(id);
        game.onPlayerConnected(id);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (! Thread.interrupted()) {
                    try {
                        String message = connection.receive(0);
                        game.onPlayerSentMsg(id, message);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }

    @Override
    public void broadcast(String message) {
        lock.readLock().lock();
        try {
            for (Connection connection : clients.values()) {
                connection.send(message);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void sendTo(String id, String message) {
        Connection connection;
        lock.readLock().lock();
        try {
            connection = clients.get(id);
        } finally {
            lock.readLock().unlock();
        }
        connection.send(message);
    }
}
