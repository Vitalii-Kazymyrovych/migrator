package com.vezha.migrator.migration.impl;

import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FaceListsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void mapsStreamsToAnalyticsIdsDropsEnabledAndSetsDefaults() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        StreamToAnalyticsResolver resolver = mock(StreamToAnalyticsResolver.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("name", "Employees");
        row.put("comment", "Main list");
        row.put("min_confidence", 60);
        row.put("events_holder", "both");
        row.put("status", "active");
        row.put("created_at", "2025-01-01");
        row.put("client_id", 10);
        row.put("color", "#fff");
        row.put("time_attendance", false);
        row.put("send_internal_notifications", 1);
        row.put("streams", "[11,12]");
        row.put("list_permissions", null);
        row.put("enabled", 0);

        when(source.queryForList("SELECT * FROM face_lists")).thenReturn(List.of(row));
        when(resolver.getAll(11)).thenReturn(List.of(101, 102));
        when(resolver.getAll(12)).thenReturn(List.of(102, 103));

        new FaceListsMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        var setterCaptor = org.mockito.ArgumentCaptor.forClass(org.springframework.jdbc.core.ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO face_lists (id, name, comment, min_confidence, events_holder, status, created_at, client_id, color, time_attendance, send_internal_notifications, list_permissions, analytics_ids, show_popup_for_internal_notifications) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                setterCaptor.capture()
        );

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals("[101,102,103]", transformed.get("analytics_ids"));
        assertEquals(true, transformed.get("send_internal_notifications"));
        assertEquals("", transformed.get("list_permissions"));
        assertEquals(false, transformed.get("show_popup_for_internal_notifications"));

        MigratorTestSupport.assertSetterValues(
                setterCaptor.getValue(),
                transformed,
                1,
                "Employees",
                "Main list",
                60,
                "both",
                "active",
                "2025-01-01",
                10,
                "#fff",
                false,
                true,
                "",
                "[101,102,103]",
                false
        );
    }
}
