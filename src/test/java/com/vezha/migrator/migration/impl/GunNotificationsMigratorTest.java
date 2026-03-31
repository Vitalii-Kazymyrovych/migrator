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

class GunNotificationsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void dropsStreamIdAndAddsLatLong() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("stream_id", 5);
        row.put("va_id", 7);
        when(source.queryForList("SELECT * FROM gun_notifications")).thenReturn(List.of(row));

        new GunNotificationsMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(any(String.class), rowsCaptor.capture(), eq(500), any());
        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals(false, transformed.containsKey("stream_id"));
        assertEquals(null, transformed.get("latitude"));
    }
}
