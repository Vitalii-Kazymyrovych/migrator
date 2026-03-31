package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public class StreamsMigrator extends BaseMigratorSupport implements TableMigrator {

    public StreamsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "streams";
    }

    @Override
    public void migrate() {
        directCopy("streams", "streams", row -> {
            Object uuidValue = row.get("uuid");
            if (uuidValue != null) {
                row.put("uuid", UUID.fromString(uuidValue.toString()));
            }
            return row;
        });
    }
}
