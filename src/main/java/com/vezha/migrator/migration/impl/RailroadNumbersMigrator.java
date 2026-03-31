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

public class RailroadNumbersMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(RailroadNumbersMigrator.class);
    private static final String RAILROAD_PLUGIN = "RailroadsAnalyticsModule";

    private final StreamToAnalyticsResolver streamToAnalyticsResolver;

    public RailroadNumbersMigrator(JdbcTemplate sourceJdbcTemplate,
                                   JdbcTemplate destinationJdbcTemplate,
                                   StreamToAnalyticsResolver streamToAnalyticsResolver) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
        this.streamToAnalyticsResolver = streamToAnalyticsResolver;
    }

    @Override
    public String tableName() {
        return "railroad_numbers";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT * FROM railroad_numbers");
        if (rows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int streamId = ((Number) row.get("stream_id")).intValue();
            Integer analyticsId = streamToAnalyticsResolver.getFirstByPlugin(streamId, RAILROAD_PLUGIN).orElse(null);
            if (analyticsId == null) {
                log.warn("Skipping railroad_numbers.id={} due to missing analytics mapping for stream_id={}", row.get("id"), streamId);
                continue;
            }

            Map<String, Object> target = new LinkedHashMap<>();
            target.put("id", row.get("id"));
            target.put("number", row.get("number"));
            target.put("box", row.get("box"));
            target.put("number_image", row.get("number_image"));
            target.put("frame_image", row.get("frame_image"));
            target.put("created_at", row.get("created_at"));
            target.put("direction", row.get("direction"));
            target.put("client_id", row.get("client_id"));
            target.put("iso_code", row.get("iso_code"));
            target.put("analytics_id", analyticsId);
            target.put("zone", null);
            target.put("latitude", null);
            target.put("longitude", null);
            target.put("average_character_height", null);
            transformed.add(target);
        }

        if (transformed.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO railroad_numbers (id, number, box, number_image, frame_image, created_at, direction, client_id, iso_code, analytics_id, zone, latitude, longitude, average_character_height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("number"),
                        row.get("box"),
                        row.get("number_image"),
                        row.get("frame_image"),
                        row.get("created_at"),
                        row.get("direction"),
                        row.get("client_id"),
                        row.get("iso_code"),
                        row.get("analytics_id"),
                        row.get("zone"),
                        row.get("latitude"),
                        row.get("longitude"),
                        row.get("average_character_height")
                )
        );
    }
}
