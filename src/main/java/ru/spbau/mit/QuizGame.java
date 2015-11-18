package ru.spbau.mit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class QuizGame implements Game {

    public static final String FORMAT_NEW_ROUND = "New round started: %s (%d letters)";
    public static final String FORMAT_CURRENT_PREFIX = "Current prefix is %s";
    public static final String FORMAT_WINNER = "The winner is %s";
    public static final String FORMAT_WRONG = "Wrong try";
    public static final String FORMAT_STOP = "Game has been stopped by %s";
    public static final String FORMAT_NOBODY = "Nobody guessed, the word was %s";
    public static final String MESSAGE_START = "!start";
    public static final String MESSAGE_STOP = "!stop";

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

    protected long lastEvent = 0;

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
            gameServer.sendTo(id, String.format(FORMAT_NEW_ROUND, questions.get(currentQuestion).question, questions.get(currentQuestion).answer.length()));
            for (int i = 1; i <= currentProgress; ++i) {
                gameServer.sendTo(id, String.format(FORMAT_CURRENT_PREFIX, questions.get(currentQuestion).answer.substring(0, i)));
            }
        } finally {
            lockMessages.unlock();
        }
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        lockMessages.lock();
        try {
            switch (msg) {
                case MESSAGE_START:
                    if (!isRunning) {
                        startGame(id);
                    }
                    break;

                case MESSAGE_STOP:
                    if (isRunning) {
                        stopGame(id);
                    }
                    break;

                default:
                    if (msg.equals(questions.get(currentQuestion).answer)) {
                        stopTicker();
                        gameServer.broadcast(String.format(FORMAT_WINNER, id));
                        startRound();
                    } else {
                        gameServer.sendTo(id, FORMAT_WRONG);
                    }
            }
        } finally {
            lockMessages.unlock();
        }
    }

    protected void readQuestions() {
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
    }

    protected void startGame(String id) {
        readQuestions();
        isRunning = true;
        startRound();
    }

    protected void stopGame(String id) {
        stopTicker();
        gameServer.broadcast(String.format(FORMAT_STOP, id));
        isRunning = false;
    }

    protected void startRound() {
        currentQuestion++;
        if (currentQuestion == questions.size()) {
            currentQuestion = 0;
        }
        currentProgress = 0;
        gameServer.broadcast(String.format(FORMAT_NEW_ROUND, questions.get(currentQuestion).question, questions.get(currentQuestion).answer.length()));
        runTicker();
    }

    protected void runTicker() {
        lastEvent = System.currentTimeMillis();
        new Thread(new Runnable() {
            final long startLastEvent = lastEvent;

            @Override
            public void run() {
                try {
                    Thread.sleep(delayUntilNextLetter);
                } catch (InterruptedException e) {
                    return;
                }
                lockMessages.lock();
                try {
                    if (startLastEvent == lastEvent) {
                        tickGame();
                    }
                } finally {
                    lockMessages.unlock();
                }
            }
        }).start();
    }

    protected void stopTicker() {
        lastEvent = 0;
    }

    protected void tickGame() {
        currentProgress += 1;
        if (currentProgress > maxLettersToOpen) {
            gameServer.broadcast(String.format(FORMAT_NOBODY, questions.get(currentQuestion).answer));
            startRound();
        } else {
            gameServer.broadcast(String.format(FORMAT_CURRENT_PREFIX, questions.get(currentQuestion).answer.substring(0, currentProgress)));
            runTicker();
        }
    }
}
