package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortLogisticsDetectionsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void setsDetectionDefaultColumns() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 6);
        row.put("truck_number", "1");
        row.put("truck_arabic_number", "1");
        row.put("truck_box", "[]");
        row.put("truck_plate_image", "a");
        row.put("truck_frame_image", "b");
        row.put("trailer_number", "2");
        row.put("trailer_arabic_number", "2");
        row.put("trailer_box", "[]");
        row.put("trailer_plate_image", "c");
        row.put("trailer_frame_image", "d");
        row.put("adr", null);
        row.put("pattern", "p");
        row.put("country", "US");
        row.put("state", "CA");
        row.put("make_model_id", 1);
        row.put("vehicle_type", "truck");
        row.put("color_id", 1);
        row.put("client_id", 2);
        row.put("rule_id", 3);
        row.put("created_at", "2026");
        row.put("front_plate_recognized_at", "2026");
        row.put("trailer_plate_recognized_at", "2026");
        row.put("back_plate_number", "3");
        row.put("back_plate_arabic_number", "3");
        row.put("back_plate_box", "[]");
        row.put("back_plate_image", "e");
        row.put("back_plate_frame_image", "f");
        row.put("back_plate_recognized_at", "2026");

        when(source.queryForList("SELECT * FROM port_logistics_container_numbers")).thenReturn(List.of());
        when(source.queryForList("SELECT * FROM port_logistics_detections")).thenReturn(List.of(row));
        when(source.queryForList("SELECT * FROM port_logistics_rules")).thenReturn(List.of());

        new PortLogisticsMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(eq("INSERT INTO port_logistics_detections (id, truck_number, truck_arabic_number, truck_box, truck_plate_image, truck_frame_image, trailer_number, trailer_arabic_number, trailer_box, trailer_plate_image, trailer_frame_image, adr, pattern, country, state, make_model_id, vehicle_type, color_id, client_id, rule_id, created_at, front_plate_recognized_at, trailer_plate_recognized_at, back_plate_number, back_plate_arabic_number, back_plate_box, back_plate_image, back_plate_frame_image, back_plate_recognized_at, direction, front_plate_angle_degrees, back_plate_angle_degrees, trailer_plate_angle_degrees, overview_snapshots, front_plate_analytics_id, back_plate_analytics_id, trailer_plate_analytics_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"), rowsCaptor.capture(), eq(500), any());
        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);

        assertEquals("unknown", transformed.get("direction"));
        assertEquals(0, transformed.get("front_plate_analytics_id"));
    }
}
