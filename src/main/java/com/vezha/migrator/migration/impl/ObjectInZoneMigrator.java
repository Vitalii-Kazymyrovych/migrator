package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectInZoneMigrator extends BaseMigratorSupport implements TableMigrator {

    public ObjectInZoneMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "object_in_zone_notifications";
    }

    @Override
    public void migrate() {
        directCopy("object_in_zone_notifications", "object_in_zone_notifications", this::transform);
    }

    private Map<String, Object> transform(Map<String, Object> row) {
        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("status", row.get("status"));
        transformed.put("accepted_by", row.get("accepted_by"));
        transformed.put("frame_image", row.get("frame_image"));
        transformed.put("thumbnail_image", row.get("thumbnail_image"));
        transformed.put("zone", row.get("zone"));
        transformed.put("dwell_time", row.get("dwell_time"));
        transformed.put("trigger", row.get("trigger"));
        transformed.put("notification_type", row.get("notification_type"));
        transformed.put("action_type", row.get("action_type"));
        transformed.put("resolution", row.get("resolution"));
        transformed.put("created_at", row.get("created_at"));
        transformed.put("client_id", row.get("client_id"));
        transformed.put("analytics_id", row.get("va_id"));
        transformed.put("latitude", null);
        transformed.put("longitude", null);
        return transformed;
    }
}
