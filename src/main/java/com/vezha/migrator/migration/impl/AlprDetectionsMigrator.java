package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlprDetectionsMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(AlprDetectionsMigrator.class);
    private static final String ALPR_PLUGIN = "AlprAnalyticsModule";

    private final StreamToAnalyticsResolver streamToAnalyticsResolver;

    public AlprDetectionsMigrator(JdbcTemplate sourceJdbcTemplate,
                                  JdbcTemplate destinationJdbcTemplate,
                                  StreamToAnalyticsResolver streamToAnalyticsResolver) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
        this.streamToAnalyticsResolver = streamToAnalyticsResolver;
    }

    @Override
    public String tableName() {
        return "alpr_detections";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> sourceRows = sourceJdbcTemplate.queryForList("SELECT * FROM alpr_plates");
        if (sourceRows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = sourceRows.stream().map(this::transform).toList();
        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO alpr_detections (id, plate_number, arabic_number, adr, box, plate_image, frame_image, make_model_id, vehicle_type, created_at, color_id, direction, country, pattern, client_id, analytics_id, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("plate_number"),
                        row.get("arabic_number"),
                        row.get("adr"),
                        row.get("box"),
                        row.get("plate_image"),
                        row.get("frame_image"),
                        row.get("make_model_id"),
                        row.get("vehicle_type"),
                        row.get("created_at"),
                        row.get("color_id"),
                        row.get("direction"),
                        row.get("country"),
                        row.get("pattern"),
                        row.get("client_id"),
                        row.get("analytics_id"),
                        row.get("latitude"),
                        row.get("longitude")
                )
        );
    }

    private Map<String, Object> transform(Map<String, Object> row) {
        int streamId = ((Number) row.get("stream_id")).intValue();
        Integer analyticsId = streamToAnalyticsResolver.getFirstByPlugin(streamId, ALPR_PLUGIN).orElse(null);
        if (analyticsId == null) {
            analyticsId = toInteger(row.get("va_id"));
            log.warn("No ALPR analytics mapping found for alpr_plates.id={}, fallback va_id={}", row.get("id"), analyticsId);
        }

        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("plate_number", row.get("plate_number"));
        transformed.put("arabic_number", row.get("arabic_number"));
        transformed.put("adr", row.get("adr"));
        transformed.put("box", row.get("box"));
        transformed.put("plate_image", row.get("plate_image"));
        transformed.put("frame_image", row.get("frame_image"));
        transformed.put("make_model_id", row.get("make_model_id"));
        transformed.put("vehicle_type", row.get("vehicle_type"));
        transformed.put("created_at", row.get("created_at"));
        transformed.put("color_id", row.get("color_id"));
        transformed.put("direction", row.get("direction"));
        transformed.put("country", row.get("country"));
        transformed.put("pattern", row.get("pattern"));
        transformed.put("client_id", row.get("client_id"));
        transformed.put("analytics_id", analyticsId);
        transformed.put("latitude", 0);
        transformed.put("longitude", 0);
        return transformed;
    }

    private Integer toInteger(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}
