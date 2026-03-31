package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RolesMigrator extends BaseMigratorSupport implements TableMigrator {

    public RolesMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "roles";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT id, role_name, permissions, client_id FROM roles");
        if (rows.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO roles (id, role_name, permissions, client_id, description) VALUES (?, ?, ?, ?, ?)",
                rows,
                500,
                (ps, row) -> setValues(ps,
                        row.get("id"),
                        row.get("role_name"),
                        row.get("permissions"),
                        row.get("client_id"),
                        null)
        );
    }
}
