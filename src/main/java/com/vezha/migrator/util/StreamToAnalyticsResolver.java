package com.vezha.migrator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StreamToAnalyticsResolver {

    private static final Logger log = LoggerFactory.getLogger(StreamToAnalyticsResolver.class);

    private final Map<Integer, List<AnalyticsEntry>> streamIdToAnalytics = new HashMap<>();

    public void load(JdbcTemplate jdbcTemplate) {
        streamIdToAnalytics.clear();
        jdbcTemplate.query("SELECT stream_id, id, plugin_name FROM analytics ORDER BY id ASC", rs -> {
            int streamId = rs.getInt("stream_id");
            streamIdToAnalytics
                    .computeIfAbsent(streamId, ignored -> new ArrayList<>())
                    .add(new AnalyticsEntry(rs.getInt("id"), rs.getString("plugin_name")));
        });
    }

    public Optional<Integer> getFirstByPlugin(int streamId, String pluginName) {
        return streamIdToAnalytics.getOrDefault(streamId, List.of()).stream()
                .filter(entry -> pluginName.equals(entry.pluginName()))
                .map(AnalyticsEntry::id)
                .findFirst()
                .or(() -> {
                    log.warn("No analytics_id found for stream_id={} and plugin_name={}", streamId, pluginName);
                    return Optional.empty();
                });
    }

    public List<Integer> getAll(int streamId) {
        return streamIdToAnalytics.getOrDefault(streamId, List.of())
                .stream()
                .map(AnalyticsEntry::id)
                .toList();
    }

    public record AnalyticsEntry(int id, String pluginName) {
    }
}
