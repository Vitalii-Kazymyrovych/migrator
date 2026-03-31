package com.vezha.migrator.migration.impl;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

abstract class BaseMigratorSupport {

    protected final JdbcTemplate sourceJdbcTemplate;
    protected final JdbcTemplate destinationJdbcTemplate;

    protected BaseMigratorSupport(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        this.sourceJdbcTemplate = sourceJdbcTemplate;
        this.destinationJdbcTemplate = destinationJdbcTemplate;
    }

    protected interface RowCustomizer {
        Map<String, Object> apply(Map<String, Object> row);
    }

    protected void directCopy(String sourceTable, String destinationTable) {
        directCopy(sourceTable, destinationTable, row -> row);
    }

    protected void directCopy(String sourceTable, String destinationTable, RowCustomizer rowCustomizer) {
        List<Map<String, Object>> rawRows = sourceJdbcTemplate.queryForList("SELECT * FROM " + sourceTable);
        if (rawRows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> rows = new ArrayList<>(rawRows.size());
        for (Map<String, Object> rawRow : rawRows) {
            rows.add(rowCustomizer.apply(new LinkedHashMap<>(rawRow)));
        }

        List<String> columns = new ArrayList<>(rows.get(0).keySet());
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");
        for (String column : columns) {
            columnJoiner.add(column);
            placeholderJoiner.add("?");
        }

        String sql = "INSERT INTO " + destinationTable + " (" + columnJoiner + ") VALUES (" + placeholderJoiner + ")";
        destinationJdbcTemplate.batchUpdate(sql, rows, 500, (PreparedStatement ps, Map<String, Object> row) -> {
            for (int i = 0; i < columns.size(); i++) {
                ps.setObject(i + 1, row.get(columns.get(i)));
            }
        });
    }

    protected static void setValues(PreparedStatement ps, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
    }
}
