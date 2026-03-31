package com.vezha.migrator.migration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.vezha.migrator.config.ConfigModel;
import com.vezha.migrator.migration.impl.AlprListEventsMigrator;
import com.vezha.migrator.migration.impl.StatsTrafficMigrator;
import com.vezha.migrator.migration.impl.StreamGroupsMigrator;
import com.vezha.migrator.util.IdToUuidResolver;
import com.vezha.migrator.util.SourceSchemaInspector;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import com.vezha.migrator.util.TargetSchemaInspector;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MigrationOrchestratorTest {

    @Test
    void runsResolversThenEnabledMigratorsThenResetsPostgresSequences() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(10L);
        when(destinationJdbc.queryForObject(anyString(), eq(Boolean.class), anyString(), anyString())).thenReturn(true);

        IdToUuidResolver idResolver = mock(IdToUuidResolver.class);
        StreamToAnalyticsResolver analyticsResolver = mock(StreamToAnalyticsResolver.class);
        SourceSchemaInspector sourceSchemaInspector = mock(SourceSchemaInspector.class);
        when(sourceSchemaInspector.tableExists(anyString())).thenReturn(true);
        TargetSchemaInspector targetSchemaInspector = mock(TargetSchemaInspector.class);
        when(targetSchemaInspector.tableExists(anyString())).thenReturn(true);

        TableMigrator clientsMigrator = migrator("clients");
        TableMigrator settingsMigrator = migrator("settings", "system_settings");
        TableMigrator eventManagerMigrator = migrator("event_manager");

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                idResolver,
                analyticsResolver,
                sourceSchemaInspector,
                targetSchemaInspector,
                List.of(clientsMigrator, settingsMigrator, eventManagerMigrator)
        );

        ConfigModel config = postgresConfig();
        orchestrator.run(config);

        verify(idResolver, times(1)).load(sourceJdbc);
        verify(analyticsResolver, times(1)).load(sourceJdbc);
        verify(sourceSchemaInspector, times(1)).load(config);
        verify(targetSchemaInspector, times(1)).load(config);

        verify(clientsMigrator, times(1)).migrate();
        verify(settingsMigrator, times(1)).migrate();
        verify(eventManagerMigrator, never()).migrate();

        verify(destinationJdbc, times(1)).execute("SELECT setval(pg_get_serial_sequence('videoanalytics.clients', 'id'), 10, true)");
        verify(destinationJdbc, times(1)).execute("SELECT setval(pg_get_serial_sequence('videoanalytics.alpr_stats_hourly', 'id'), 10, true)");
        verify(destinationJdbc, never()).execute("SELECT setval(pg_get_serial_sequence('videoanalytics.system_settings', 'id'), 10, true)");
    }

    @Test
    void skipsMigratorWhenTargetTableIsMissing() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(10L);
        when(destinationJdbc.queryForObject(anyString(), eq(Boolean.class), anyString(), anyString())).thenReturn(true);

        SourceSchemaInspector sourceSchemaInspector = mock(SourceSchemaInspector.class);
        when(sourceSchemaInspector.tableExists(anyString())).thenReturn(true);

        TargetSchemaInspector targetSchemaInspector = mock(TargetSchemaInspector.class);
        when(targetSchemaInspector.tableExists("clients")).thenReturn(false);
        when(targetSchemaInspector.tableExists("alpr_stats_hourly")).thenReturn(true);

        TableMigrator clientsMigrator = migrator("clients");

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                mock(IdToUuidResolver.class),
                mock(StreamToAnalyticsResolver.class),
                sourceSchemaInspector,
                targetSchemaInspector,
                List.of(clientsMigrator)
        );

        orchestrator.run(postgresConfigOnlyClients());

        verify(clientsMigrator, never()).migrate();
    }

    @Test
    void skipsMigratorWhenSourceTableIsMissing() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(10L);
        when(destinationJdbc.queryForObject(anyString(), eq(Boolean.class), anyString(), anyString())).thenReturn(true);

        SourceSchemaInspector sourceSchemaInspector = mock(SourceSchemaInspector.class);
        when(sourceSchemaInspector.tableExists("clients")).thenReturn(false);

        TargetSchemaInspector targetSchemaInspector = mock(TargetSchemaInspector.class);
        when(targetSchemaInspector.tableExists("clients")).thenReturn(true);

        TableMigrator clientsMigrator = migrator("clients");

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                mock(IdToUuidResolver.class),
                mock(StreamToAnalyticsResolver.class),
                sourceSchemaInspector,
                targetSchemaInspector,
                List.of(clientsMigrator)
        );

        orchestrator.run(postgresConfigOnlyClients());

        verify(clientsMigrator, never()).migrate();
    }

    @Test
    void streamGroupsSkipsAnalyticsGroupsWriteButStillMigratesWhenOnlyAnalyticsGroupsMissing() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(10L);
        when(destinationJdbc.queryForObject(anyString(), eq(Boolean.class), anyString(), anyString())).thenReturn(true);

        SourceSchemaInspector sourceSchemaInspector = mock(SourceSchemaInspector.class);
        when(sourceSchemaInspector.tableExists(anyString())).thenReturn(true);

        TargetSchemaInspector targetSchemaInspector = mock(TargetSchemaInspector.class);
        when(targetSchemaInspector.tableExists("stream_groups")).thenReturn(true);
        when(targetSchemaInspector.tableExists("analytics_groups")).thenReturn(false);

        StreamGroupsMigrator streamGroupsMigrator = mock(StreamGroupsMigrator.class);
        when(streamGroupsMigrator.tableName()).thenReturn("stream_groups");

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                mock(IdToUuidResolver.class),
                mock(StreamToAnalyticsResolver.class),
                sourceSchemaInspector,
                targetSchemaInspector,
                List.of(streamGroupsMigrator)
        );

        orchestrator.run(postgresConfigOnlyStreamGroups());

        verify(streamGroupsMigrator, times(1)).setMigrateAnalyticsGroups(false);
        verify(streamGroupsMigrator, times(1)).migrate();
    }

    @Test
    void statsTrafficMigratorRunsWhenOnlyOneSourceTableExists() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);

        SourceSchemaInspector sourceSchemaInspector = mock(SourceSchemaInspector.class);
        when(sourceSchemaInspector.tableExists("stats_traffic_hourly")).thenReturn(true);
        when(sourceSchemaInspector.tableExists("stats_traffic_minutely")).thenReturn(false);

        TargetSchemaInspector targetSchemaInspector = mock(TargetSchemaInspector.class);
        when(targetSchemaInspector.tableExists("stats_traffic_minutely")).thenReturn(true);

        StatsTrafficMigrator statsTrafficMigrator = mock(StatsTrafficMigrator.class);
        when(statsTrafficMigrator.tableName()).thenReturn("stats_traffic");
        when(statsTrafficMigrator.getTargetTable()).thenReturn("stats_traffic_minutely");

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                mock(IdToUuidResolver.class),
                mock(StreamToAnalyticsResolver.class),
                sourceSchemaInspector,
                targetSchemaInspector,
                List.of(statsTrafficMigrator)
        );

        orchestrator.run(postgresConfigOnlyStatsTraffic());

        verify(statsTrafficMigrator, times(1)).migrate();
    }

    @Test
    void alprListEventsMigratorRunsAndLogsWarnWhenAlprPlatesIsMissing() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);

        SourceSchemaInspector sourceSchemaInspector = mock(SourceSchemaInspector.class);
        when(sourceSchemaInspector.tableExists("alpr_notifications")).thenReturn(true);
        when(sourceSchemaInspector.tableExists("alpr_plates")).thenReturn(false);

        TargetSchemaInspector targetSchemaInspector = mock(TargetSchemaInspector.class);
        when(targetSchemaInspector.tableExists("alpr_list_events")).thenReturn(true);

        AlprListEventsMigrator migrator = mock(AlprListEventsMigrator.class);
        when(migrator.tableName()).thenReturn("alpr_list_events");
        when(migrator.getTargetTable()).thenReturn("alpr_list_events");

        Logger orchestratorLogger = (Logger) LoggerFactory.getLogger(MigrationOrchestrator.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        orchestratorLogger.addAppender(listAppender);

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                mock(IdToUuidResolver.class),
                mock(StreamToAnalyticsResolver.class),
                sourceSchemaInspector,
                targetSchemaInspector,
                List.of(migrator)
        );

        try {
            orchestrator.run(postgresConfigOnlyAlprListEvents());
        } finally {
            orchestratorLogger.detachAppender(listAppender);
        }

        verify(migrator, times(1)).migrate();
        assertTrue(listAppender.list.stream().anyMatch(event ->
                event.getLevel().equals(Level.WARN)
                        && event.getFormattedMessage().contains("source table alpr_plates not found in source DB")
        ));
    }

    @Test
    void resetsMysqlAutoIncrementForEligibleTables() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(7L);

        SourceSchemaInspector sourceSchemaInspector = mock(SourceSchemaInspector.class);
        TargetSchemaInspector targetSchemaInspector = mock(TargetSchemaInspector.class);

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                mock(IdToUuidResolver.class),
                mock(StreamToAnalyticsResolver.class),
                sourceSchemaInspector,
                targetSchemaInspector,
                List.of()
        );

        ConfigModel config = mysqlDestinationConfig();
        orchestrator.run(config);

        verify(destinationJdbc, times(1)).execute("ALTER TABLE clients AUTO_INCREMENT = 8");
        verify(destinationJdbc, never()).execute("ALTER TABLE event_manager AUTO_INCREMENT = 8");
        verify(destinationJdbc, never()).execute("ALTER TABLE system_settings AUTO_INCREMENT = 8");
        verify(destinationJdbc, never()).execute("ALTER TABLE stats_traffic_minutely AUTO_INCREMENT = 8");
    }

    private TableMigrator migrator(String name) {
        return migrator(name, name);
    }

    private TableMigrator migrator(String name, String targetTable) {
        TableMigrator migrator = mock(TableMigrator.class);
        when(migrator.tableName()).thenReturn(name);
        when(migrator.getTargetTable()).thenReturn(targetTable);
        when(migrator.getSourceTables()).thenReturn(List.of(name));
        return migrator;
    }

    private ConfigModel postgresConfig() {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());

        ConfigModel.TableConfig clients = new ConfigModel.TableConfig();
        clients.setEnabled(true);
        model.getMigration().getTables().put("clients", clients);

        ConfigModel.TableConfig settings = new ConfigModel.TableConfig();
        settings.setEnabled(true);
        model.getMigration().getTables().put("settings", settings);

        ConfigModel.TableConfig eventManager = new ConfigModel.TableConfig();
        eventManager.setEnabled(false);
        model.getMigration().getTables().put("event_manager", eventManager);

        ConfigModel.TableConfig alprHourly = new ConfigModel.TableConfig();
        alprHourly.setEnabled(true);
        model.getMigration().getTables().put("alpr_hourly_statistics", alprHourly);

        model.getDestination().getPostgres().setEnabled(true);
        model.getDestination().getPostgres().setSchema("videoanalytics");
        return model;
    }

    private ConfigModel postgresConfigOnlyClients() {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());

        ConfigModel.TableConfig clients = new ConfigModel.TableConfig();
        clients.setEnabled(true);
        model.getMigration().getTables().put("clients", clients);

        model.getDestination().getPostgres().setEnabled(true);
        model.getDestination().getPostgres().setSchema("videoanalytics");
        return model;
    }

    private ConfigModel postgresConfigOnlyStreamGroups() {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());

        ConfigModel.TableConfig streamGroups = new ConfigModel.TableConfig();
        streamGroups.setEnabled(true);
        model.getMigration().getTables().put("stream_groups", streamGroups);

        model.getDestination().getPostgres().setEnabled(true);
        model.getDestination().getPostgres().setSchema("videoanalytics");
        return model;
    }

    private ConfigModel postgresConfigOnlyStatsTraffic() {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());

        ConfigModel.TableConfig statsTraffic = new ConfigModel.TableConfig();
        statsTraffic.setEnabled(true);
        model.getMigration().getTables().put("stats_traffic", statsTraffic);

        model.getDestination().getPostgres().setEnabled(true);
        model.getDestination().getPostgres().setSchema("videoanalytics");
        return model;
    }

    private ConfigModel postgresConfigOnlyAlprListEvents() {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());

        ConfigModel.TableConfig alprListEvents = new ConfigModel.TableConfig();
        alprListEvents.setEnabled(true);
        model.getMigration().getTables().put("alpr_list_events", alprListEvents);

        model.getDestination().getPostgres().setEnabled(true);
        model.getDestination().getPostgres().setSchema("videoanalytics");
        return model;
    }

    private ConfigModel mysqlDestinationConfig() {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());

        ConfigModel.TableConfig clients = new ConfigModel.TableConfig();
        clients.setEnabled(true);
        model.getMigration().getTables().put("clients", clients);

        ConfigModel.TableConfig eventManager = new ConfigModel.TableConfig();
        eventManager.setEnabled(true);
        model.getMigration().getTables().put("event_manager", eventManager);

        ConfigModel.TableConfig settings = new ConfigModel.TableConfig();
        settings.setEnabled(true);
        model.getMigration().getTables().put("settings", settings);

        ConfigModel.TableConfig statsTraffic = new ConfigModel.TableConfig();
        statsTraffic.setEnabled(true);
        model.getMigration().getTables().put("stats_traffic", statsTraffic);

        model.getDestination().getMysql().setEnabled(true);
        model.getDestination().getMysql().setDatabase("videoanalytics");
        return model;
    }
}
