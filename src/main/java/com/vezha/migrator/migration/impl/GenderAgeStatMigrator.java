package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

public class GenderAgeStatMigrator extends BaseMigratorSupport implements TableMigrator {

    public GenderAgeStatMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "gender_age_stat";
    }

    @Override
    public void migrate() {
        directCopy("gender_age_stat", "gender_age_stat");
    }
}
