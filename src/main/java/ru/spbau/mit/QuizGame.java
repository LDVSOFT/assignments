package ru.spbau.mit;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;


public class QuizGame implements Game {

    public static final String FORMAT_NEW_ROUND = "New round started: %s (%d letters)";
    public static final String FORMAT_CURRENT_PREFIX = "Current prefix is %s";
    public static final String FORMAT_WINNER = "The winner is %s";
    public static final String FORMAT_WRONG = "Wrong try";
    public static final String FORMAT_STOP = "Game has been stopped by %s";
    public static final String FORMAT_NOBODY = "Nobody guessed, the word was %s";

    public void setDictionaryFilename(String dictionaryFilename) {
        this.dictionaryFilename = dictionaryFilename;
    }

    public void setMaxLettersToOpen(int maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }

    public void setDelayUntilNextLetter(int delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    // Params:
    protected String dictionaryFilename;
    protected int maxLettersToOpen;
    protected int delayUntilNextLetter;

    protected static class Question {
        private String question;
        private String answer;

        private Question(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    protected GameServer gameServer;
    protected Lock lockMessages = new ReentrantLock();
    protected ArrayList<Question> questions = new ArrayList<>();
    protected int currentQuestion = -1;
    protected int currentProgress;
    protected boolean isRunning = false;

    protected int tickerTaskId = 0;

    public QuizGame(GameServer server) {
        gameServer = server;
    }

    @Override
    public void onPlayerConnected(String id) {
        if (!isRunning) {
            return;
        }

        lockMessages.lock();
        try {
            gameServer.sendTo(id, format(FORMAT_NEW_ROUND, questions.get(currentQuestion).question, questions.get(currentQuestion).answer.length()));
            for (int i = 1; i <= currentProgress; ++i) {
                gameServer.sendTo(id, format(FORMAT_CURRENT_PREFIX, questions.get(currentQuestion).answer.substring(0, i)));
            }
        } finally {
            lockMessages.unlock();
        }
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        lockMessages.lock();
        try {
            if (msg.length() != 0 && msg.charAt(0) == '!') {
                //Commands
                switch (msg) {
                    case "!start":
                        if (!isRunning) {
                            startGame(id);
                        }
                        break;

                    case "!stop":
                        if (isRunning) {
                            stopGame(id);
                        }
                        break;
                }
                return;
            }

            boolean answered = false;
            if (msg.equals(questions.get(currentQuestion).answer)) {
                answered = true;
                stopTicker();
                gameServer.broadcast(format(FORMAT_WINNER, id));
            } else {
                gameServer.sendTo(id, FORMAT_WRONG);
            }
            if (answered)
                startRound();
        } finally {
            lockMessages.unlock();
        }
    }

    protected void startGame(String id) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dictionaryFilename));
            String line;
            questions.clear();
            while ((line = reader.readLine()) != null) {
                int sep = line.indexOf(';');
                questions.add(new Question(line.substring(0, sep), line.substring(sep + 1)));
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't read questions", e);
        }
//        currentQuestion = -1;
        isRunning = true;
        startRound();
    }

    protected void stopGame(String id) {
        stopTicker();
        gameServer.broadcast(format(FORMAT_STOP, id));
        isRunning = false;
    }

    protected void startRound() {
        ++currentQuestion;
        if (currentQuestion == questions.size()) {
            currentQuestion = 0;
        }
        currentProgress = 0;
        gameServer.broadcast(format(FORMAT_NEW_ROUND, questions.get(currentQuestion).question, questions.get(currentQuestion).answer.length()));
        runTicker();
    }
    protected void runTicker() {
        new Thread(new Runnable() {
            final int task = tickerTaskId;

            @Override
            public void run() {
                try {
                    Thread.sleep(delayUntilNextLetter);
                } catch (InterruptedException e) {
                    return;
                }
                lockMessages.lock();
                try {
                    if (tickerTaskId == task) {
                        tickerTaskId += 1;
                        tickGame();
                    }
                } finally {
                    lockMessages.unlock();
                }
            }
        }).start();
    }

    protected void stopTicker() {
        tickerTaskId += 1;
    }

    protected void tickGame() {
        currentProgress += 1;
        if (currentProgress > maxLettersToOpen) {
            gameServer.broadcast(format(FORMAT_NOBODY, questions.get(currentQuestion).answer));
            startRound();
        } else {
            gameServer.broadcast(format(FORMAT_CURRENT_PREFIX, questions.get(currentQuestion).answer.substring(0, currentProgress)));
            runTicker();
        }
    }
}
