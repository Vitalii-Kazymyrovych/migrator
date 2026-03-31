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

public class AlprListEventsMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(AlprListEventsMigrator.class);
    private static final String ALPR_PLUGIN = "AlprAnalyticsModule";

    private final StreamToAnalyticsResolver streamToAnalyticsResolver;

    public AlprListEventsMigrator(JdbcTemplate sourceJdbcTemplate,
                                  JdbcTemplate destinationJdbcTemplate,
                                  StreamToAnalyticsResolver streamToAnalyticsResolver) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
        this.streamToAnalyticsResolver = streamToAnalyticsResolver;
    }

    @Override
    public String tableName() {
        return "alpr_list_events";
    }

    @Override
    public List<String> getSourceTables() {
        return List.of("alpr_notifications", "alpr_plates");
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> joinedRows = sourceJdbcTemplate.queryForList(
                "SELECT n.id, n.plate_id, n.list_id, n.list_item_id, n.list_item_name, n.status, n.accepted_by, n.created_at, "
                        + "p.id AS plate_exists, p.plate_number, p.arabic_number, p.adr, p.box, p.plate_image, p.frame_image, "
                        + "p.make_model_id, p.vehicle_type, p.color_id, p.direction, p.country, p.pattern, p.client_id, p.stream_id, p.va_id "
                        + "FROM alpr_notifications n LEFT JOIN alpr_plates p ON p.id = n.plate_id"
        );

        if (joinedRows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = new ArrayList<>();
        for (Map<String, Object> row : joinedRows) {
            if (row.get("plate_exists") == null) {
                log.error("Skipping alpr_notifications.id={} because plate_id={} does not exist", row.get("id"), row.get("plate_id"));
                continue;
            }
            transformed.add(transform(row));
        }

        if (transformed.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO alpr_list_events (id, detection_id, list_id, list_item_id, list_item_name, status, accepted_by, created_at, plate_number, arabic_number, adr, box, plate_image, frame_image, make_model_id, vehicle_type, color_id, direction, country, pattern, client_id, analytics_id, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("detection_id"),
                        row.get("list_id"),
                        row.get("list_item_id"),
                        row.get("list_item_name"),
                        row.get("status"),
                        row.get("accepted_by"),
                        row.get("created_at"),
                        row.get("plate_number"),
                        row.get("arabic_number"),
                        row.get("adr"),
                        row.get("box"),
                        row.get("plate_image"),
                        row.get("frame_image"),
                        row.get("make_model_id"),
                        row.get("vehicle_type"),
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
        Integer analyticsId = streamToAnalyticsResolver.getFirstByPlugin(streamId, ALPR_PLUGIN)
                .orElse(toInteger(row.get("va_id")));

        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("detection_id", row.get("plate_id"));
        transformed.put("list_id", row.get("list_id"));
        transformed.put("list_item_id", row.get("list_item_id"));
        transformed.put("list_item_name", row.get("list_item_name"));
        transformed.put("status", row.get("status"));
        transformed.put("accepted_by", row.get("accepted_by"));
        transformed.put("created_at", row.get("created_at"));
        transformed.put("plate_number", row.get("plate_number"));
        transformed.put("arabic_number", row.get("arabic_number"));
        transformed.put("adr", row.get("adr"));
        transformed.put("box", row.get("box"));
        transformed.put("plate_image", row.get("plate_image"));
        transformed.put("frame_image", row.get("frame_image"));
        transformed.put("make_model_id", row.get("make_model_id"));
        transformed.put("vehicle_type", row.get("vehicle_type"));
        transformed.put("color_id", row.get("color_id"));
        transformed.put("direction", row.get("direction"));
        transformed.put("country", row.get("country"));
        transformed.put("pattern", row.get("pattern"));
        transformed.put("client_id", row.get("client_id"));
        transformed.put("analytics_id", analyticsId);
        transformed.put("latitude", null);
        transformed.put("longitude", null);
        return transformed;
    }

    private Integer toInteger(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}
