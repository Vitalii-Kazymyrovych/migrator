package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class UsersMigrator extends BaseMigratorSupport implements TableMigrator {

    public UsersMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "users";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT * FROM users WHERE id != 1");
        if (rows.isEmpty()) return;

        // остальная логика transform
        rows.forEach(row -> {
            Object roleId = row.remove("role_id");
            int roleIdValue = roleId instanceof Number ? ((Number) roleId).intValue() : 0;
            row.put("role_ids", roleIdValue == 0 ? "[]" : "[" + roleIdValue + "]");
        });

        // вставка через directCopy невозможна с готовыми rows — используй batchUpdate напрямую
        List<String> columns = new java.util.ArrayList<>(rows.get(0).keySet());
        String sql = "INSERT INTO users (" + String.join(", ", columns) + ") VALUES (" +
                columns.stream().map(c -> "?").collect(java.util.stream.Collectors.joining(", ")) + ")";
        destinationJdbcTemplate.batchUpdate(sql, rows, 500,
                (ps, row) -> {
                    for (int i = 0; i < columns.size(); i++) ps.setObject(i + 1, row.get(columns.get(i)));
                });
    }
}
