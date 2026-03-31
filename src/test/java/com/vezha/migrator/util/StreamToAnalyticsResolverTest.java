package com.vezha.migrator.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreamToAnalyticsResolverTest {

    @Test
    void getAllReturnsFullListForStream() throws Exception {
        StreamToAnalyticsResolver resolver = loadResolverWithRows();
        assertEquals(List.of(100, 101, 103), resolver.getAll(1));
    }

    @Test
    void getAllReturnsEmptyForUnknownStream() throws Exception {
        StreamToAnalyticsResolver resolver = loadResolverWithRows();
        assertTrue(resolver.getAll(42).isEmpty());
    }

    @Test
    void getFirstByPluginReturnsLowestMatchingId() throws Exception {
        StreamToAnalyticsResolver resolver = loadResolverWithRows();
        assertEquals(Optional.of(101), resolver.getFirstByPlugin(1, "AlprAnalyticsModule"));
    }

    @Test
    void getFirstByPluginReturnsEmptyWhenPluginMissingAndLogsWarn() throws Exception {
        StreamToAnalyticsResolver resolver = loadResolverWithRows();

        Logger logger = (Logger) LoggerFactory.getLogger(StreamToAnalyticsResolver.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertTrue(resolver.getFirstByPlugin(1, "MissingPlugin").isEmpty());
            assertTrue(appender.list.stream().anyMatch(event ->
                    event.getLevel() == Level.WARN
                            && event.getFormattedMessage().contains("stream_id=1")
                            && event.getFormattedMessage().contains("plugin_name=MissingPlugin")));
        } finally {
            logger.detachAppender(appender);
        }
    }

    private StreamToAnalyticsResolver loadResolverWithRows() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        doAnswer(invocation -> {
            var callback = invocation.getArgument(1, org.springframework.jdbc.core.RowCallbackHandler.class);

            ResultSet row1 = mock(ResultSet.class);
            when(row1.getInt("stream_id")).thenReturn(1);
            when(row1.getInt("id")).thenReturn(100);
            when(row1.getString("plugin_name")).thenReturn("TrafficAnalyticsModule");
            callback.processRow(row1);

            ResultSet row2 = mock(ResultSet.class);
            when(row2.getInt("stream_id")).thenReturn(1);
            when(row2.getInt("id")).thenReturn(101);
            when(row2.getString("plugin_name")).thenReturn("AlprAnalyticsModule");
            callback.processRow(row2);

            ResultSet row3 = mock(ResultSet.class);
            when(row3.getInt("stream_id")).thenReturn(2);
            when(row3.getInt("id")).thenReturn(102);
            when(row3.getString("plugin_name")).thenReturn("FaceAnalyticsModule");
            callback.processRow(row3);

            ResultSet row4 = mock(ResultSet.class);
            when(row4.getInt("stream_id")).thenReturn(1);
            when(row4.getInt("id")).thenReturn(103);
            when(row4.getString("plugin_name")).thenReturn("AlprAnalyticsModule");
            callback.processRow(row4);
            return null;
        }).when(jdbcTemplate).query(eq("SELECT stream_id, id, plugin_name FROM analytics ORDER BY id ASC"), any(org.springframework.jdbc.core.RowCallbackHandler.class));

        StreamToAnalyticsResolver resolver = new StreamToAnalyticsResolver();
        resolver.load(jdbcTemplate);
        return resolver;
    }
}
