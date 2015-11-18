package ru.spbau.mit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class TestQuizGame extends Assert {
    @Test(timeout=1000)
    public void testSimpleSuccess() throws Exception {
        doBasicSingleConnectionTest(
                ScriptItem.of("!start", "New round started: Who was the imaginary love of Don Quixote? (8 letters)"),
                ScriptItem.of("Dulcinea", "The winner is 0"),
                ScriptItem.of(null, "New round started: Who was the wife of Othello? (9 letters)")
        );
    }

    @Test(timeout=10000)
    public void testSimpleFail() throws Exception {
        doBasicSingleConnectionTest(
                ScriptItem.of("!start", "New round started: Who was the imaginary love of Don Quixote? (8 letters)"),
                ScriptItem.of("Maria", "Wrong try"),
                ScriptItem.of(null, "Current prefix is D"),
                ScriptItem.of("Otello", "Wrong try"),
                ScriptItem.of(null, "Current prefix is Du"),
                ScriptItem.of("Natasha", "Wrong try"),
                ScriptItem.of(null, "Nobody guessed, the word was Dulcinea")
        );
    }

    @Test(timeout=10000)
    public void testSimpleStop() throws Exception {
        doBasicSingleConnectionTest(
                ScriptItem.of("!start", "New round started: Who was the imaginary love of Don Quixote? (8 letters)"),
                ScriptItem.of("Maria", "Wrong try"),
                ScriptItem.of(null, "Current prefix is D"),
                ScriptItem.of("Otello", "Wrong try"),
                ScriptItem.of(null, "Current prefix is Du"),
                ScriptItem.of("!stop", "Game has been stopped by 0"),
                ScriptItem.of("!start", "New round started: Who was the wife of Othello? (9 letters)"),
                ScriptItem.of("!stop", "Game has been stopped by 0")
        );
    }

    @Test(timeout=10000)
    public void testDoubleSuccess() throws Exception {
        doBasicTest(
                getCommonProperties(),
                new ConnectionWithTestScript(
                        ScriptItem.of("!start", "New round started: Who was the imaginary love of Don Quixote? (8 letters)"),
                        ScriptItem.of("Maria", "Wrong try"),
                        ScriptItem.of(null, "Current prefix is D"),
                        ScriptItem.of("Otello", "Wrong try"),
                        ScriptItem.of(null, "Current prefix is Du"),
                        ScriptItem.of(null, "The winner is 1"),
                        ScriptItem.of(null, "New round started: Who was the wife of Othello? (9 letters)"),
                        ScriptItem.of("Desdemona", "The winner is 0")
                ),
                new ConnectionWithTestScript(
                        ScriptItem.of(null, "New round started: Who was the imaginary love of Don Quixote? (8 letters)"),
                        ScriptItem.of(null, "Current prefix is D"),
                        ScriptItem.of(null, "Current prefix is Du"),
                        ScriptItem.of("Dulcinea", "The winner is 1"),
                        ScriptItem.of(null, "New round started: Who was the wife of Othello? (9 letters)"),
                        ScriptItem.of(null, "The winner is 0")
                )
        );
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private void doBasicSingleConnectionTest(ScriptItem... scriptItems) throws Exception {
        Properties properties = getCommonProperties();
        doBasicTest(properties, new ConnectionWithTestScript(scriptItems));
    }

    private Properties getCommonProperties() throws IOException {
        Properties properties = new Properties();
        File file = folder.newFile();
        PrintWriter printWriter = new PrintWriter(new FileWriter(file));
        printWriter.println("Who was the imaginary love of Don Quixote?;Dulcinea");
        printWriter.println("Who was the wife of Othello?;Desdemona");
        printWriter.close();

        properties.setProperty("delayUntilNextLetter", "500");
        properties.setProperty("maxLettersToOpen", "2");
        properties.setProperty("dictionaryFilename", file.getAbsolutePath());

        return properties;
    }

    private void doBasicTest(Properties properties, ConnectionWithTestScript... connections) throws Exception {
        Server server = TestUtil.createInitializedGameServer("QuizGame", properties);

        for (ConnectionWithTestScript connection : connections) {
            server.accept(connection);
        }

        for (ConnectionWithTestScript connection : connections) {
            connection.join();
        }
    }

    private static class ConnectionWithTestScript extends AbstractConnection {
        private final List<ScriptItem> sequence;
        private int currentIndex = 0;
        private Throwable error = null;
        private boolean wasId = false;
        private final Queue<String> messagesToSend = new ArrayDeque<>();

        public ConnectionWithTestScript(ScriptItem... scriptItems) {
            this.sequence = Arrays.asList(scriptItems);
            if (!sequence.isEmpty()) {
                addMessageToSend(sequence.get(0).request);
            }
        }

        private void addMessageToSend(String request) {
            if (request == null) return;
            messagesToSend.add(request);
        }

        @Override
        public synchronized void send(String message) {
            try {
                doSend(message);
            } catch (Throwable t) {
                end(t);
                throw t;
            }
        }

        private void doSend(String message) {
            if (!wasId) {
                assertTrue(Integer.parseInt(message) >= 0);
                wasId = true;
                return;
            }

            if (isDone()) return;
            assertEquals(sequence.get(currentIndex).response, message);

            increaseIndex();
        }

        @Override
        public synchronized String receive(long timeout) throws InterruptedException {
            try {
                String result = doReceive();

                if (timeout == 0) {
                    while (result == null) {
                        wait();
                        result = doReceive();
                    }
                }

                // Pretend that we've waited
                return result;
            } catch (Throwable t) {
                end(t);
                throw t;
            }
        }

        private String doReceive() {
            checkNotDone();

            while (!messagesToSend.isEmpty()) {
                String next = messagesToSend.poll();
                if (next != null) {
                    return next;
                }
            }

            return null;
        }

        private void end(Throwable t) {
            currentIndex = sequence.size() - 1;
            error = t;
            increaseIndex();
        }

        private void checkNotDone() {
            assertFalse(isDone());
        }

        private void increaseIndex() {
            currentIndex++;
            if (isDone()) {
                close();

                synchronized (this) {
                    notifyAll();
                }
            } else {
                addMessageToSend(sequence.get(currentIndex).request);
            }
        }

        private boolean isDone() {
            return currentIndex == sequence.size();
        }

        public void join() throws Exception {
            synchronized (this) {
                while (!isDone() && error == null) {
                    wait();
                }
                if (error != null) {
                    throw new RuntimeException(error);
                }
            }
        }
    }

    private static class ScriptItem {
        private String request;
        private String response;

        public ScriptItem(String request, String response) {
            this.request = request;
            this.response = response;
        }

        private static ScriptItem of(String request, String response) {
            return new ScriptItem(request, response);
        }
    }
}
