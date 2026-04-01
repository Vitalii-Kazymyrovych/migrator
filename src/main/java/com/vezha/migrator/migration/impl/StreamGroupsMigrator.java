package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import com.vezha.migrator.util.StreamToAnalyticsGroupResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class StreamGroupsMigrator extends BaseMigratorSupport implements TableMigrator {

    private boolean migrateAnalyticsGroups = true;

    public StreamGroupsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate, StreamToAnalyticsGroupResolver streamToAnalyticsGroupResolver) {
        super(sourceJdbcTemplate, destinationJdbcTemplate, streamToAnalyticsGroupResolver);
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
            List<Map<String, Object>> pluginData = sourceJdbcTemplate.queryForList(
                    "SELECT DISTINCT sg.id as group_id, sg.parent_id, sg.name, sg.client_id, a.plugin_name " +
                            "FROM stream_groups sg " +
                            "JOIN streams s ON s.parent_id = sg.id " +
                            "JOIN analytics a ON a.stream_id = s.id " +
                            "WHERE a.plugin_name IS NOT NULL AND a.plugin_name != ''"
            );

            if (!pluginData.isEmpty()) {
                for (Map<String, Object> row : pluginData) {
                    int streamGroupId = ((Number) row.get("group_id")).intValue();
                    String pluginName = (String) row.get("plugin_name");

                    Integer analyticsGroupId = destinationJdbcTemplate.queryForObject(
                            "INSERT INTO analytics_groups (parent_id, name, client_id, plugin_name) VALUES (?, ?, ?, ?) RETURNING id",
                            Integer.class,
                            row.get("parent_id"),
                            row.get("name"),
                            row.get("client_id"),
                            pluginName
                    );
                    streamToAnalyticsGroupResolver.register(streamGroupId, pluginName, analyticsGroupId);
                }
            }
        }
    }
}
