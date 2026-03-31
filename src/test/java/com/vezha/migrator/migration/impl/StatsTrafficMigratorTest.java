package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatsTrafficMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void mergesBothTablesAndDoesNotInsertIds() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> hourly = new LinkedHashMap<>();
        hourly.put("id", 1);
        hourly.put("va_id", 10);
        hourly.put("line", "L1");
        hourly.put("type", "car");
        hourly.put("count", 1);
        hourly.put("direction", "N");
        hourly.put("created_at", "2026-01-01");
        hourly.put("client_id", 2);
        hourly.put("present", true);

        Map<String, Object> minutely = new LinkedHashMap<>(hourly);
        minutely.put("id", 2);

        when(source.queryForList("SELECT * FROM stats_traffic_hourly")).thenReturn(List.of(hourly));
        when(source.queryForList("SELECT * FROM stats_traffic_minutely")).thenReturn(List.of(minutely));

        new StatsTrafficMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(any(String.class), rowsCaptor.capture(), eq(500), any());

        List<Map<String, Object>> transformed = rowsCaptor.getValue();
        assertFalse(transformed.get(0).containsKey("id"));
        assertFalse(transformed.get(1).containsKey("id"));
    }
}
