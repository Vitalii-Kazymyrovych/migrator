package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void wrapsRoleIdIntoJsonArrayAndDropsRoleIdColumn() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 5);
        row.put("username", "user1");
        row.put("role_id", 3);

        when(source.queryForList("SELECT * FROM users")).thenReturn(List.of(row));

        new UsersMigrator(source, destination).migrate();

        var sqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(sqlCaptor.capture(), rowsCaptor.capture(), org.mockito.ArgumentCaptor.forClass(Integer.class).capture(), setterCaptor.capture());

        assertEquals("INSERT INTO users (id, username, role_ids) VALUES (?, ?, ?)", sqlCaptor.getValue());
        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertFalse(transformed.containsKey("role_id"));
        assertEquals("[3]", transformed.get("role_ids"));

        MigratorTestSupport.assertSetterValues(setterCaptor.getValue(), transformed, 5, "user1", "[3]");
    }

    @SuppressWarnings("unchecked")
    @Test
    void usesEmptyJsonArrayWhenRoleIdIsZero() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 7);
        row.put("role_id", 0);
        when(source.queryForList("SELECT * FROM users")).thenReturn(List.of(row));

        new UsersMigrator(source, destination).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(org.mockito.ArgumentCaptor.forClass(String.class).capture(), rowsCaptor.capture(), org.mockito.ArgumentCaptor.forClass(Integer.class).capture(), org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class).capture());
        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals("[]", transformed.get("role_ids"));
    }
}
