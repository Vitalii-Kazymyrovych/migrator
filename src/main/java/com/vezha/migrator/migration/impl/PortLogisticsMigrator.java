package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PortLogisticsMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(PortLogisticsMigrator.class);

    public PortLogisticsMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "port_logistics";
    }

    @Override
    public void migrate() {
        migrateContainerNumbers();
        migrateDetections();
        migrateRules();
    }

    private void migrateContainerNumbers() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT * FROM port_logistics_container_numbers");
        if (rows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = rows.stream().map(this::transformContainerNumber).toList();
        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO port_logistics_container_numbers (id, number, iso, box, number_image, frame_image, detection_id, recognized_at, analytics_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("number"),
                        row.get("iso"),
                        row.get("box"),
                        row.get("number_image"),
                        row.get("frame_image"),
                        row.get("detection_id"),
                        row.get("recognized_at"),
                        row.get("analytics_id")
                )
        );
    }

    private Map<String, Object> transformContainerNumber(Map<String, Object> row) {
        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("number", row.get("number"));
        transformed.put("iso", row.get("iso"));
        transformed.put("box", row.get("box"));
        transformed.put("number_image", row.get("number_image"));
        transformed.put("frame_image", row.get("frame_image"));
        transformed.put("detection_id", row.get("detection_id"));

        Object recognizedAt = row.get("recognized_at");
        if (recognizedAt == null) {
            log.warn("port_logistics_container_numbers.id={} has NULL recognized_at, using CURRENT_TIMESTAMP", row.get("id"));
            recognizedAt = Timestamp.from(Instant.now());
        }
        transformed.put("recognized_at", recognizedAt);
        transformed.put("analytics_id", 0);
        return transformed;
    }

    private void migrateDetections() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT * FROM port_logistics_detections");
        if (rows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = rows.stream().map(this::transformDetection).toList();
        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO port_logistics_detections (id, truck_number, truck_arabic_number, truck_box, truck_plate_image, truck_frame_image, trailer_number, trailer_arabic_number, trailer_box, trailer_plate_image, trailer_frame_image, adr, pattern, country, state, make_model_id, vehicle_type, color_id, client_id, rule_id, created_at, front_plate_recognized_at, trailer_plate_recognized_at, back_plate_number, back_plate_arabic_number, back_plate_box, back_plate_image, back_plate_frame_image, back_plate_recognized_at, direction, front_plate_angle_degrees, back_plate_angle_degrees, trailer_plate_angle_degrees, overview_snapshots, front_plate_analytics_id, back_plate_analytics_id, trailer_plate_analytics_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("truck_number"),
                        row.get("truck_arabic_number"),
                        row.get("truck_box"),
                        row.get("truck_plate_image"),
                        row.get("truck_frame_image"),
                        row.get("trailer_number"),
                        row.get("trailer_arabic_number"),
                        row.get("trailer_box"),
                        row.get("trailer_plate_image"),
                        row.get("trailer_frame_image"),
                        row.get("adr"),
                        row.get("pattern"),
                        row.get("country"),
                        row.get("state"),
                        row.get("make_model_id"),
                        row.get("vehicle_type"),
                        row.get("color_id"),
                        row.get("client_id"),
                        row.get("rule_id"),
                        row.get("created_at"),
                        row.get("front_plate_recognized_at"),
                        row.get("trailer_plate_recognized_at"),
                        row.get("back_plate_number"),
                        row.get("back_plate_arabic_number"),
                        row.get("back_plate_box"),
                        row.get("back_plate_image"),
                        row.get("back_plate_frame_image"),
                        row.get("back_plate_recognized_at"),
                        row.get("direction"),
                        row.get("front_plate_angle_degrees"),
                        row.get("back_plate_angle_degrees"),
                        row.get("trailer_plate_angle_degrees"),
                        row.get("overview_snapshots"),
                        row.get("front_plate_analytics_id"),
                        row.get("back_plate_analytics_id"),
                        row.get("trailer_plate_analytics_id")
                )
        );
    }

    private Map<String, Object> transformDetection(Map<String, Object> row) {
        Map<String, Object> transformed = new LinkedHashMap<>(row);
        transformed.put("direction", "unknown");
        transformed.put("front_plate_angle_degrees", null);
        transformed.put("back_plate_angle_degrees", null);
        transformed.put("trailer_plate_angle_degrees", null);
        transformed.put("overview_snapshots", null);
        transformed.put("front_plate_analytics_id", 0);
        transformed.put("back_plate_analytics_id", 0);
        transformed.put("trailer_plate_analytics_id", 0);
        return transformed;
    }

    private void migrateRules() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT * FROM port_logistics_rules");
        if (rows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformed = rows.stream().map(this::transformRule).toList();
        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO port_logistics_rules (id, name, container_analytics_ids, created_at, start_at, status, client_id, buffer_time, entry_lpr_analytics_id, exit_lpr_analytics_id, overview_stream_configs, entry_lpr_analytics_angle_degrees, exit_lpr_analytics_angle_degrees, is_reverse_enabled, group_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transformed,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("name"),
                        row.get("container_analytics_ids"),
                        row.get("created_at"),
                        row.get("start_at"),
                        row.get("status"),
                        row.get("client_id"),
                        row.get("buffer_time"),
                        row.get("entry_lpr_analytics_id"),
                        row.get("exit_lpr_analytics_id"),
                        row.get("overview_stream_configs"),
                        row.get("entry_lpr_analytics_angle_degrees"),
                        row.get("exit_lpr_analytics_angle_degrees"),
                        row.get("is_reverse_enabled"),
                        row.get("group_id")
                )
        );
    }

    private Map<String, Object> transformRule(Map<String, Object> row) {
        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", row.get("id"));
        transformed.put("name", row.get("name"));
        transformed.put("container_analytics_ids", row.get("container_analytics_ids"));
        transformed.put("created_at", row.get("created_at"));
        transformed.put("start_at", row.get("start_at"));
        transformed.put("status", row.get("status"));
        transformed.put("client_id", row.get("client_id"));
        transformed.put("buffer_time", row.get("buffer_time"));
        transformed.put("entry_lpr_analytics_id", row.get("front_lpr_analytics_id"));
        transformed.put("exit_lpr_analytics_id", row.get("back_lpr_analytics_id"));
        transformed.put("overview_stream_configs", "");
        transformed.put("entry_lpr_analytics_angle_degrees", 0);
        transformed.put("exit_lpr_analytics_angle_degrees", 0);
        transformed.put("is_reverse_enabled", false);
        transformed.put("group_id", 0);
        return transformed;
    }
}
