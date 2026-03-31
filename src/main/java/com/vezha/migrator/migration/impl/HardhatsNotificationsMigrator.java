package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

public class HardhatsNotificationsMigrator extends BaseMigratorSupport implements TableMigrator {

    public HardhatsNotificationsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "hardhats_notifications";
    }

    @Override
    public void migrate() {
        directCopy("hardhats_notifications", "hardhats_notifications", this::transform);
    }

    private Map<String, Object> transform(Map<String, Object> row) {
        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("status", row.get("status"));
        transformed.put("accepted_by", row.get("accepted_by"));
        transformed.put("objects", row.get("objects"));
        transformed.put("va_id", row.get("va_id"));
        transformed.put("frame_image", row.get("frame_image"));
        transformed.put("thumbnail_image", row.get("thumbnail_image"));
        transformed.put("created_at", row.get("created_at"));
        transformed.put("zone", row.get("zone"));
        transformed.put("client_id", row.get("client_id"));
        transformed.put("latitude", null);
        transformed.put("longitude", null);
        return transformed;
    }
}
