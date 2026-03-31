package com.vezha.migrator.migration;

import com.vezha.migrator.config.ConfigModel;
import com.vezha.migrator.migration.impl.AlprListEventsMigrator;
import com.vezha.migrator.migration.impl.StatsTrafficMigrator;
import com.vezha.migrator.migration.impl.StreamGroupsMigrator;
import com.vezha.migrator.util.IdToUuidResolver;
import com.vezha.migrator.util.SourceSchemaInspector;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import com.vezha.migrator.util.TargetSchemaInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MigrationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(MigrationOrchestrator.class);

    private static final List<String> EXCLUDED_SEQUENCE_RESET_TABLES = List.of(
            "event_manager",
            "system_settings",
            "stats_traffic_minutely"
    );

    private final JdbcTemplate sourceJdbcTemplate;
    private final JdbcTemplate destinationJdbcTemplate;
    private final IdToUuidResolver idToUuidResolver;
    private final StreamToAnalyticsResolver streamToAnalyticsResolver;
    private final SourceSchemaInspector sourceSchemaInspector;
    private final TargetSchemaInspector targetSchemaInspector;
    private final List<TableMigrator> migrators;

    public MigrationOrchestrator(
            JdbcTemplate sourceJdbcTemplate,
            JdbcTemplate destinationJdbcTemplate,
            IdToUuidResolver idToUuidResolver,
            StreamToAnalyticsResolver streamToAnalyticsResolver,
            SourceSchemaInspector sourceSchemaInspector,
            TargetSchemaInspector targetSchemaInspector,
            List<TableMigrator> migrators
    ) {
        this.sourceJdbcTemplate = sourceJdbcTemplate;
        this.destinationJdbcTemplate = destinationJdbcTemplate;
        this.idToUuidResolver = idToUuidResolver;
        this.streamToAnalyticsResolver = streamToAnalyticsResolver;
        this.sourceSchemaInspector = sourceSchemaInspector;
        this.targetSchemaInspector = targetSchemaInspector;
        this.migrators = new ArrayList<>(migrators);
    }

    public void run(ConfigModel configModel) {
        idToUuidResolver.load(sourceJdbcTemplate);
        streamToAnalyticsResolver.load(sourceJdbcTemplate);
        sourceSchemaInspector.load(configModel);
        targetSchemaInspector.load(configModel);

        Map<String, ConfigModel.TableConfig> tables = configModel.getMigration().getTables();
        migrators.stream()
                .filter(migrator -> isEnabled(tables, migrator.tableName()))
                .forEach(this::runIfTablesExist);

        resetSequences(configModel);
    }

    private void runIfTablesExist(TableMigrator migrator) {
        if (!validateSourceTables(migrator)) {
            return;
        }

        if (migrator instanceof StreamGroupsMigrator streamGroupsMigrator) {
            if (!targetSchemaInspector.tableExists("stream_groups")) {
                log.info("Skipping {}: table {} not found in target DB", migrator.getClass().getSimpleName(), "stream_groups");
                return;
            }
            if (!targetSchemaInspector.tableExists("analytics_groups")) {
                log.info("{}: table {} not found in target DB, skipping analytics_groups write", migrator.getClass().getSimpleName(), "analytics_groups");
                streamGroupsMigrator.setMigrateAnalyticsGroups(false);
            }
            migrator.migrate();
            return;
        }

        String targetTable = migrator.getTargetTable();
        if (!targetSchemaInspector.tableExists(targetTable)) {
            log.info("Skipping {}: table {} not found in target DB", migrator.getClass().getSimpleName(), targetTable);
            return;
        }

        migrator.migrate();
    }

    private boolean validateSourceTables(TableMigrator migrator) {
        if (migrator instanceof AlprListEventsMigrator) {
            if (!sourceSchemaInspector.tableExists("alpr_notifications")) {
                log.info("Skipping {}: source table {} not found in source DB", migrator.getClass().getSimpleName(), "alpr_notifications");
                return false;
            }
            if (!sourceSchemaInspector.tableExists("alpr_plates")) {
                log.warn("{}: source table {} not found in source DB; JOIN will produce no results", migrator.getClass().getSimpleName(), "alpr_plates");
            }
            return true;
        }

        if (migrator instanceof StatsTrafficMigrator) {
            boolean hasHourly = sourceSchemaInspector.tableExists("stats_traffic_hourly");
            boolean hasMinutely = sourceSchemaInspector.tableExists("stats_traffic_minutely");
            if (!hasHourly && !hasMinutely) {
                log.info("Skipping {}: source table {} not found in source DB", migrator.getClass().getSimpleName(), "stats_traffic_hourly");
                log.info("Skipping {}: source table {} not found in source DB", migrator.getClass().getSimpleName(), "stats_traffic_minutely");
                return false;
            }
            if (!hasHourly) {
                log.info("{}: source table {} not found in source DB; migrating from {} only",
                        migrator.getClass().getSimpleName(), "stats_traffic_hourly", "stats_traffic_minutely");
            }
            if (!hasMinutely) {
                log.info("{}: source table {} not found in source DB; migrating from {} only",
                        migrator.getClass().getSimpleName(), "stats_traffic_minutely", "stats_traffic_hourly");
            }
            return true;
        }

        for (String sourceTable : migrator.getSourceTables()) {
            if (!sourceSchemaInspector.tableExists(sourceTable)) {
                log.info("Skipping {}: source table {} not found in source DB", migrator.getClass().getSimpleName(), sourceTable);
                return false;
            }
        }
        return true;
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
        Boolean numericId;
        try {
            numericId = destinationJdbcTemplate.queryForObject(
                    "SELECT data_type IN ('smallint','integer','bigint') " +
                            "FROM information_schema.columns " +
                            "WHERE table_schema = ? AND table_name = ? AND column_name = 'id'",
                    Boolean.class,
                    schema,
                    table
            );
        } catch (EmptyResultDataAccessException ignored) {
            return;
        }
        if (!Boolean.TRUE.equals(numericId)) {
            return;
        }

        Long maxId = destinationJdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) FROM " + schema + "." + table,
                Long.class
        );
        long sequenceValue = (maxId != null && maxId > 0) ? maxId : 1L;
        boolean isCalled = maxId != null && maxId > 0;
        destinationJdbcTemplate.execute(
                "SELECT setval(pg_get_serial_sequence('" + schema + "." + table + "', 'id'), "
                        + sequenceValue + ", " + isCalled + ")"
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
