package ru.spbau.mit;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;


public class TestHelloWorldServer {

    @Test
    public void testServerSendsHelloWorld() {
        Server server = createHelloWorldServer();
        TestConnection connection = new TestConnection();
        server.accept(connection);
        TestUtil.waitUntilClosed(connection);
        assertEquals("Hello world", connection.lastMessage);
    }

    @Test
    public void testServerHandlesMultipleClients() {
        Server server = createHelloWorldServer();
        TestConnection fast = new TestConnection();
        TestConnection slow = new SlowTestConnection();
        server.accept(slow);
        server.accept(fast);

        TestUtil.waitUntilClosed(fast);
        assertEquals(null, slow.lastMessage);
        assertEquals("Hello world", fast.lastMessage);
        TestUtil.waitUntilClosed(slow);
        assertEquals("Hello world", slow.lastMessage);
    }

    private static Server createHelloWorldServer() {
        return TestUtil.createInstanceOfClassWithDefaultConstructor("HelloWorldServer");
    }

    private static class TestConnection extends AbstractConnection {

        public String lastMessage = null;

        @Override
        public synchronized void send(String message) {
            assertConnectionOpened();
            lastMessage = message;
            notify();
        }
    }

    private static class SlowTestConnection extends TestConnection {
        @Override
        public void send(String message) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                // ignore
            } finally {
                super.send(message);
            }
        }
    }
}
