package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class EventManagerMigrator extends BaseMigratorSupport implements TableMigrator {

    public EventManagerMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "event_manager";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList(
                "SELECT id, title, description, created_at, nodes, client_id FROM event_manager"
        );
        if (rows.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO event_manager (uuid, title, description, created_at, nodes, client_id) VALUES (?, ?, ?, ?, ?, ?)",
                rows,
                500,
                (ps, row) -> setValues(ps,
                        row.get("id"),
                        row.get("title"),
                        row.get("description"),
                        row.get("created_at"),
                        row.get("nodes"),
                        row.get("client_id"))
        );
    }
}
