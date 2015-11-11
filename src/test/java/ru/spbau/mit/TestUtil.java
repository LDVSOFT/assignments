package ru.spbau.mit;

import java.util.Properties;


public class TestUtil {
    public static <D> D createInstanceOfClassWithDefaultConstructor(String shortName) {
        try {
            return (D) Class.forName("ru.spbau.mit." + shortName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Server createInitializedGameServer(String pluginShortName, Properties properties) throws Exception {
        Class<?> gameClass = Class.forName("ru.spbau.mit.GameServerImpl");
        return (Server) gameClass.getConstructor(String.class, Properties.class)
                        .newInstance("ru.spbau.mit." + pluginShortName, properties);
    }

    public static void waitUntilClosed(Connection connection) {
        while (!connection.isClosed()) {
            Thread.yield();
        }
    }
}
