package com.vezha.migrator.migration.impl;

import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlprDetectionsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void usesVaIdFallbackAndAddsLatLongDefaults() throws SQLException {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        StreamToAnalyticsResolver resolver = mock(StreamToAnalyticsResolver.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 5);
        row.put("plate_number", "AA1234");
        row.put("arabic_number", "");
        row.put("adr", null);
        row.put("box", "[1,2,3,4]");
        row.put("plate_image", "p.jpg");
        row.put("frame_image", "f.jpg");
        row.put("make_model_id", 4);
        row.put("vehicle_type", "car");
        row.put("created_at", "2026-01-01 00:00:00");
        row.put("color_id", 3);
        row.put("direction", "N");
        row.put("country", "US");
        row.put("pattern", "us");
        row.put("client_id", 2);
        row.put("stream_id", 20);
        row.put("va_id", 77);
        row.put("list_items", "[1]");
        when(source.queryForList("SELECT * FROM alpr_plates")).thenReturn(List.of(row));
        when(resolver.getFirstByPlugin(20, "AlprAnalyticsModule")).thenReturn(Optional.empty());

        new AlprDetectionsMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        var setterCaptor = org.mockito.ArgumentCaptor.forClass(ParameterizedPreparedStatementSetter.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO alpr_detections (id, plate_number, arabic_number, adr, box, plate_image, frame_image, make_model_id, vehicle_type, created_at, color_id, direction, country, pattern, client_id, analytics_id, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                setterCaptor.capture()
        );

        Map<String, Object> transformed = (Map<String, Object>) rowsCaptor.getValue().get(0);
        assertEquals(77, transformed.get("analytics_id"));
        assertEquals(0, transformed.get("latitude"));
        assertEquals(0, transformed.get("longitude"));

        MigratorTestSupport.assertSetterValues(
                setterCaptor.getValue(),
                transformed,
                5,
                "AA1234",
                "",
                null,
                "[1,2,3,4]",
                "p.jpg",
                "f.jpg",
                4,
                "car",
                "2026-01-01 00:00:00",
                3,
                "N",
                "US",
                "us",
                2,
                77,
                0,
                0
        );
    }
}
