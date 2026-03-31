package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZoneExitNotificationsObjectTypeMigratorTest {

    @Test
    void migratesWithDirectCopy() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("name", "car");
        when(source.queryForList("SELECT * FROM zone_exit_notifications_object_type")).thenReturn(List.of(row));

        new ZoneExitNotificationsObjectTypeMigrator(source, destination).migrate();

        verify(destination).batchUpdate(
                eq("INSERT INTO zone_exit_notifications_object_type (id, name) VALUES (?, ?)"),
                eq(List.of(row)),
                eq(500),
                any());
    }
}
