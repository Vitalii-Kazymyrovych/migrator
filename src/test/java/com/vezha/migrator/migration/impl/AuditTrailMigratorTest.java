package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditTrailMigratorTest {

    @Test
    void mapsEventCategoryAndActionToDeterministicUuids() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 100);
        row.put("created_at", Instant.parse("2026-01-01T00:00:00Z"));
        row.put("session_id", "s1");
        row.put("user_id", 5);
        row.put("user_ip", "127.0.0.1");
        row.put("source_id", 9);
        row.put("message", "updated");
        row.put("client_id", 2);
        row.put("event_category", 11);
        row.put("event_action", 19);
        when(source.queryForList("SELECT id, created_at, session_id, user_id, user_ip, source_id, message, client_id, event_category, event_action FROM audit_trail"))
                .thenReturn(List.of(row));

        new AuditTrailMigrator(source, destination).migrate();

        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO audit_trail (id, created_at, session_id, user_id, user_ip, source_id, message, client_id, event_category_id, event_action_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                setterCaptor.capture()
        );

        UUID expectedCategory = UUID.nameUUIDFromBytes("event_category:11".getBytes(StandardCharsets.UTF_8));
        UUID expectedAction = UUID.nameUUIDFromBytes("event_action:19".getBytes(StandardCharsets.UTF_8));

        MigratorTestSupport.assertSetterValues(
                setterCaptor.getValue(),
                row,
                100,
                row.get("created_at"),
                "s1",
                5,
                "127.0.0.1",
                9,
                "updated",
                2,
                expectedCategory,
                expectedAction
        );
    }
}
