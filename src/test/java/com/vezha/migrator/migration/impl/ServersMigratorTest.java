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

class ServersMigratorTest {

    @Test
    void appliesDefaultColumnsForNewSchema() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 4);
        row.put("name", "server-a");
        when(source.queryForList("SELECT id, name FROM servers")).thenReturn(List.of(row));

        new ServersMigrator(source, destination).migrate();

        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO servers (id, name, is_external_address_enabled, address, port) VALUES (?, ?, ?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                setterCaptor.capture());

        MigratorTestSupport.assertSetterValues(setterCaptor.getValue(), row, 4, "server-a", false, null, null);
    }
}
