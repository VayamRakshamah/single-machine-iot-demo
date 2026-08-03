package io.github.iotdemo.edge.store;

import io.github.iotdemo.edge.config.GatewayProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class OutboxStore {
    private final String jdbcUrl;

    public OutboxStore(GatewayProperties properties) {
        this.jdbcUrl = "jdbc:sqlite:" + properties.bufferPath();
    }

    @PostConstruct
    void initialize() throws Exception {
        String file = jdbcUrl.substring("jdbc:sqlite:".length());
        Path parent = Path.of(file).toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("CREATE TABLE IF NOT EXISTS sequence_state (id INTEGER PRIMARY KEY CHECK(id=1), value INTEGER NOT NULL)");
            statement.execute("INSERT OR IGNORE INTO sequence_state(id,value) VALUES(1,0)");
            statement.execute("CREATE TABLE IF NOT EXISTS outbox (sequence_number INTEGER PRIMARY KEY, payload TEXT NOT NULL, created_at TEXT NOT NULL)");
        }
    }

    public synchronized long nextSequence() throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("UPDATE sequence_state SET value=value+1 WHERE id=1");
                ResultSet result = statement.executeQuery("SELECT value FROM sequence_state WHERE id=1");
                long value = result.getLong(1); connection.commit(); return value;
            } catch (SQLException error) { connection.rollback(); throw error; }
        }
    }

    public void enqueue(long sequence, String payload) throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO outbox(sequence_number,payload,created_at) VALUES(?,?,datetime('now'))")) {
            statement.setLong(1, sequence); statement.setString(2, payload); statement.executeUpdate();
        }
    }

    public List<Item> pending(int limit) throws SQLException {
        List<Item> items = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "SELECT sequence_number,payload FROM outbox ORDER BY sequence_number LIMIT ?")) {
            statement.setInt(1, limit); ResultSet result = statement.executeQuery();
            while (result.next()) items.add(new Item(result.getLong(1), result.getString(2)));
        }
        return items;
    }

    public void acknowledge(long sequence) throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM outbox WHERE sequence_number=?")) {
            statement.setLong(1, sequence); statement.executeUpdate();
        }
    }

    public long count() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT count(*) FROM outbox")) { return result.getLong(1); }
    }

    private Connection connect() throws SQLException { return DriverManager.getConnection(jdbcUrl); }
    public record Item(long sequenceNumber, String payload) {}
}

