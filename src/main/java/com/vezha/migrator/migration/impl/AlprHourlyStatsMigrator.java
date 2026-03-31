package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlprHourlyStatsMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(AlprHourlyStatsMigrator.class);
    private static final String ALPR_PLUGIN = "AlprAnalyticsModule";

    private final StreamToAnalyticsResolver streamToAnalyticsResolver;

    public AlprHourlyStatsMigrator(JdbcTemplate sourceJdbcTemplate,
                                   JdbcTemplate destinationJdbcTemplate,
                                   StreamToAnalyticsResolver streamToAnalyticsResolver) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
        this.streamToAnalyticsResolver = streamToAnalyticsResolver;
    }

    @Override
    public String tableName() {
        return "alpr_hourly_statistics";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT * FROM alpr_stats_hourly");
        if (rows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int streamId = ((Number) row.get("stream_id")).intValue();
            Integer analyticsId = streamToAnalyticsResolver.getFirstByPlugin(streamId, ALPR_PLUGIN).orElse(null);
            if (analyticsId == null) {
                log.warn("Skipping alpr_stats_hourly.id={} because analytics_id was not resolved for stream_id={}", row.get("id"), streamId);
                continue;
            }

            Map<String, Object> target = new LinkedHashMap<>();
            target.put("id", row.get("id"));
            target.put("total", row.get("total"));
            target.put("numbers", row.get("numbers"));
            target.put("make_models", row.get("make_models"));
            target.put("created_at", row.get("created_at"));
            target.put("client_id", row.get("client_id"));
            target.put("analytics_id", analyticsId);
            transformed.add(target);
        }

        if (transformed.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO alpr_hourly_statistics (id, total, numbers, make_models, created_at, client_id, analytics_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("total"),
                        row.get("numbers"),
                        row.get("make_models"),
                        row.get("created_at"),
                        row.get("client_id"),
                        row.get("analytics_id")
                )
        );
    }
}
