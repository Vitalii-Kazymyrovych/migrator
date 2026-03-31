package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventManagerMigratorTest {

    @Test
    void mapsOldIdToUuidAndOmitsSerialIdInsert() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", "legacy-id");
        row.put("title", "title");
        row.put("description", "desc");
        row.put("created_at", Instant.parse("2026-01-01T00:00:00Z"));
        row.put("nodes", "[]");
        row.put("client_id", 99);
        when(source.queryForList("SELECT id, title, description, created_at, nodes, client_id FROM event_manager"))
                .thenReturn(List.of(row));

        new EventManagerMigrator(source, destination).migrate();

        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO event_manager (uuid, title, description, created_at, nodes, client_id) VALUES (?, ?, ?, ?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                setterCaptor.capture());

        MigratorTestSupport.assertSetterValues(setterCaptor.getValue(), row, "legacy-id", "title", "desc", row.get("created_at"), "[]", 99);
    }
}
