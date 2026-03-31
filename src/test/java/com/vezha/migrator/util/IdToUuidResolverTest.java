package com.vezha.migrator.util;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class IdToUuidResolverTest {

    @Test
    void knownIdReturnsUuid() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        UUID expected = UUID.randomUUID();

        doAnswer(invocation -> {
            var rch = invocation.getArgument(1, org.springframework.jdbc.core.RowCallbackHandler.class);
            var rs = mock(java.sql.ResultSet.class);
            org.mockito.Mockito.when(rs.getInt("id")).thenReturn(10);
            org.mockito.Mockito.when(rs.getString("uuid")).thenReturn(expected.toString());
            rch.processRow(rs);
            return null;
        }).when(jdbcTemplate).query(eq("SELECT id, uuid FROM streams"), org.mockito.ArgumentMatchers.any(org.springframework.jdbc.core.RowCallbackHandler.class));

        IdToUuidResolver resolver = new IdToUuidResolver();
        resolver.load(jdbcTemplate);

        assertEquals(Optional.of(expected), resolver.getUuid(10));
    }

    @Test
    void unknownIdReturnsEmptyOptional() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        doAnswer(invocation -> null)
                .when(jdbcTemplate)
                .query(eq("SELECT id, uuid FROM streams"), org.mockito.ArgumentMatchers.any(org.springframework.jdbc.core.RowCallbackHandler.class));

        IdToUuidResolver resolver = new IdToUuidResolver();
        resolver.load(jdbcTemplate);

        assertTrue(resolver.getUuid(404).isEmpty());
    }
}
