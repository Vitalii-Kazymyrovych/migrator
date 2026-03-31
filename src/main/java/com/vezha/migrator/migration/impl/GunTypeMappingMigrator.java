package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

public class GunTypeMappingMigrator extends BaseMigratorSupport implements TableMigrator {

    public GunTypeMappingMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "gun_type_mapping";
    }

    @Override
    public void migrate() {
        directCopy("gun_type_mapping", "gun_type_mapping");
    }
}
