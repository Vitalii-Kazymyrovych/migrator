package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class StreamGroupsMigrator extends BaseMigratorSupport implements TableMigrator {

    private boolean migrateAnalyticsGroups = true;

    public StreamGroupsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "stream_groups";
    }

    @Override
    public String getTargetTable() {
        return "stream_groups";
    }

    public void setMigrateAnalyticsGroups(boolean migrateAnalyticsGroups) {
        this.migrateAnalyticsGroups = migrateAnalyticsGroups;
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList(
                "SELECT id, parent_id, name, client_id FROM stream_groups"
        );
        if (rows.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO stream_groups (id, parent_id, name, client_id) OVERRIDING SYSTEM VALUE VALUES (?, ?, ?, ?)",
                rows,
                500,
                (ps, row) -> setValues(ps, row.get("id"), row.get("parent_id"), row.get("name"), row.get("client_id"))
        );

        if (migrateAnalyticsGroups) {
            destinationJdbcTemplate.batchUpdate(
                    "INSERT INTO analytics_groups (id, parent_id, name, client_id, plugin_name) OVERRIDING SYSTEM VALUE VALUES (?, ?, ?, ?, ?)",
                    rows,
                    500,
                    (ps, row) -> setValues(ps, row.get("id"), row.get("parent_id"), row.get("name"), row.get("client_id"), "")
            );
        }
    }
}
