package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

public class AlprListItemsMigrator extends BaseMigratorSupport implements TableMigrator {

    public AlprListItemsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "alpr_list_items";
    }

    @Override
    public void migrate() {
        directCopy("alpr_list_items", "alpr_list_items");
    }
}
