package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

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
        directCopy("users", "users", row -> {
            Object roleId = row.remove("role_id");
            int roleIdValue = roleId instanceof Number ? ((Number) roleId).intValue() : 0;
            row.put("role_ids", roleIdValue == 0 ? "[]" : "[" + roleIdValue + "]");
            return row;
        });
    }
}
