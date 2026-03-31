package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

public class SmokeFireTypeMappingMigrator extends BaseMigratorSupport implements TableMigrator {

    public SmokeFireTypeMappingMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "smoke_fire_type_mapping";
    }

    @Override
    public void migrate() {
        directCopy("smoke_fire_type_mapping", "smoke_fire_type_mapping");
    }
}
