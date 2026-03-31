package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatsTrafficMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(StatsTrafficMigrator.class);

    public StatsTrafficMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "stats_traffic";
    }

    @Override
    public String getTargetTable() {
        return "stats_traffic_minutely";
    }

    @Override
    public List<String> getSourceTables() {
        return List.of("stats_traffic_hourly", "stats_traffic_minutely");
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> hourlyRows = querySourceRows("stats_traffic_hourly");
        List<Map<String, Object>> minutelyRows = querySourceRows("stats_traffic_minutely");
        log.info("Found {} rows in stats_traffic_hourly", hourlyRows.size());
        log.info("Found {} rows in stats_traffic_minutely", minutelyRows.size());

        if (hourlyRows.isEmpty() && minutelyRows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> mergedRows = new ArrayList<>(hourlyRows.size() + minutelyRows.size());
        hourlyRows.forEach(row -> mergedRows.add(stripId(row)));
        minutelyRows.forEach(row -> mergedRows.add(stripId(row)));

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO stats_traffic_minutely (va_id, line, type, count, direction, created_at, client_id, present) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                mergedRows,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("va_id"),
                        row.get("line"),
                        row.get("type"),
                        row.get("count"),
                        row.get("direction"),
                        row.get("created_at"),
                        row.get("client_id"),
                        row.get("present")
                )
        );
    }

    private List<Map<String, Object>> querySourceRows(String tableName) {
        try {
            return sourceJdbcTemplate.queryForList("SELECT * FROM " + tableName);
        } catch (DataAccessException exception) {
            log.info("Source table {} not available during migration, treating as empty", tableName);
            return List.of();
        }
    }

    private Map<String, Object> stripId(Map<String, Object> row) {
        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("va_id", row.get("va_id"));
        transformed.put("line", row.get("line"));
        transformed.put("type", row.get("type"));
        transformed.put("count", row.get("count"));
        transformed.put("direction", row.get("direction"));
        transformed.put("created_at", row.get("created_at"));
        transformed.put("client_id", row.get("client_id"));
        transformed.put("present", row.get("present"));
        return transformed;
    }
}
