package com.vezha.migrator.migration.impl;

import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlprListEventsMigratorTest {

    @SuppressWarnings("unchecked")
    @Test
    void skipsMissingJoinedPlateAndMapsDetectionFields() {
        JdbcTemplate source = mock(JdbcTemplate.class);
        JdbcTemplate destination = mock(JdbcTemplate.class);
        StreamToAnalyticsResolver resolver = mock(StreamToAnalyticsResolver.class);

        Map<String, Object> missingPlate = new LinkedHashMap<>();
        missingPlate.put("id", 1);
        missingPlate.put("plate_id", 501);
        missingPlate.put("plate_exists", null);

        Map<String, Object> joined = new LinkedHashMap<>();
        joined.put("id", 2);
        joined.put("plate_id", 21);
        joined.put("list_id", 8);
        joined.put("list_item_id", 9);
        joined.put("list_item_name", "item");
        joined.put("status", "new");
        joined.put("accepted_by", null);
        joined.put("created_at", "2026-01-01");
        joined.put("plate_exists", 21);
        joined.put("plate_number", "AA1");
        joined.put("arabic_number", "");
        joined.put("adr", null);
        joined.put("box", "[]");
        joined.put("plate_image", "p");
        joined.put("frame_image", "f");
        joined.put("make_model_id", 1);
        joined.put("vehicle_type", "car");
        joined.put("color_id", 2);
        joined.put("direction", "N");
        joined.put("country", "US");
        joined.put("pattern", "pat");
        joined.put("client_id", 3);
        joined.put("stream_id", 44);
        joined.put("va_id", 88);

        when(source.queryForList("SELECT n.id, n.plate_id, n.list_id, n.list_item_id, n.list_item_name, n.status, n.accepted_by, n.created_at, p.id AS plate_exists, p.plate_number, p.arabic_number, p.adr, p.box, p.plate_image, p.frame_image, p.make_model_id, p.vehicle_type, p.color_id, p.direction, p.country, p.pattern, p.client_id, p.stream_id, p.va_id FROM alpr_notifications n LEFT JOIN alpr_plates p ON p.id = n.plate_id"))
                .thenReturn(List.of(missingPlate, joined));
        when(resolver.getFirstByPlugin(44, "AlprAnalyticsModule")).thenReturn(Optional.of(333));

        new AlprListEventsMigrator(source, destination, resolver).migrate();

        var rowsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(destination).batchUpdate(
                eq("INSERT INTO alpr_list_events (id, detection_id, list_id, list_item_id, list_item_name, status, accepted_by, created_at, plate_number, arabic_number, adr, box, plate_image, frame_image, make_model_id, vehicle_type, color_id, direction, country, pattern, client_id, analytics_id, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
                rowsCaptor.capture(),
                eq(500),
                any()
        );

        List<Map<String, Object>> transformedRows = rowsCaptor.getValue();
        assertEquals(1, transformedRows.size());
        assertEquals(21, transformedRows.get(0).get("detection_id"));
        assertEquals(333, transformedRows.get(0).get("analytics_id"));
        assertEquals(null, transformedRows.get(0).get("latitude"));
    }
}
