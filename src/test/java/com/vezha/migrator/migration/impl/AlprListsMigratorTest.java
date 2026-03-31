package com.vezha.migrator.migration.impl;

import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlprListsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void mapsStreamsBooleanDefaultsAndPopupFlag() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        StreamToAnalyticsResolver resolver = mock(StreamToAnalyticsResolver.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1);
        row.put("name", "VIP");
        row.put("streams", "[10,11]");
        row.put("send_internal_notifications", 1);
        row.put("enabled", 0);
        row.put("list_permissions", null);
        when(source.queryForList("SELECT * FROM alpr_lists")).thenReturn(List.of(row));
        when(resolver.getAll(10)).thenReturn(List.of(100, 101));
        when(resolver.getAll(11)).thenReturn(List.of(101, 102));

        new AlprListsMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        var setterCaptor = org.mockito.ArgumentCaptor.forClass(org.springframework.jdbc.core.ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO alpr_lists (id, name, send_internal_notifications, enabled, list_permissions, analytics_ids, show_popup_for_internal_notifications) VALUES (?, ?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                setterCaptor.capture()
        );

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals("[100,101,102]", transformed.get("analytics_ids"));
        assertEquals(true, transformed.get("send_internal_notifications"));
        assertEquals(false, transformed.get("enabled"));
        assertEquals("", transformed.get("list_permissions"));
        assertEquals(false, transformed.get("show_popup_for_internal_notifications"));

        MigratorTestSupport.assertSetterValues(
                setterCaptor.getValue(),
                transformed,
                1,
                "VIP",
                true,
                false,
                "",
                "[100,101,102]",
                false
        );
    }
}
