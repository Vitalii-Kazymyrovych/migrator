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
import java.util.stream.Collectors;

public class SqlFileReader implements SourceReader {
    private static final Logger log = LoggerFactory.getLogger(SqlFileReader.class);

    private static final Path SOURCE_SQL_PATH = Path.of("source.sql");
    private static final Path SOURCE_DDL_PATH = Path.of("oldDDL.md");

    @Override
    public JdbcTemplate read(ConfigModel configModel) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:h2:mem:migrator-source;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        JdbcTemplate sourceJdbcTemplate = new JdbcTemplate(dataSource);
        sourceJdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS videoanalytics");
        sourceJdbcTemplate.execute("SET SCHEMA videoanalytics");
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
                if (isCopyStatement(statement)) continue;
                for (String expanded : expandMultiRowInserts(statement)) {
                    try {
                        String normalized = normalizeForH2(expanded);
                        jdbcTemplate.execute(normalized);
                        log.debug("OK: {}", normalized.substring(0, Math.min(80, normalized.length())));
                    } catch (DataAccessException ex) {
                        String rootCause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
                        log.warn("Skipping SQL statement from {} due to parser incompatibility: {} | rootCause={}", path, ex.getMessage(), rootCause);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read SQL file: " + path, e);
        }
    }

    private boolean isCopyStatement(String statement) {
        return statement.trim().toUpperCase().startsWith("COPY ");
    }

    private String normalizeForH2(String statement) {
        return statement
                .replaceAll("(?i)\\bvideoanalytics\\.", "")
                .replaceAll("(?i)\\bCREATE TABLE\\b(?!\\s+IF\\s+NOT\\s+EXISTS)", "CREATE TABLE IF NOT EXISTS")
                .replaceAll("(?i)^\\s*SET\\s+search_path\\s*=.*$", "")
                .replace("\\'", "''")
                .replace("`", "\"")
                // екранувати Value тільки якщо воно ще не в лапках
                .replaceAll("(?i)(?<!\")\\bValue\\b(?!\")", "\"Value\"")
                .replaceAll("(?i)\\s+CHARACTER\\s+SET\\s+\\w+", "")
                .replaceAll("(?i)\\s+COLLATE\\s+\\w+", "")
                .replaceAll("(?i)b'([01])'", "$1")
                .replaceAll("(?im)^\\s*(UNIQUE\\s+)?KEY\\s+[^\\n]*\\n", "")
                .replaceAll("(?im)^\\s*CONSTRAINT\\s+[^\\n]*FOREIGN\\s+KEY[^\\n]*\\n", "")
                .replaceAll(",\\s*\\)", ")")
                .replaceAll("(?i)\\)\\s*ENGINE\\s*=\\s*\\w+.*$", ")")
                .replaceAll("(?im)^\\s*SET\\s+\\w+\\s*=.*$", "")
                .replaceAll("(?i)\\bCACHE\\s+\\d+", "");
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

    private List<String> expandMultiRowInserts(String statement) {
        // якщо це не INSERT з кількома VALUES-блоками — повертаємо як є
        if (!statement.trim().toUpperCase().startsWith("INSERT")) {
            return List.of(statement);
        }
        // шукаємо патерн: INSERT INTO t (cols) VALUES (...), (...), (...)
        int valuesIdx = statement.toUpperCase().indexOf("VALUES");
        if (valuesIdx == -1) return List.of(statement);

        String prefix = statement.substring(0, valuesIdx + "VALUES".length());
        String valuesPart = statement.substring(valuesIdx + "VALUES".length()).trim();

        List<String> rows = splitValueRows(valuesPart);
        if (rows.size() <= 1) return List.of(statement);

        return rows.stream()
                .map(row -> prefix + " " + row)
                .collect(Collectors.toList());
    }

    private List<String> splitValueRows(String valuesPart) {
        List<String> rows = new ArrayList<>();
        int depth = 0;
        int start = 0;

        for (int i = 0; i < valuesPart.length(); i++) {
            char c = valuesPart.charAt(i);
            if (c == '(') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    rows.add(valuesPart.substring(start, i + 1));
                }
            }
        }
        return rows;
    }
}
