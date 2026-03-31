package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

public class ObjectInZoneObjectTypeMigrator extends BaseMigratorSupport implements TableMigrator {

    public ObjectInZoneObjectTypeMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "object_in_zone_object_type";
    }

    @Override
    public void migrate() {
        directCopy("object_in_zone_object_type", "object_in_zone_object_type");
    }
}
