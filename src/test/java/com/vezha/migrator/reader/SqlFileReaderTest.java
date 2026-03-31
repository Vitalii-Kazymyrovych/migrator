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
        Path sqlFile = Files.createTempFile("source", ".sql");
        Files.writeString(sqlFile, """
                -- table setup
                CREATE TABLE clients (id INT PRIMARY KEY, name VARCHAR(50));
                INSERT INTO clients(id, name) VALUES (1, 'Acme');
                """);

        ConfigModel configModel = new ConfigModel();
        configModel.getSource().getSqlFile().setEnabled(true);
        configModel.getSource().getSqlFile().setPath(sqlFile.toString());

        JdbcTemplate source = new SqlFileReader().read(configModel);
        Integer count = source.queryForObject("SELECT COUNT(*) FROM clients", Integer.class);
        String name = source.queryForObject("SELECT name FROM clients WHERE id = 1", String.class);

        assertEquals(1, count);
        assertEquals("Acme", name);
    }
}
