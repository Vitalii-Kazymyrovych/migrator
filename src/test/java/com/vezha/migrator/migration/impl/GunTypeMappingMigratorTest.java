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

class GunTypeMappingMigratorTest {

    @Test
    void migratesWithDirectCopy() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("name", "gun");
        when(source.queryForList("SELECT * FROM gun_type_mapping")).thenReturn(List.of(row));

        new GunTypeMappingMigrator(source, destination).migrate();

        verify(destination).batchUpdate(
                eq("INSERT INTO gun_type_mapping (id, name) VALUES (?, ?)"),
                eq(List.of(row)),
                eq(500),
                any());
    }
}
