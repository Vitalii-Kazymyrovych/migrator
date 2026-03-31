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

class PortLogisticsRulesMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void renamesRuleAnalyticsFieldsAndDefaultsNewFields() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 7);
        row.put("name", "R1");
        row.put("container_analytics_ids", "[]");
        row.put("created_at", "2026");
        row.put("start_at", "2026");
        row.put("status", "active");
        row.put("client_id", 1);
        row.put("buffer_time", 5);
        row.put("front_lpr_analytics_id", 100);
        row.put("back_lpr_analytics_id", 200);
        row.put("buffet_time", 10);

        when(source.queryForList("SELECT * FROM port_logistics_container_numbers")).thenReturn(List.of());
        when(source.queryForList("SELECT * FROM port_logistics_detections")).thenReturn(List.of());
        when(source.queryForList("SELECT * FROM port_logistics_rules")).thenReturn(List.of(row));

        new PortLogisticsMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(eq("INSERT INTO port_logistics_rules (id, name, container_analytics_ids, created_at, start_at, status, client_id, buffer_time, entry_lpr_analytics_id, exit_lpr_analytics_id, overview_stream_configs, entry_lpr_analytics_angle_degrees, exit_lpr_analytics_angle_degrees, is_reverse_enabled, group_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"), rowsCaptor.capture(), eq(500), any());
        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);

        assertEquals(100, transformed.get("entry_lpr_analytics_id"));
        assertEquals(200, transformed.get("exit_lpr_analytics_id"));
        assertEquals("", transformed.get("overview_stream_configs"));
        assertEquals(false, transformed.get("is_reverse_enabled"));
    }
}
