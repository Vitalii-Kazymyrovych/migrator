package com.vezha.migrator.migration.impl;

import com.vezha.migrator.util.IdToUuidResolver;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyticsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void transformsStreamUuidGroupIdBitAndGeneratedUuid() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        IdToUuidResolver resolver = mock(IdToUuidResolver.class);

        Map<String, Object> streamRow = new LinkedHashMap<>();
        streamRow.put("id", 4);
        streamRow.put("parent_id", 55);
        when(source.queryForList("SELECT id, parent_id FROM streams")).thenReturn(List.of(streamRow));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("type", "analytics-type");
        row.put("plugin_name", "plugin");
        row.put("name", "an1");
        row.put("created_at", Instant.parse("2026-01-01T00:00:00Z"));
        row.put("status", "enabled");
        row.put("client_id", 7);
        row.put("stream", "main");
        row.put("module", "module");
        row.put("last_gpu_id", 2);
        row.put("desired_server_id", 3);
        row.put("disable_balancing", 1);
        row.put("start_signature", "sig");
        row.put("allowed_server_ids", "[1,2]");
        row.put("restrictions", "{}");
        row.put("events_holder", "holder");
        row.put("start_at", Instant.parse("2026-01-02T00:00:00Z"));
        row.put("stream_id", 4);
        row.put("topic", "drop-me");
        when(source.queryForList("SELECT * FROM analytics")).thenReturn(List.of(row));

        UUID streamUuid = UUID.fromString("7b4be005-f725-4ca8-94a6-17e4ae8f66bc");
        when(resolver.getUuid(4)).thenReturn(Optional.of(streamUuid));

        new AnalyticsMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO analytics (id, type, plugin_name, name, created_at, status, client_id, stream, module, last_gpu_id, desired_server_id, disable_balancing, start_signature, allowed_server_ids, restrictions, events_holder, start_at, stream_uuid, uuid, group_id) OVERRIDING SYSTEM VALUE VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                setterCaptor.capture()
        );

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals(streamUuid, transformed.get("stream_uuid"));
        assertEquals(55, transformed.get("group_id"));
        assertEquals(true, transformed.get("disable_balancing"));
        assertNull(transformed.get("topic"));
        assertNotNull(transformed.get("uuid"));

        MigratorTestSupport.assertSetterValues(
                setterCaptor.getValue(),
                transformed,
                1,
                "analytics-type",
                "plugin",
                "an1",
                row.get("created_at"),
                "enabled",
                7,
                "main",
                "module",
                2,
                3,
                true,
                "sig",
                "[1,2]",
                "{}",
                "holder",
                row.get("start_at"),
                streamUuid,
                transformed.get("uuid"),
                55
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void defaultsToNullStreamUuidAndGroupZeroWhenMissingStream() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        IdToUuidResolver resolver = mock(IdToUuidResolver.class);

        when(source.queryForList("SELECT id, parent_id FROM streams")).thenReturn(List.of());

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 2);
        row.put("stream_id", 999);
        when(source.queryForList("SELECT * FROM analytics")).thenReturn(List.of(row));
        when(resolver.getUuid(999)).thenReturn(Optional.empty());

        new AnalyticsMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(
                org.mockito.ArgumentCaptor.forClass(String.class).capture(),
                rowsCaptor.capture(),
                org.mockito.ArgumentCaptor.forClass(Integer.class).capture(),
                org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class).capture()
        );

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertNull(transformed.get("stream_uuid"));
        assertEquals(0, transformed.get("group_id"));
        verify(resolver).getUuid(anyInt());
    }
}
