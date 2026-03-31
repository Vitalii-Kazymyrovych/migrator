package com.vezha.migrator;

import com.vezha.migrator.config.AppConfig;
import com.vezha.migrator.config.ConfigModel;
import com.vezha.migrator.config.DataSourceFactory;
import com.vezha.migrator.facemigration.FaceImageOrganizer;
import com.vezha.migrator.migration.MigrationOrchestrator;
import com.vezha.migrator.migration.TableMigrator;
import com.vezha.migrator.migration.impl.AlprDetectionsMigrator;
import com.vezha.migrator.migration.impl.AlprHourlyStatsMigrator;
import com.vezha.migrator.migration.impl.AlprListEventsMigrator;
import com.vezha.migrator.migration.impl.AlprListItemsMigrator;
import com.vezha.migrator.migration.impl.AlprListsMigrator;
import com.vezha.migrator.migration.impl.AlprSpeedRuleEventsMigrator;
import com.vezha.migrator.migration.impl.AlprSpeedRulesMigrator;
import com.vezha.migrator.migration.impl.AnalyticsMigrator;
import com.vezha.migrator.migration.impl.AuditTrailMigrator;
import com.vezha.migrator.migration.impl.ClientsMigrator;
import com.vezha.migrator.migration.impl.EventManagerMigrator;
import com.vezha.migrator.migration.impl.FaceListItemsMigrator;
import com.vezha.migrator.migration.impl.FaceListsMigrator;
import com.vezha.migrator.migration.impl.GenderAgeStatMigrator;
import com.vezha.migrator.migration.impl.GunNotificationsMigrator;
import com.vezha.migrator.migration.impl.GunTypeMappingMigrator;
import com.vezha.migrator.migration.impl.HardhatsNotificationsMigrator;
import com.vezha.migrator.migration.impl.ObjectInZoneMigrator;
import com.vezha.migrator.migration.impl.ObjectInZoneObjectTypeMigrator;
import com.vezha.migrator.migration.impl.PortLogisticsMigrator;
import com.vezha.migrator.migration.impl.RailroadNumbersMigrator;
import com.vezha.migrator.migration.impl.RolesMigrator;
import com.vezha.migrator.migration.impl.ServersMigrator;
import com.vezha.migrator.migration.impl.SettingsMigrator;
import com.vezha.migrator.migration.impl.SmokeFireNotificationsMigrator;
import com.vezha.migrator.migration.impl.SmokeFireTypeMappingMigrator;
import com.vezha.migrator.migration.impl.StatsTrafficMigrator;
import com.vezha.migrator.migration.impl.StreamGroupsMigrator;
import com.vezha.migrator.migration.impl.StreamsMigrator;
import com.vezha.migrator.migration.impl.TrafficStatMigrator;
import com.vezha.migrator.migration.impl.UsersMigrator;
import com.vezha.migrator.migration.impl.ZoneExitNotificationsMigrator;
import com.vezha.migrator.migration.impl.ZoneExitNotificationsObjectTypeMigrator;
import com.vezha.migrator.reader.DatabaseReader;
import com.vezha.migrator.reader.SourceReader;
import com.vezha.migrator.reader.SqlFileReader;
import com.vezha.migrator.util.IdToUuidResolver;
import com.vezha.migrator.util.SourceSchemaInspector;
import com.vezha.migrator.util.StreamToAnalyticsResolver;
import com.vezha.migrator.util.TargetSchemaInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class MigratorApplication {

    private static final Logger log = LoggerFactory.getLogger(MigratorApplication.class);

    public static void main(String[] args) {
        AppConfig appConfig = new AppConfig();
        ConfigModel configModel = appConfig.load(Path.of("config.json"));

        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        SourceReader sourceReader = configModel.getSource().getSqlFile().isEnabled()
                ? new SqlFileReader()
                : new DatabaseReader(dataSourceFactory);

        JdbcTemplate sourceJdbcTemplate = sourceReader.read(configModel);
        JdbcTemplate destinationJdbcTemplate = new JdbcTemplate(dataSourceFactory.forDestination(configModel.getDestination()));

        IdToUuidResolver idToUuidResolver = new IdToUuidResolver();
        StreamToAnalyticsResolver streamToAnalyticsResolver = new StreamToAnalyticsResolver();

        List<TableMigrator> migrators = List.of(
                new ClientsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new ServersMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new RolesMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new StreamsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new StreamGroupsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new AnalyticsMigrator(sourceJdbcTemplate, destinationJdbcTemplate, idToUuidResolver),
                new UsersMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new AuditTrailMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new EventManagerMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new SettingsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new AlprListsMigrator(sourceJdbcTemplate, destinationJdbcTemplate, streamToAnalyticsResolver),
                new AlprListItemsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new AlprDetectionsMigrator(sourceJdbcTemplate, destinationJdbcTemplate, streamToAnalyticsResolver),
                new AlprListEventsMigrator(sourceJdbcTemplate, destinationJdbcTemplate, streamToAnalyticsResolver),
                new AlprHourlyStatsMigrator(sourceJdbcTemplate, destinationJdbcTemplate, streamToAnalyticsResolver),
                new AlprSpeedRulesMigrator(sourceJdbcTemplate, destinationJdbcTemplate, streamToAnalyticsResolver),
                new AlprSpeedRuleEventsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new TrafficStatMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new StatsTrafficMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new GenderAgeStatMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new GunNotificationsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new GunTypeMappingMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new HardhatsNotificationsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new SmokeFireNotificationsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new SmokeFireTypeMappingMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new ObjectInZoneMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new ObjectInZoneObjectTypeMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new ZoneExitNotificationsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new ZoneExitNotificationsObjectTypeMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new RailroadNumbersMigrator(sourceJdbcTemplate, destinationJdbcTemplate, streamToAnalyticsResolver),
                new PortLogisticsMigrator(sourceJdbcTemplate, destinationJdbcTemplate),
                new FaceListsMigrator(sourceJdbcTemplate, destinationJdbcTemplate, streamToAnalyticsResolver),
                new FaceListItemsMigrator(sourceJdbcTemplate, destinationJdbcTemplate)
        );

        MigrationOrchestrator orchestrator = new MigrationOrchestrator(
                sourceJdbcTemplate,
                destinationJdbcTemplate,
                idToUuidResolver,
                streamToAnalyticsResolver,
                new SourceSchemaInspector(sourceJdbcTemplate),
                new TargetSchemaInspector(destinationJdbcTemplate),
                migrators
        );
        orchestrator.run(configModel);

        logNewOnlyAndSkippedTables();
        runFaceImageProcessingIfEnabled(configModel, sourceJdbcTemplate);
    }

    private static void runFaceImageProcessingIfEnabled(ConfigModel configModel, JdbcTemplate sourceJdbcTemplate) {
        Map<String, ConfigModel.TableConfig> tables = configModel.getMigration().getTables();
        ConfigModel.TableConfig faceListsConfig = tables.get("face_lists");
        if (faceListsConfig != null && faceListsConfig.isEnabled()) {
            new FaceImageOrganizer().organize(sourceJdbcTemplate, faceListsConfig.getImageFolderPath());
        }
    }

    private static void logNewOnlyAndSkippedTables() {
        log.info("api_tokens has no source table and will remain empty after migration");
        log.info("cleaning_settings has no source table and will remain empty after migration");
        log.info("sounds_settings has no source table and will remain empty after migration");
        log.info("plugin_configurations has no source table and will remain empty after migration");
        log.info("port_logistics_rule_groups has no source table and will remain empty after migration");
        log.info("traffic_lights_detections has no destination table and is skipped");
    }
}
