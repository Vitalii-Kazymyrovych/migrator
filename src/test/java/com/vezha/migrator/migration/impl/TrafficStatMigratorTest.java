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

class TrafficStatMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void addsDefaultColumnsWithoutChangingStreamAndVa() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("stream_id", 11);
        row.put("va_id", 22);
        row.put("line", "[]");
        when(source.queryForList("SELECT * FROM traffic_stat")).thenReturn(List.of(row));

        new TrafficStatMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(any(String.class), rowsCaptor.capture(), eq(500), any());
        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);

        assertEquals(11, transformed.get("stream_id"));
        assertEquals(22, transformed.get("va_id"));
        assertEquals(0, transformed.get("x1"));
        assertEquals(0, transformed.get("confidence"));
        assertEquals(null, transformed.get("line_object"));
        assertEquals(null, transformed.get("latitude"));
    }
}
