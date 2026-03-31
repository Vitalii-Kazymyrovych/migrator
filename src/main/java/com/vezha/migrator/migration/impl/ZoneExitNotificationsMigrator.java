package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

public class ZoneExitNotificationsMigrator extends BaseMigratorSupport implements TableMigrator {

    public ZoneExitNotificationsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "zone_exit_notifications";
    }

    @Override
    public void migrate() {
        directCopy("zone_exit_notifications", "zone_exit_notifications", this::transform);
    }

    private Map<String, Object> transform(Map<String, Object> row) {
        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("object_id", row.get("object_id"));
        transformed.put("zone_id", row.get("zone_id"));
        transformed.put("seconds_in_zone", row.get("seconds_in_zone"));
        transformed.put("object_type", row.get("object_type"));
        transformed.put("notification_type", row.get("notification_type"));
        transformed.put("created_at", row.get("created_at"));
        transformed.put("client_id", row.get("client_id"));
        transformed.put("analytics_id", row.get("va_id"));
        return transformed;
    }
}
