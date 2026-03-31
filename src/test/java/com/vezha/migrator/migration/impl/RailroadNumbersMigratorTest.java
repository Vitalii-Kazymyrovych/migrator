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

class RailroadNumbersMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void skipsRowsWithoutMappingAndAddsDefaults() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        StreamToAnalyticsResolver resolver = mock(StreamToAnalyticsResolver.class);

        Map<String, Object> skipped = new LinkedHashMap<>();
        skipped.put("id", 1);
        skipped.put("stream_id", 10);

        Map<String, Object> inserted = new LinkedHashMap<>();
        inserted.put("id", 2);
        inserted.put("number", "ABC");
        inserted.put("box", "[]");
        inserted.put("number_image", "n.jpg");
        inserted.put("frame_image", "f.jpg");
        inserted.put("created_at", "2026-01-01");
        inserted.put("direction", "N");
        inserted.put("client_id", 3);
        inserted.put("iso_code", "ISO");
        inserted.put("stream_id", 11);

        when(source.queryForList("SELECT * FROM railroad_numbers")).thenReturn(List.of(skipped, inserted));
        when(resolver.getFirstByPlugin(10, "RailroadsAnalyticsModule")).thenReturn(Optional.empty());
        when(resolver.getFirstByPlugin(11, "RailroadsAnalyticsModule")).thenReturn(Optional.of(700));

        new RailroadNumbersMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(any(String.class), rowsCaptor.capture(), eq(500), any());
        List<Map<String, Object>> transformed = rowsCaptor.getValue();

        assertEquals(1, transformed.size());
        assertEquals(700, transformed.get(0).get("analytics_id"));
        assertEquals(null, transformed.get(0).get("average_character_height"));
    }
}
