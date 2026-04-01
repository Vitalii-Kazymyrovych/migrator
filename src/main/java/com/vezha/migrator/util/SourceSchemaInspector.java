package com.vezha.migrator.util;

import com.vezha.migrator.config.ConfigModel;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceSchemaInspector {

    private static final Path SOURCE_SQL_PATH = Path.of("source.sql");

    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "(?is)\\bcreate\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?([`\"\\[]?[a-zA-Z0-9_.$]+[`\"\\]]?)"
    );

    private final JdbcTemplate sourceJdbcTemplate;
    private Set<String> tables = Set.of();

    public SourceSchemaInspector(JdbcTemplate sourceJdbcTemplate) {
        this.sourceJdbcTemplate = sourceJdbcTemplate;
    }

    public void load(ConfigModel configModel) {
        if (configModel.getSource().getSqlFile().isEnabled()) {
            // Читаем из H2 который уже загружен из source.sql + oldDDL.md
            List<String> tableNames = sourceJdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables WHERE UPPER(table_schema) = 'PUBLIC'",
                    String.class
            );
            Set<String> loaded = new HashSet<>();
            for (String tableName : tableNames) {
                loaded.add(tableName.toLowerCase(Locale.ROOT));
            }
            tables = loaded;
            return;
        }

        String query = buildDbQuery(configModel);
        if (query == null) {
            tables = Set.of();
            return;
        }

        List<String> tableNames = sourceJdbcTemplate.queryForList(query, String.class);
        Set<String> loaded = new HashSet<>();
        for (String tableName : tableNames) {
            loaded.add(tableName.toLowerCase(Locale.ROOT));
        }
        tables = loaded;
    }

    public boolean tableExists(String tableName) {
        return tables.contains(tableName.toLowerCase(Locale.ROOT));
    }

    private String buildDbQuery(ConfigModel configModel) {
        if (configModel.getSource().getMysql().isEnabled()) {
            return "SELECT table_name FROM information_schema.tables WHERE table_schema = '"
                    + configModel.getSource().getMysql().getDatabase() + "'";
        }
        if (configModel.getSource().getPostgres().isEnabled()) {
            return "SELECT table_name FROM information_schema.tables WHERE table_schema = '"
                    + configModel.getSource().getPostgres().getSchema() + "'";
        }
        if (configModel.getSource().getMssql().isEnabled()) {
            return "SELECT table_name FROM information_schema.tables WHERE table_catalog = '"
                    + configModel.getSource().getMssql().getDatabase() + "'";
        }
        if (configModel.getSource().getOracle().isEnabled()) {
            return "SELECT table_name FROM user_tables";
        }
        if (configModel.getSource().getH2().isEnabled()) {
            return "SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC'";
        }
        return null;
    }

    private Set<String> parseSqlFileTables(String path) {
        try {
            String content = Files.readString(Path.of(path));
            Matcher matcher = CREATE_TABLE_PATTERN.matcher(content);
            Set<String> parsed = new HashSet<>();
            while (matcher.find()) {
                String rawName = matcher.group(1);
                String tableName = normalizeTableName(rawName);
                if (!tableName.isBlank()) {
                    parsed.add(tableName);
                }
            }
            return parsed;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read SQL file: " + path, e);
        }
    }

    private String normalizeTableName(String rawName) {
        String normalized = rawName.trim();
        if ((normalized.startsWith("`") && normalized.endsWith("`"))
                || (normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("[") && normalized.endsWith("]"))) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex >= 0) {
            normalized = normalized.substring(dotIndex + 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }
}
