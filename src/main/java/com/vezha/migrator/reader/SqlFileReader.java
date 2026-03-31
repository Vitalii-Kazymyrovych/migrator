package com.vezha.migrator.reader;

import com.vezha.migrator.config.ConfigModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SqlFileReader implements SourceReader {

    private static final Path SOURCE_SQL_PATH = Path.of("source.sql");

    @Override
    public JdbcTemplate read(ConfigModel configModel) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:h2:mem:migrator-source;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        JdbcTemplate sourceJdbcTemplate = new JdbcTemplate(dataSource);
        executeSqlFile(sourceJdbcTemplate, sourceSqlPath().toString());
        return sourceJdbcTemplate;
    }

    private Path sourceSqlPath() {
        return SOURCE_SQL_PATH;
    }

    void executeSqlFile(JdbcTemplate jdbcTemplate, String path) {
        try {
            String content = Files.readString(Path.of(path));
            for (String statement : splitStatements(content)) {
                jdbcTemplate.execute(statement);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read SQL file: " + path, e);
        }
    }

    private List<String> splitStatements(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\'') {
                inString = !inString;
            }
            if (c == ';' && !inString) {
                addStatement(statements, current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        addStatement(statements, current.toString());
        return statements;
    }

    private void addStatement(List<String> statements, String raw) {
        String statement = stripCommentOnlyLines(raw).trim();
        if (!statement.isBlank()) {
            statements.add(statement);
        }
    }

    private String stripCommentOnlyLines(String raw) {
        String[] lines = raw.split("\\R");
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.startsWith("#")) {
                continue;
            }
            out.append(line).append(System.lineSeparator());
        }
        return out.toString();
    }
}
