package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

public class ZoneExitNotificationsObjectTypeMigrator extends BaseMigratorSupport implements TableMigrator {

    public ZoneExitNotificationsObjectTypeMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "zone_exit_notifications_object_type";
    }

    @Override
    public void migrate() {
        directCopy("zone_exit_notifications_object_type", "zone_exit_notifications_object_type");
    }
}
