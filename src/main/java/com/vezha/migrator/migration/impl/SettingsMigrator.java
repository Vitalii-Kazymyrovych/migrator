package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class SettingsMigrator extends BaseMigratorSupport implements TableMigrator {

    public SettingsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "settings";
    }

    @Override
    public String getTargetTable() {
        return "system_settings";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT Variable_name, Value FROM settings");
        if (rows.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO system_settings (variable_name, value) VALUES (?, ?)",
                rows,
                500,
                (ps, row) -> setValues(ps,
                        row.get("Variable_name"),
                        row.get("Value"))
        );
    }
}
