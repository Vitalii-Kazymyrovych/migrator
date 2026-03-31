package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class ServersMigrator extends BaseMigratorSupport implements TableMigrator {

    public ServersMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "servers";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT id, name FROM servers");
        if (rows.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO servers (id, name, is_external_address_enabled, address, port) VALUES (?, ?, ?, ?, ?)",
                rows,
                500,
                (ps, row) -> setValues(ps,
                        row.get("id"),
                        row.get("name"),
                        false,
                        null,
                        null)
        );
    }
}
