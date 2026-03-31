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

class AlprHourlyStatsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void skipsRowsWithoutResolvedAnalyticsId() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        StreamToAnalyticsResolver resolver = mock(StreamToAnalyticsResolver.class);

        Map<String, Object> skipped = new LinkedHashMap<>();
        skipped.put("id", 1);
        skipped.put("stream_id", 2);

        Map<String, Object> inserted = new LinkedHashMap<>();
        inserted.put("id", 2);
        inserted.put("total", 10);
        inserted.put("numbers", "[]");
        inserted.put("make_models", "[]");
        inserted.put("created_at", "2026-01-01");
        inserted.put("client_id", 9);
        inserted.put("stream_id", 3);

        when(source.queryForList("SELECT * FROM alpr_stats_hourly")).thenReturn(List.of(skipped, inserted));
        when(resolver.getFirstByPlugin(2, "AlprAnalyticsModule")).thenReturn(Optional.empty());
        when(resolver.getFirstByPlugin(3, "AlprAnalyticsModule")).thenReturn(Optional.of(700));

        new AlprHourlyStatsMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO alpr_hourly_statistics (id, total, numbers, make_models, created_at, client_id, analytics_id) VALUES (?, ?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                any()
        );

        List<Map<String, Object>> transformed = rowsCaptor.getValue();
        assertEquals(1, transformed.size());
        assertEquals(2, transformed.get(0).get("id"));
        assertEquals(700, transformed.get(0).get("analytics_id"));
    }
}
