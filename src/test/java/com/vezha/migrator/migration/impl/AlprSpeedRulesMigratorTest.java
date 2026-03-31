package com.vezha.migrator.migration.impl;

import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlprSpeedRulesMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void mapsStreamIdsToAnalyticsAndDefaultsToZero() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        StreamToAnalyticsResolver resolver = mock(StreamToAnalyticsResolver.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("name", "rule");
        row.put("speed_limit", 90);
        row.put("speed_unit", "kmh");
        row.put("distance", 100);
        row.put("distance_unit", "m");
        row.put("min_speed", 10);
        row.put("min_speed_unit", "kmh");
        row.put("max_duration", 20);
        row.put("events_holder", "h");
        row.put("client_id", 4);
        row.put("stream_id1", 5);
        row.put("stream_id2", 6);

        when(source.queryForList("SELECT * FROM alpr_speed_rules")).thenReturn(List.of(row));
        when(resolver.getFirstByPlugin(5, "AlprAnalyticsModule")).thenReturn(Optional.of(500));
        when(resolver.getFirstByPlugin(6, "AlprAnalyticsModule")).thenReturn(Optional.empty());

        new AlprSpeedRulesMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO alpr_speed_rules (id, name, speed_limit, speed_unit, distance, distance_unit, min_speed, min_speed_unit, max_duration, events_holder, client_id, analytics_id1, analytics_id2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                any()
        );

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals(500, transformed.get("analytics_id1"));
        assertEquals(0, transformed.get("analytics_id2"));
    }
}
