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

class SettingsMigratorTest {

    @Test
    void renamesVariableNameAndValueColumns() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Variable_name", "k");
        row.put("Value", "v");
        when(source.queryForList("SELECT Variable_name, Value FROM settings")).thenReturn(List.of(row));

        new SettingsMigrator(source, destination).migrate();

        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO system_settings (variable_name, value) VALUES (?, ?)"),
                eq(List.of(row)),
                eq(500),
                setterCaptor.capture());

        MigratorTestSupport.assertSetterValues(setterCaptor.getValue(), row, "k", "v");
    }
}
