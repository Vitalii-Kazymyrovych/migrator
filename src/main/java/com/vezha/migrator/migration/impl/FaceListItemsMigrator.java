package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

public class FaceListItemsMigrator extends BaseMigratorSupport implements TableMigrator {

    public FaceListItemsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "face_list_items";
    }

    @Override
    public void migrate() {
        directCopy("face_list_items", "face_list_items", row -> {
            row.put("expiration_settings_enabled", false);
            row.put("expiration_settings_action", "none");
            row.put("expiration_settings_list_id", null);
            row.put("expiration_settings_date", null);
            row.put("expiration_settings_events_holder", null);
            return row;
        });
    }
}
