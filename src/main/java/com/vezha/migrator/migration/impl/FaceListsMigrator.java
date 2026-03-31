package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FaceListsMigrator extends BaseMigratorSupport implements TableMigrator {

    private final StreamToAnalyticsResolver streamToAnalyticsResolver;

    public FaceListsMigrator(JdbcTemplate sourceJdbcTemplate,
                             JdbcTemplate destinationJdbcTemplate,
                             StreamToAnalyticsResolver streamToAnalyticsResolver) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
        this.streamToAnalyticsResolver = streamToAnalyticsResolver;
    }

    @Override
    public String tableName() {
        return "face_lists";
    }

    @Override
    public void migrate() {
        directCopy("face_lists", "face_lists", row -> {
            row.put("analytics_ids", resolveAnalyticsIdsJson(row.remove("streams")));
            row.put("send_internal_notifications", toBoolean(row.get("send_internal_notifications")));
            row.remove("enabled");
            row.put("list_permissions", row.get("list_permissions") == null ? "" : row.get("list_permissions"));
            row.put("show_popup_for_internal_notifications", false);
            return row;
        });
    }

    private String resolveAnalyticsIdsJson(Object streamsRaw) {
        if (streamsRaw == null) {
            return null;
        }

        List<Integer> streamIds = parseJsonIntArray(streamsRaw.toString());
        if (streamIds.isEmpty()) {
            return null;
        }

        Set<Integer> analyticsIds = new LinkedHashSet<>();
        for (Integer streamId : streamIds) {
            analyticsIds.addAll(streamToAnalyticsResolver.getAll(streamId));
        }

        if (analyticsIds.isEmpty()) {
            return null;
        }
        return "[" + analyticsIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("") + "]";
    }

    private static List<Integer> parseJsonIntArray(String value) {
        String trimmed = value.trim();
        if (trimmed.length() < 2 || "[]".equals(trimmed)) {
            return List.of();
        }

        String content = trimmed;
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            content = trimmed.substring(1, trimmed.length() - 1);
        }

        String[] tokens = content.split(",");
        List<Integer> result = new ArrayList<>();
        for (String token : tokens) {
            String cleaned = token.trim();
            if (!cleaned.isEmpty()) {
                result.add(Integer.parseInt(cleaned));
            }
        }
        return result;
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
