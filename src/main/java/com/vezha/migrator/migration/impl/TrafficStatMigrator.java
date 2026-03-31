package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

public class TrafficStatMigrator extends BaseMigratorSupport implements TableMigrator {

    public TrafficStatMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "traffic_stat";
    }

    @Override
    public void migrate() {
        directCopy("traffic_stat", "traffic_stat", this::transform);
    }

    private Map<String, Object> transform(Map<String, Object> row) {
        row.put("x1", 0);
        row.put("y1", 0);
        row.put("x2", 0);
        row.put("y2", 0);
        row.put("confidence", 0);
        row.put("line_object", null);
        row.put("latitude", null);
        row.put("longitude", null);
        return row;
    }
}
