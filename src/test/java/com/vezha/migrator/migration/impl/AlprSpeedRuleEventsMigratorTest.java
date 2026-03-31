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

class AlprSpeedRuleEventsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void renamesSpeedLimitToSpeedValueAndDropsDetectionIds() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 9);
        row.put("rule_id", 7);
        row.put("speed_limit", 65);
        row.put("speed_unit", "mph");
        row.put("plate_number", "lost");
        when(source.queryForList("SELECT * FROM alpr_speed_rule_events")).thenReturn(List.of(row));

        new AlprSpeedRuleEventsMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO alpr_speed_rule_events (id, rule_id, speed_value, speed_unit, detection1_id, detection2_id) VALUES (?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                any()
        );

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals(65, transformed.get("speed_value"));
        assertEquals(null, transformed.get("detection1_id"));
        assertEquals(null, transformed.get("detection2_id"));
    }
}
