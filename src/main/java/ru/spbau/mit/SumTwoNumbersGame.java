package ru.spbau.mit;

import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class SumTwoNumbersGame implements Game {
    protected static final String RIGHT = "Right";
    protected static final String WRONG = "Wrong";
    public static final int BOUND = 1000000000;

    protected GameServer gameServer;
    protected int i, j;
    protected ReadWriteLock lock = new ReentrantReadWriteLock();
    protected Random random = new Random(0xDEADBEEF);

    public SumTwoNumbersGame(GameServer server) {
        gameServer = server;
        startRound();
    }

    protected void startRound() {
        lock.writeLock().lock();
        try {
            i = random.nextInt(BOUND);
            j = random.nextInt(BOUND);
            gameServer.broadcast(String.format("%d %d", i, j));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void onPlayerConnected(String id) {
        lock.readLock().lock();
        try {
            gameServer.sendTo(id, String.format("%d %d", i, j));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        lock.readLock().lock();
        boolean answered = false;
        try {
            if (!msg.matches("\\d+")) {
                return;
            }
            int answer = Integer.decode(msg);
            if (answer == i + j) {
                answered = true;
                gameServer.sendTo(id, RIGHT);
            } else {
                gameServer.sendTo(id, WRONG);
            }
        } finally {
            lock.readLock().unlock();
        }

        if (answered)
            startRound();
    }
}
