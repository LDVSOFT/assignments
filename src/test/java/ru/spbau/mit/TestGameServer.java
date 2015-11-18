package ru.spbau.mit;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;


public class TestGameServer {

    Random random = new Random(42);

    @Test
    public void testGameServerRespondsWithRandomIDsOnConnection()
            throws Exception {
        Server server = createGameServer();

        List<TestConnection> connections = new ArrayList<TestConnection>();
        int size = 1 + random.nextInt(100);
        for (int i = 0; i < size; i++) {
            TestConnection connection = new TestConnection();
            connections.add(connection);
            server.accept(connection);
        }

        Thread.sleep(1000);

        Set<String> ids = new HashSet<String>();
        for (TestConnection c: connections) {
            c.close();
            TestUtil.waitUntilClosed(c);
            if (c.id != null) {
                ids.add(c.id);
            }
        }

        assertEquals(size, ids.size());
    }

    @Test
    public void testGameServerBroadcastsToAll()
            throws Exception {
        GameServer server = createGameServer();

        List<TestConnection> connections = new ArrayList<TestConnection>();
        int size = 1 + random.nextInt(100);
        for (int i = 0; i < size; i++) {
            TestConnection connection =
                new TestConnection(TestConnection.State.RECEIVED_MESSAGE);
            connections.add(connection);
            server.accept(connection);
        }

        server.broadcast("TEST");

        for (TestConnection c: connections) {
            TestUtil.waitUntilClosed(c);
            assertEquals("TEST", c.lastMessage);
        }
    }

    private static GameServer createGameServer() throws Exception {
        return (GameServer) TestUtil.createInitializedGameServer(
                "TestGameServer$EmptyGame", new Properties());
    }

    public static class EmptyGame implements Game {

        public EmptyGame(GameServer ignored) {
        }

        @Override
        public void onPlayerConnected(String id) {
        }

        @Override
        public void onPlayerSentMsg(String id, String message) {
        }
    }

    private static class TestConnection extends AbstractConnection {

        public String id;
        public String lastMessage;

        public enum State {
            INITIALIZED, RECEIVED_ID, RECEIVED_MESSAGE
        }

        public TestConnection() {
            this(null); // Do not close connection automatically
        }

        public TestConnection(State closingState) {
            this.closingState = closingState;
        }

        @Override
        public synchronized void send(String message) {
            switch (state) {
                case INITIALIZED:
                    id = message;
                    state = State.RECEIVED_ID;
                    break;
                case RECEIVED_ID:
                case RECEIVED_MESSAGE:
                default:
                    lastMessage = message;
                    state = State.RECEIVED_MESSAGE;
                    break;
            }
        }

        @Override
        public synchronized String receive(long timeout)
                throws InterruptedException {
            return null;
        }

        @Override
        public synchronized boolean isClosed() {
            return super.isClosed() || state == closingState;
        }

        private State state = State.INITIALIZED;
        private State closingState;
    };
}
