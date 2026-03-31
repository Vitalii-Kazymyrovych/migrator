package com.vezha.migrator.util;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class IdToUuidResolver {

    private final Map<Integer, UUID> streamIdToUuid = new HashMap<>();

    public void load(JdbcTemplate jdbcTemplate) {
        streamIdToUuid.clear();
        jdbcTemplate.query("SELECT id, uuid FROM streams", rs -> {
            streamIdToUuid.put(rs.getInt("id"), UUID.fromString(rs.getString("uuid")));
        });
    }

    public Optional<UUID> getUuid(int streamId) {
        return Optional.ofNullable(streamIdToUuid.get(streamId));
    }

    public int size() {
        return streamIdToUuid.size();
    }
}
