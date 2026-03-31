package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenderAgeStatMigratorTest {

    @Test
    void directCopyIsUsed() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        when(source.queryForList("SELECT * FROM gender_age_stat")).thenReturn(java.util.List.of(Map.of("id", 1, "date", "2026-01-01")));

        new GenderAgeStatMigrator(source, destination).migrate();

        verify(destination).batchUpdate(any(String.class), any(java.util.List.class), eq(500), any());
    }
}
