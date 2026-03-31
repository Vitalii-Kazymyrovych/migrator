package com.vezha.migrator.migration.impl;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void castsUuidStringToUuidBeforeInsert() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);

        String uuid = "51f0f7a7-6db7-443a-bef6-8897f9926af9";
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 10);
        row.put("uuid", uuid);
        when(source.queryForList("SELECT * FROM streams")).thenReturn(List.of(row));

        new StreamsMigrator(source, destination).migrate();

        var sqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        var batchSizeCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);

        verify(destination).batchUpdate(sqlCaptor.capture(), rowsCaptor.capture(), batchSizeCaptor.capture(), setterCaptor.capture());

        org.junit.jupiter.api.Assertions.assertEquals("INSERT INTO streams (id, uuid) VALUES (?, ?)", sqlCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals(500, batchSizeCaptor.getValue());

        Map<String, Object> insertedRow = (Map<String, Object>) rowsCaptor.getValue().get(0);
        org.junit.jupiter.api.Assertions.assertEquals(UUID.fromString(uuid), insertedRow.get("uuid"));

        MigratorTestSupport.assertSetterValues(setterCaptor.getValue(), insertedRow, 10, UUID.fromString(uuid));
    }
}
