package com.vezha.migrator.util;

import java.util.HashMap;
import java.util.Map;

public class StreamToAnalyticsGroupResolver {

    private Map<String, Integer> groupMap = new HashMap<>();

    public void register(int streamGroupId, String pluginName, int analyticsGroupId) {
        groupMap.put(key(streamGroupId, pluginName), analyticsGroupId);
    }

    public void clear() {
        groupMap.clear();
    }

    public int resolve(int streamGroupId, String pluginName) {
        return groupMap.getOrDefault(key(streamGroupId, pluginName), 0);
    }

    private String key(int groupId, String pluginName) {
        return groupId + ":" + pluginName;
    }
}