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

class SmokeFireNotificationsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void addsLatLongDefaults() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 4);
        row.put("stream_id", 10);
        row.put("va_id", 11);
        when(source.queryForList("SELECT * FROM smoke_fire_notifications")).thenReturn(List.of(row));

        new SmokeFireNotificationsMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(any(String.class), rowsCaptor.capture(), eq(500), any());
        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals(false, transformed.containsKey("stream_id"));
        assertEquals(null, transformed.get("latitude"));
    }
}
