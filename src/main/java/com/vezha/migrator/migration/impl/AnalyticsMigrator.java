package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import com.vezha.migrator.util.IdToUuidResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AnalyticsMigrator extends BaseMigratorSupport implements TableMigrator {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsMigrator.class);

    private final IdToUuidResolver idToUuidResolver;

    public AnalyticsMigrator(JdbcTemplate sourceJdbcTemplate,
                             JdbcTemplate destinationJdbcTemplate,
                             IdToUuidResolver idToUuidResolver) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
        this.idToUuidResolver = idToUuidResolver;
    }

    @Override
    public String tableName() {
        return "analytics";
    }

    @Override
    public void migrate() {
        Map<Integer, Integer> streamToGroup = loadStreamToGroupMap();
        List<Map<String, Object>> sourceRows = sourceJdbcTemplate.queryForList("SELECT * FROM analytics");
        if (sourceRows.isEmpty()) {
            return;
        }

        List<Map<String, Object>> transformedRows = sourceRows.stream().map(row -> transform(row, streamToGroup)).toList();

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO analytics (id, type, plugin_name, name, created_at, status, client_id, stream, module, last_gpu_id, desired_server_id, disable_balancing, start_signature, allowed_server_ids, restrictions, events_holder, start_at, stream_uuid, uuid, group_id) OVERRIDING SYSTEM VALUE VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                transformedRows,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("type"),
                        row.get("plugin_name"),
                        row.get("name"),
                        row.get("created_at"),
                        row.get("status"),
                        row.get("client_id"),
                        row.get("stream"),
                        row.get("module"),
                        row.get("last_gpu_id"),
                        row.get("desired_server_id"),
                        row.get("disable_balancing"),
                        row.get("start_signature"),
                        row.get("allowed_server_ids"),
                        row.get("restrictions"),
                        row.get("events_holder"),
                        row.get("start_at"),
                        row.get("stream_uuid"),
                        row.get("uuid"),
                        row.get("group_id")
                )
        );
    }

    private Map<Integer, Integer> loadStreamToGroupMap() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList("SELECT id, parent_id FROM streams");
        Map<Integer, Integer> streamToGroup = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object streamId = row.get("id");
            Object parentId = row.get("parent_id");
            if (streamId instanceof Number && parentId instanceof Number) {
                streamToGroup.put(((Number) streamId).intValue(), ((Number) parentId).intValue());
            }
        }
        return streamToGroup;
    }

    private Map<String, Object> transform(Map<String, Object> sourceRow, Map<Integer, Integer> streamToGroup) {
        Object streamIdRaw = sourceRow.get("stream_id");
        Integer streamId = streamIdRaw instanceof Number ? ((Number) streamIdRaw).intValue() : null;

        UUID streamUuid = null;
        if (streamId != null) {
            streamUuid = idToUuidResolver.getUuid(streamId).orElse(null);
            if (streamUuid == null) {
                log.warn("No stream UUID found for analytics.id={} stream_id={}", sourceRow.get("id"), streamId);
            }
        }

        Integer groupId = streamId == null ? 0 : streamToGroup.getOrDefault(streamId, 0);

        Map<String, Object> transformed = new LinkedHashMap<>();
        transformed.put("id", sourceRow.get("id"));
        transformed.put("type", sourceRow.get("type"));
        transformed.put("plugin_name", sourceRow.get("plugin_name"));
        transformed.put("name", sourceRow.get("name"));
        transformed.put("created_at", sourceRow.get("created_at"));
        transformed.put("status", sourceRow.get("status"));
        transformed.put("client_id", sourceRow.get("client_id"));
        transformed.put("stream", sourceRow.get("stream"));
        transformed.put("module", sourceRow.get("module"));
        transformed.put("last_gpu_id", sourceRow.get("last_gpu_id"));
        transformed.put("desired_server_id", sourceRow.get("desired_server_id"));
        transformed.put("disable_balancing", toBoolean(sourceRow.get("disable_balancing")));
        transformed.put("start_signature", sourceRow.get("start_signature"));
        transformed.put("allowed_server_ids", sourceRow.get("allowed_server_ids"));
        transformed.put("restrictions", sourceRow.get("restrictions"));
        transformed.put("events_holder", sourceRow.get("events_holder"));
        transformed.put("start_at", sourceRow.get("start_at"));
        transformed.put("stream_uuid", streamUuid);
        transformed.put("uuid", UUID.randomUUID());
        transformed.put("group_id", groupId);
        return transformed;
    }

    private static Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        if (value instanceof byte[] bytes && bytes.length > 0) {
            return bytes[0] != 0;
        }
        return "1".equals(value.toString()) || "true".equalsIgnoreCase(value.toString());
    }
}
