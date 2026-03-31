package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlprSpeedRulesMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(AlprSpeedRulesMigrator.class);
    private static final String ALPR_PLUGIN = "AlprAnalyticsModule";

    private final StreamToAnalyticsResolver streamToAnalyticsResolver;

    public AlprSpeedRulesMigrator(JdbcTemplate sourceJdbcTemplate,
                                  JdbcTemplate destinationJdbcTemplate,
                                  StreamToAnalyticsResolver streamToAnalyticsResolver) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
        this.streamToAnalyticsResolver = streamToAnalyticsResolver;
    }

    @Override
    public String tableName() {
        return "alpr_speed_rules";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT * FROM alpr_speed_rules");
        if (rows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = rows.stream().map(this::transform).toList();
        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO alpr_speed_rules (id, name, speed_limit, speed_unit, distance, distance_unit, min_speed, min_speed_unit, max_duration, events_holder, client_id, analytics_id1, analytics_id2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("name"),
                        row.get("speed_limit"),
                        row.get("speed_unit"),
                        row.get("distance"),
                        row.get("distance_unit"),
                        row.get("min_speed"),
                        row.get("min_speed_unit"),
                        row.get("max_duration"),
                        row.get("events_holder"),
                        row.get("client_id"),
                        row.get("analytics_id1"),
                        row.get("analytics_id2")
                )
        );
    }

    private Map<String, Object> transform(Map<String, Object> row) {
        Integer analyticsId1 = resolve((Number) row.get("stream_id1"), row.get("id"), "stream_id1");
        Integer analyticsId2 = resolve((Number) row.get("stream_id2"), row.get("id"), "stream_id2");

        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("name", row.get("name"));
        transformed.put("speed_limit", row.get("speed_limit"));
        transformed.put("speed_unit", row.get("speed_unit"));
        transformed.put("distance", row.get("distance"));
        transformed.put("distance_unit", row.get("distance_unit"));
        transformed.put("min_speed", row.get("min_speed"));
        transformed.put("min_speed_unit", row.get("min_speed_unit"));
        transformed.put("max_duration", row.get("max_duration"));
        transformed.put("events_holder", row.get("events_holder"));
        transformed.put("client_id", row.get("client_id"));
        transformed.put("analytics_id1", analyticsId1);
        transformed.put("analytics_id2", analyticsId2);
        return transformed;
    }

    private Integer resolve(Number streamIdRaw, Object rowId, String streamColumn) {
        if (streamIdRaw == null) {
            log.warn("No stream id in {} for alpr_speed_rules.id={}, defaulting analytics_id to 0", streamColumn, rowId);
            return 0;
        }

        int streamId = streamIdRaw.intValue();
        Integer resolved = streamToAnalyticsResolver.getFirstByPlugin(streamId, ALPR_PLUGIN).orElse(null);
        if (resolved == null) {
            log.warn("No ALPR analytics mapping for alpr_speed_rules.id={} {}={}, defaulting analytics_id to 0", rowId, streamColumn, streamId);
            return 0;
        }
        return resolved;
    }
}
