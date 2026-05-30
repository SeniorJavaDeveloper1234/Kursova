package ua.lpnu.deposits.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Thread-safe singleton that manages the single SQLite {@link Connection}.
 * On the first call to {@link #getInstance()} the {@code data/} directory and
 * database file are created if absent, and the schema is applied from
 * {@code schema.sql} on the classpath.
 */
public class DatabaseConnection {

    private static final String DB_DIR = "data";
    private static final String DB_FILE = "data/deposits.db";
    private static final String SCHEMA_RESOURCE = "/schema.sql";

    private static volatile DatabaseConnection instance;
    private final Connection connection;

    private DatabaseConnection() throws SQLException {
        ensureDataDirectory();
        String url = "jdbc:sqlite:" + DB_FILE;
        connection = DriverManager.getConnection(url);
        enableForeignKeys();
        applySchema();
    }

    /**
     * Returns the singleton instance, creating it on the first call.
     *
     * @return the singleton {@code DatabaseConnection}
     * @throws SQLException if the connection or schema initialisation fails
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the underlying JDBC connection.
     *
     * @return the active {@link Connection}
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the underlying connection and clears the singleton so the next
     * call to {@link #getInstance()} will create a fresh one.
     *
     * @throws SQLException if closing the connection fails
     */
    public static void closeInstance() throws SQLException {
        synchronized (DatabaseConnection.class) {
            if (instance != null) {
                instance.connection.close();
                instance = null;
            }
        }
    }

    private void ensureDataDirectory() throws SQLException {
        Path dir = Paths.get(DB_DIR);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new SQLException("Cannot create data directory: " + dir.toAbsolutePath(), e);
            }
        }
    }

    private void enableForeignKeys() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
    }

    private void applySchema() throws SQLException {
        InputStream is = DatabaseConnection.class.getResourceAsStream(SCHEMA_RESOURCE);
        if (is == null) {
            throw new SQLException("Schema resource not found on classpath: " + SCHEMA_RESOURCE);
        }
        String sql;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new SQLException("Failed to read schema resource", e);
        }

        try (Statement st = connection.createStatement()) {
            for (String statement : sql.split(";")) {
                String trimmed = statement.strip();
                if (!trimmed.isEmpty()) {
                    st.execute(trimmed);
                }
            }
        }
    }
}
