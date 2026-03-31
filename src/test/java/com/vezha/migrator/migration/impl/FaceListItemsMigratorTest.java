package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FaceListItemsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void copiesOriginalColumnsAndAppendsExpirationDefaults() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 7);
        row.put("name", "John");
        row.put("comment", "VIP");
        row.put("status", "active");
        row.put("created_at", "2025-01-01");
        row.put("created_by", "admin");
        row.put("closed_at", null);
        row.put("list_id", 1);
        row.put("expiration_settings", "{}");
        row.put("client_id", 2);

        when(source.queryForList("SELECT * FROM face_list_items")).thenReturn(List.of(row));

        new FaceListItemsMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        var setterCaptor = org.mockito.ArgumentCaptor.forClass(org.springframework.jdbc.core.ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO face_list_items (id, name, comment, status, created_at, created_by, closed_at, list_id, expiration_settings, client_id, expiration_settings_enabled, expiration_settings_action, expiration_settings_list_id, expiration_settings_date, expiration_settings_events_holder) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                setterCaptor.capture()
        );

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        MigratorTestSupport.assertSetterValues(
                setterCaptor.getValue(),
                transformed,
                7,
                "John",
                "VIP",
                "active",
                "2025-01-01",
                "admin",
                null,
                1,
                "{}",
                2,
                false,
                "none",
                null,
                null,
                null
        );
    }
}
