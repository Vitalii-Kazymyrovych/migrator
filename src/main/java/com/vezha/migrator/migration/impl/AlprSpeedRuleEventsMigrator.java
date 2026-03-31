package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlprSpeedRuleEventsMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(AlprSpeedRuleEventsMigrator.class);

    public AlprSpeedRuleEventsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "alpr_speed_rule_events";
    }

    @Override
    public void migrate() {
        log.info("alpr_speed_rule_events migration maps old speed_limit column into new speed_value column.");
        log.info("alpr_speed_rule_events migration drops old per-event plate/frame/timestamp details due to schema mismatch.");

        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT * FROM alpr_speed_rule_events");
        if (rows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = rows.stream().map(this::transform).toList();
        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO alpr_speed_rule_events (id, rule_id, speed_value, speed_unit, detection1_id, detection2_id) VALUES (?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("rule_id"),
                        row.get("speed_value"),
                        row.get("speed_unit"),
                        row.get("detection1_id"),
                        row.get("detection2_id")
                )
        );
    }

    private Map<String, Object> transform(Map<String, Object> row) {
        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("rule_id", row.get("rule_id"));
        transformed.put("speed_value", row.get("speed_limit"));
        transformed.put("speed_unit", row.get("speed_unit"));
        transformed.put("detection1_id", null);
        transformed.put("detection2_id", null);
        return transformed;
    }
}
