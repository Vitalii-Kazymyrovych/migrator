package com.vezha.migrator.migration;

import com.vezha.migrator.config.ConfigModel;
import com.vezha.migrator.util.IdToUuidResolver;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MigrationOrchestrator {

    private static final List<String> EXCLUDED_SEQUENCE_RESET_TABLES = List.of(
            "event_manager",
            "system_settings",
            "stats_traffic_minutely"
    );

    private final JdbcTemplate sourceJdbcTemplate;
    private final JdbcTemplate destinationJdbcTemplate;
    private final IdToUuidResolver idToUuidResolver;
    private final StreamToAnalyticsResolver streamToAnalyticsResolver;
    private final List<TableMigrator> migrators;

    public MigrationOrchestrator(
            JdbcTemplate sourceJdbcTemplate,
            JdbcTemplate destinationJdbcTemplate,
            IdToUuidResolver idToUuidResolver,
            StreamToAnalyticsResolver streamToAnalyticsResolver,
            List<TableMigrator> migrators
    ) {
        this.sourceJdbcTemplate = sourceJdbcTemplate;
        this.destinationJdbcTemplate = destinationJdbcTemplate;
        this.idToUuidResolver = idToUuidResolver;
        this.streamToAnalyticsResolver = streamToAnalyticsResolver;
        this.migrators = new ArrayList<>(migrators);
    }

    public void run(ConfigModel configModel) {
        idToUuidResolver.load(sourceJdbcTemplate);
        streamToAnalyticsResolver.load(sourceJdbcTemplate);

        Map<String, ConfigModel.TableConfig> tables = configModel.getMigration().getTables();
        migrators.stream()
                .filter(migrator -> isEnabled(tables, migrator.tableName()))
                .forEach(TableMigrator::migrate);

        resetSequences(configModel);
    }

    private boolean isEnabled(Map<String, ConfigModel.TableConfig> tableConfigMap, String table) {
        ConfigModel.TableConfig tableConfig = tableConfigMap.get(table);
        return tableConfig != null && tableConfig.isEnabled();
    }

    void resetSequences(ConfigModel configModel) {
        Map<String, ConfigModel.TableConfig> tables = configModel.getMigration().getTables();
        if (configModel.getDestination().getPostgres().isEnabled()) {
            String schema = configModel.getDestination().getPostgres().getSchema();
            tables.keySet().stream()
                    .filter(table -> isEnabled(tables, table))
                    .map(this::mapToDestinationTable)
                    .distinct()
                    .filter(table -> !EXCLUDED_SEQUENCE_RESET_TABLES.contains(table))
                    .forEach(table -> resetPostgresSequence(schema, table));
            return;
        }

        if (configModel.getDestination().getMysql().isEnabled()) {
            tables.keySet().stream()
                    .filter(table -> isEnabled(tables, table))
                    .map(this::mapToDestinationTable)
                    .distinct()
                    .filter(table -> !EXCLUDED_SEQUENCE_RESET_TABLES.contains(table))
                    .forEach(this::resetMySqlSequence);
        }
    }

    private String mapToDestinationTable(String table) {
        return switch (table) {
            case "settings" -> "system_settings";
            case "stats_traffic" -> "stats_traffic_minutely";
            case "alpr_hourly_statistics" -> "alpr_stats_hourly";
            default -> table;
        };
    }

    private void resetPostgresSequence(String schema, String table) {
        Long maxId = destinationJdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) FROM " + schema + "." + table,
                Long.class
        );
        destinationJdbcTemplate.execute(
                "SELECT setval(pg_get_serial_sequence('" + schema + "." + table + "', 'id'), " + maxId + ")"
        );
    }

    private void resetMySqlSequence(String table) {
        Long maxId = destinationJdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) FROM " + table,
                Long.class
        );
        destinationJdbcTemplate.execute("ALTER TABLE " + table + " AUTO_INCREMENT = " + (maxId + 1));
    }
}
