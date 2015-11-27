package ru.spbau.mit;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class SumTwoNumbersGame implements Game {
    private static final String RIGHT = "Right";
    private static final String WRONG = "Wrong";
    public static final int BOUND = 1000000000;

    private GameServer gameServer;
    private int i, j;
    private Lock lockData = new ReentrantLock();
    private Random random = new Random(0xDEADBEEF);

    public SumTwoNumbersGame(GameServer server) {
        gameServer = server;
        startRound();
    }

    private void startRound() {
        i = random.nextInt(BOUND);
        j = random.nextInt(BOUND);
        gameServer.broadcast(String.format("%d %d", i, j));
    }

    @Override
    public void onPlayerConnected(String id) {
        lockData.lock();
        try {
            gameServer.sendTo(id, String.format("%d %d", i, j));
        } finally {
            lockData.unlock();
        }
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        lockData.lock();
        boolean answered = false;
        try {
            if (!msg.matches("-?\\d+")) {
                return;
            }
            int answer = Integer.decode(msg);
            if (answer == i + j) {
                answered = true;
                gameServer.sendTo(id, RIGHT);
            } else {
                gameServer.sendTo(id, WRONG);
            }

            if (answered)
                startRound();
        } finally {
            lockData.unlock();
        }
    }
}
