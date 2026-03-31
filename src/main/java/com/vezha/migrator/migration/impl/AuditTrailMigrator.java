package com.vezha.migrator.migration.impl;

import com.vezha.migrator.migration.TableMigrator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuditTrailMigrator extends BaseMigratorSupport implements TableMigrator {

    public AuditTrailMigrator(JdbcTemplate sourceJdbcTemplate, JdbcTemplate destinationJdbcTemplate) {
        super(sourceJdbcTemplate, destinationJdbcTemplate);
    }

    @Override
    public String tableName() {
        return "audit_trail";
    }

    @Override
    public void migrate() {
        List<Map<String, Object>> rows = sourceJdbcTemplate.queryForList(
                "SELECT id, created_at, session_id, user_id, user_ip, source_id, message, client_id, event_category, event_action FROM audit_trail"
        );
        if (rows.isEmpty()) {
            return;
        }

        destinationJdbcTemplate.batchUpdate(
                "INSERT INTO audit_trail (id, created_at, session_id, user_id, user_ip, source_id, message, client_id, event_category_id, event_action_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                rows,
                500,
                (ps, row) -> setValues(
                        ps,
                        row.get("id"),
                        row.get("created_at"),
                        row.get("session_id"),
                        row.get("user_id"),
                        row.get("user_ip"),
                        row.get("source_id"),
                        row.get("message"),
                        row.get("client_id"),
                        deterministicUuid("event_category", row.get("event_category")),
                        deterministicUuid("event_action", row.get("event_action"))
                )
        );
    }

    private static UUID deterministicUuid(String prefix, Object value) {
        return UUID.nameUUIDFromBytes((prefix + ":" + value).getBytes(StandardCharsets.UTF_8));
    }
}
