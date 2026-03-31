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

class AlprListItemsMigratorTest {

    @Test
    void migratesWithDirectCopy() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 11);
        row.put("list_id", 9);
        row.put("plate", "AA1111");
        when(source.queryForList("SELECT * FROM alpr_list_items")).thenReturn(List.of(row));

        new AlprListItemsMigrator(source, destination).migrate();

        verify(destination).batchUpdate(
                eq("INSERT INTO alpr_list_items (id, list_id, plate) VALUES (?, ?, ?)"),
                eq(List.of(row)),
                eq(500),
                any());
    }
}
