package com.vezha.migrator.reader;

import com.vezha.migrator.config.ConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SqlFileReader implements SourceReader {
    private static final Logger log = LoggerFactory.getLogger(SqlFileReader.class);

    private static final Path SOURCE_SQL_PATH = Path.of("source.sql");
    private static final Path SOURCE_DDL_PATH = Path.of("oldDDL.md");

    @Override
    public JdbcTemplate read(ConfigModel configModel) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:h2:mem:migrator-source;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        JdbcTemplate sourceJdbcTemplate = new JdbcTemplate(dataSource);
        sourceJdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS videoanalytics");
        if (Files.exists(sourceDdlPath())) {
            executeSqlFile(sourceJdbcTemplate, sourceDdlPath().toString());
        }
        executeSqlFile(sourceJdbcTemplate, sourceSqlPath().toString());
        return sourceJdbcTemplate;
    }

    private Path sourceSqlPath() {
        return SOURCE_SQL_PATH;
    }

    private Path sourceDdlPath() {
        return SOURCE_DDL_PATH;
    }

    void executeSqlFile(JdbcTemplate jdbcTemplate, String path) {
        try {
            String content = Files.readString(Path.of(path));
            for (String statement : splitStatements(content)) {
                try {
                    jdbcTemplate.execute(normalizeForH2(statement));
                } catch (DataAccessException ex) {
                    log.warn("Skipping SQL statement from {} due to parser incompatibility: {}", path, ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read SQL file: " + path, e);
        }
    }

    private String normalizeForH2(String statement) {
        return statement
                .replace("\\'", "''")
                .replaceAll("(?i)\\s+CHARACTER\\s+SET\\s+\\w+", "")
                .replaceAll("(?i)\\s+COLLATE\\s+\\w+", "")
                .replaceAll("(?i)b'([01])'", "$1")
                .replaceAll("(?i)\\)\\s*ENGINE\\s*=\\s*\\w+.*$", ")");
    }

    private List<String> splitStatements(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '\'' && !isEscapedByBackslash(content, i)) {
                if (inString && i + 1 < content.length() && content.charAt(i + 1) == '\'') {
                    current.append(c);
                    current.append(content.charAt(i + 1));
                    i++;
                    continue;
                }
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

    private boolean isEscapedByBackslash(String content, int quoteIndex) {
        int backslashCount = 0;
        for (int i = quoteIndex - 1; i >= 0 && content.charAt(i) == '\\'; i--) {
            backslashCount++;
        }
        return backslashCount % 2 != 0;
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
