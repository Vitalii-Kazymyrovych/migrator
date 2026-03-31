package com.vezha.migrator.reader;

import com.vezha.migrator.config.ConfigModel;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlFileReaderTest {

    @Test
    void loadsSqlFileIntoInMemorySourceDatabase() throws IOException {
        Path sourceSql = Path.of("source.sql");
        Files.writeString(sourceSql, """
                -- table setup
                CREATE TABLE clients (id INT PRIMARY KEY, name VARCHAR(50));
                INSERT INTO clients(id, name) VALUES (1, 'Acme');
                """);

        ConfigModel configModel = new ConfigModel();
        configModel.getSource().getSqlFile().setEnabled(true);

        try {
            JdbcTemplate source = new SqlFileReader().read(configModel);
            Integer count = source.queryForObject("SELECT COUNT(*) FROM clients", Integer.class);
            String name = source.queryForObject("SELECT name FROM clients WHERE id = 1", String.class);

            assertEquals(1, count);
            assertEquals("Acme", name);
        } finally {
            Files.deleteIfExists(sourceSql);
        }
    }

    @Test
    void loadsDbeaverStyleMultiRowInsertWithEscapedAndDoubledQuotes() throws IOException {
        Path sourceSql = Path.of("source.sql");
        Files.writeString(sourceSql, """
                CREATE TABLE events (id INT PRIMARY KEY, payload VARCHAR(255));
                INSERT INTO events(id, payload) VALUES
                    (1, 'simple value'),
                    (2, 'O''Reilly export'),
                    (3, 'DBeaver \\\'quoted\\\' value; semicolon inside');
                """);

        ConfigModel configModel = new ConfigModel();
        configModel.getSource().getSqlFile().setEnabled(true);

        try {
            JdbcTemplate source = new SqlFileReader().read(configModel);
            Integer count = source.queryForObject("SELECT COUNT(*) FROM events", Integer.class);
            String second = source.queryForObject("SELECT payload FROM events WHERE id = 2", String.class);
            String third = source.queryForObject("SELECT payload FROM events WHERE id = 3", String.class);

            assertEquals(3, count);
            assertEquals("O'Reilly export", second);
            assertEquals("DBeaver 'quoted' value; semicolon inside", third);
        } finally {
            Files.deleteIfExists(sourceSql);
        }
    }
}
