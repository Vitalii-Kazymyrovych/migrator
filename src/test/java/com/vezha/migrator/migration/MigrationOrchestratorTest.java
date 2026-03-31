package com.vezha.migrator.migration;

import com.vezha.migrator.config.ConfigModel;
import com.vezha.migrator.util.IdToUuidResolver;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;

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

        IdToUuidResolver idResolver = mock(IdToUuidResolver.class);
        StreamToAnalyticsResolver analyticsResolver = mock(StreamToAnalyticsResolver.class);

        TableMigrator clientsMigrator = migrator("clients");
        TableMigrator settingsMigrator = migrator("settings");
        TableMigrator eventManagerMigrator = migrator("event_manager");

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                idResolver,
                analyticsResolver,
                List.of(clientsMigrator, settingsMigrator, eventManagerMigrator)
        );

        ConfigModel config = postgresConfig();
        orchestrator.run(config);

        verify(idResolver, times(1)).load(sourceJdbc);
        verify(analyticsResolver, times(1)).load(sourceJdbc);

        verify(clientsMigrator, times(1)).migrate();
        verify(settingsMigrator, times(1)).migrate();
        verify(eventManagerMigrator, never()).migrate();

        verify(destinationJdbc, times(1)).execute("SELECT setval(pg_get_serial_sequence('videoanalytics.clients', 'id'), 10)");
        verify(destinationJdbc, times(1)).execute("SELECT setval(pg_get_serial_sequence('videoanalytics.alpr_stats_hourly', 'id'), 10)");
        verify(destinationJdbc, never()).execute("SELECT setval(pg_get_serial_sequence('videoanalytics.system_settings', 'id'), 10)");
    }

    @Test
    void resetsMysqlAutoIncrementForEligibleTables() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(7L);

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbc,
                destinationJdbc,
                mock(IdToUuidResolver.class),
                mock(StreamToAnalyticsResolver.class),
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
        TableMigrator migrator = mock(TableMigrator.class);
        when(migrator.tableName()).thenReturn(name);
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
        return model;
    }
}
