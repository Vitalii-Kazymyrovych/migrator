package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

public class ClientsMigrator extends BaseMigratorSupport implements TableMigrator {

    public ClientsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "clients";
    }

    @Override
    public void migrate() {
        directCopy("clients", "clients");
    }
}
