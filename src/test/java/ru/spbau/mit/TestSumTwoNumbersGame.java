package ru.spbau.mit;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Properties;
import java.util.regex.*;


public class TestSumTwoNumbersGame {

    @Test
    public void testSumTwoNumbersWithTwoPlayersOneRound() throws Exception {
        Server server = TestUtil.createInitializedGameServer("SumTwoNumbersGame", new Properties());

        Bot failBot = new Bot() {
            @Override
            public int computeAnswer(int a, int b) {
                return a + b + 1;
            }
        };

        Bot winBot = new Bot() {
            @Override
            public int computeAnswer(int a, int b) {
                return a + b;
            }
        };

        server.accept(failBot);
        Thread.sleep(100);
        server.accept(winBot);
        TestUtil.waitUntilClosed(failBot);
        TestUtil.waitUntilClosed(winBot);

        assertEquals(failBot.broadcast, winBot.broadcast);
        assertEquals("Right", winBot.reply);
        assertEquals("Wrong", failBot.reply);
    }

    private static abstract class Bot extends AbstractConnection {

        public enum State {
            INITIALIZED, RECEIVED_ID, RECEIVED_TASK, RECEIVED_REPLY, RECEIVED_BROADCAST
        }

        public String id;
        public String reply;
        public String broadcast;

        public abstract int computeAnswer(int a, int b);

        @Override
        public synchronized void send(String message) {
            switch (state) {
                case INITIALIZED:
                    id = message;
                    state = State.RECEIVED_ID;
                    break;
                case RECEIVED_ID:
                    int[] task = parseTask(message);
                    assert task != null : String.format("'%s' is not a task for 'Sum two numbers' game", message);
                    answer = Integer.toString(computeAnswer(task[0], task[1]));
                    state = State.RECEIVED_TASK;
                    break;
                case RECEIVED_TASK:
                    reply = message;
                    state = State.RECEIVED_REPLY;
                    break;
                case RECEIVED_REPLY:
                    broadcast = message;
                    state = State.RECEIVED_BROADCAST;
                    break;
                case RECEIVED_BROADCAST:
                default:
                    break;
            }

            notify();
        }

        @Override
        public synchronized String receive(long timeout)
                throws InterruptedException {
            assertConnectionOpened();
            while (answer == null && !isClosed()) {
                wait(timeout);
                if (answer == null && timeout > 0) {
                    return null;
                }
            }
            assertConnectionOpened();

            String result = answer;
            answer = null;
            return result;
        }

        @Override
        public synchronized boolean isClosed() {
            return super.isClosed() || state == State.RECEIVED_BROADCAST;
        }

        private int[] parseTask(String message) {
            Matcher m = TASK_REGEX.matcher(message);
            if (m.matches()) {
                int a = Integer.parseInt(m.group(1));
                int b = Integer.parseInt(m.group(2));
                int[] result = { a, b };
                return result;
            } else {
                return null;
            }
        }

        private static final Pattern TASK_REGEX =
            Pattern.compile("(\\d+) (\\d+)");

        private State state = State.INITIALIZED;
        private String answer;
    }
}
