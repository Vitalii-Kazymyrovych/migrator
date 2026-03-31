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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RolesMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void insertsDescriptionAsNull() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 5);
        row.put("role_name", "admin");
        row.put("permissions", "{}");
        row.put("client_id", 77);
        when(source.queryForList("SELECT id, role_name, permissions, client_id FROM roles")).thenReturn(List.of(row));

        new RolesMigrator(source, destination).migrate();

        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO roles (id, role_name, permissions, client_id, description) VALUES (?, ?, ?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                setterCaptor.capture());

        MigratorTestSupport.assertSetterValues(setterCaptor.getValue(), row, 5, "admin", "{}", 77, null);
    }
}
