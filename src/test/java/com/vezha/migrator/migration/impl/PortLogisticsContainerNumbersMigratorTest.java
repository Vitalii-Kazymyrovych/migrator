package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortLogisticsContainerNumbersMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void fillsRecognizedAtAndAnalyticsIdDefaults() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 5);
        row.put("number", "MSCU123");
        row.put("iso", "22G1");
        row.put("box", "[]");
        row.put("number_image", "n.jpg");
        row.put("frame_image", "f.jpg");
        row.put("detection_id", 99);
        row.put("recognized_at", null);

        when(source.queryForList("SELECT * FROM port_logistics_container_numbers")).thenReturn(List.of(row));
        when(source.queryForList("SELECT * FROM port_logistics_detections")).thenReturn(List.of());
        when(source.queryForList("SELECT * FROM port_logistics_rules")).thenReturn(List.of());

        new PortLogisticsMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(eq("INSERT INTO port_logistics_container_numbers (id, number, iso, box, number_image, frame_image, detection_id, recognized_at, analytics_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"), rowsCaptor.capture(), eq(500), any());

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertNotNull(transformed.get("recognized_at"));
        assertEquals(0, transformed.get("analytics_id"));
    }
}
