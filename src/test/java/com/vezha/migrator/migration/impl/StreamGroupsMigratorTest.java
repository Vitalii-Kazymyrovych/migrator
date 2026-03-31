package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamGroupsMigratorTest {

    @Test
    void writesToBothStreamGroupsAndAnalyticsGroupsWithPreservedIds() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 12);
        row.put("parent_id", 4);
        row.put("name", "North");
        row.put("client_id", 8);

        when(source.queryForList("SELECT id, parent_id, name, client_id FROM stream_groups")).thenReturn(List.of(row));

        new StreamGroupsMigrator(source, destination).migrate();

        var streamSetterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO stream_groups (id, parent_id, name, client_id) OVERRIDING SYSTEM VALUE VALUES (?, ?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                streamSetterCaptor.capture()
        );
        MigratorTestSupport.assertSetterValues(streamSetterCaptor.getValue(), row, 12, 4, "North", 8);

        var analyticsSetterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO analytics_groups (id, parent_id, name, client_id, plugin_name) OVERRIDING SYSTEM VALUE VALUES (?, ?, ?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                analyticsSetterCaptor.capture()
        );
        MigratorTestSupport.assertSetterValues(analyticsSetterCaptor.getValue(), row, 12, 4, "North", 8, "");
    }

    @Test
    void skipsAnalyticsGroupsWriteWhenConfigured() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 12);
        row.put("parent_id", 4);
        row.put("name", "North");
        row.put("client_id", 8);

        when(source.queryForList("SELECT id, parent_id, name, client_id FROM stream_groups")).thenReturn(List.of(row));

        StreamGroupsMigrator migrator = new StreamGroupsMigrator(source, destination);
        migrator.setMigrateAnalyticsGroups(false);
        migrator.migrate();

        verify(destination).batchUpdate(
                eq("INSERT INTO stream_groups (id, parent_id, name, client_id) OVERRIDING SYSTEM VALUE VALUES (?, ?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                org.mockito.ArgumentMatchers.any(ParameterizedPreparedStatementSetter.class)
        );
        verify(destination, never()).batchUpdate(
                eq("INSERT INTO analytics_groups (id, parent_id, name, client_id, plugin_name) OVERRIDING SYSTEM VALUE VALUES (?, ?, ?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                org.mockito.ArgumentMatchers.any(ParameterizedPreparedStatementSetter.class)
        );
    }
}
