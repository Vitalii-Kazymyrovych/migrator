# Migration Tool — Subtask Index

For each subtask: which files to create and which lines in `spec.md` to read.
Read ONLY the listed spec lines. Do not read anything else.

---

## Subtask 1 — Infrastructure & Config

### spec.md lines to read
- Lines 70–185 — Section 2: config.json specification & validation rules
- Lines 189–213 — Section 3: IdToUuidResolver + StreamToAnalyticsResolver
- Lines 760–796 — Section 7: Batching & performance + Section 7.1: Sequence reset
- Lines 798–808 — Section 9: Tests for AppConfig, DataSourceFactory, IdToUuidResolver, StreamToAnalyticsResolver, BatchInserter, MigrationOrchestrator

### Files to create
```
src/main/java/com/videoanalytics/migrator/
  config/
    ConfigModel.java
    AppConfig.java
    DataSourceFactory.java
  migration/
    TableMigrator.java          (interface)
    MigrationOrchestrator.java  (shell — calls migrators in order, sequence reset)
  util/
    IdToUuidResolver.java
    StreamToAnalyticsResolver.java
    BatchInserter.java

src/test/java/com/videoanalytics/migrator/
  config/
    AppConfigTest.java
    DataSourceFactoryTest.java
  util/
    IdToUuidResolverTest.java
    StreamToAnalyticsResolverTest.java
    BatchInserterTest.java
  migration/
    MigrationOrchestratorTest.java
```

---

## Subtask 2 — Simple Direct Migrators

### spec.md lines to read
- Lines 217–221 — Section 4.1: clients
- Lines 222–227 — Section 4.2: streams
- Lines 291–299 — Section 4.6: roles
- Lines 300–310 — Section 4.7: servers
- Lines 330–341 — Section 4.9: event_manager
- Lines 342–351 — Section 4.10: settings -> system_settings
- Lines 374–378 — Section 4.12: alpr_list_items
- Lines 485–488 — Section 4.21 (gun_type_mapping note)
- Lines 507–509 — Section 4.23 (smoke_fire_type_mapping note)
- Lines 520–522 — Section 4.24 (object_in_zone_object_type note)
- Lines 532–533 — Section 4.25 (zone_exit_notifications_object_type note)
- Lines 809–815 — Section 9: tests for these migrators

### Files to create
```
src/main/java/com/videoanalytics/migrator/migration/impl/
  ClientsMigrator.java
  StreamsMigrator.java
  RolesMigrator.java
  ServersMigrator.java
  EventManagerMigrator.java
  SettingsMigrator.java
  AlprListItemsMigrator.java
  GunTypeMappingMigrator.java
  SmokeFireTypeMappingMigrator.java
  ObjectInZoneObjectTypeMigrator.java
  ZoneExitNotificationsObjectTypeMigrator.java

src/test/java/com/videoanalytics/migrator/migration/impl/
  ClientsMigratorTest.java
  StreamsMigratorTest.java
  RolesMigratorTest.java
  ServersMigratorTest.java
  EventManagerMigratorTest.java
  SettingsMigratorTest.java
  AlprListItemsMigratorTest.java
```

---

## Subtask 3 — Complex Core Migrators

### spec.md lines to read
- Lines 228–247 — Section 4.3: stream_groups (dual-write to analytics_groups)
- Lines 248–280 — Section 4.4: analytics
- Lines 282–290 — Section 4.5: users
- Lines 311–329 — Section 4.8: audit_trail
- Lines 809–812 — Section 9: tests for StreamGroups, Analytics, Users, AuditTrail

### Files to create
```
src/main/java/com/videoanalytics/migrator/migration/impl/
  StreamGroupsMigrator.java
  AnalyticsMigrator.java
  UsersMigrator.java
  AuditTrailMigrator.java

src/test/java/com/videoanalytics/migrator/migration/impl/
  StreamGroupsMigratorTest.java
  AnalyticsMigratorTest.java
  UsersMigratorTest.java
  AuditTrailMigratorTest.java
```

---

## Subtask 4 — ALPR Migrators

### spec.md lines to read
- Lines 352–373 — Section 4.11: alpr_lists
- Lines 379–390 — Section 4.13: alpr_plates -> alpr_detections
- Lines 391–408 — Section 4.14: alpr_notifications -> alpr_list_events
- Lines 409–417 — Section 4.15: alpr_stats_hourly -> alpr_hourly_statistics
- Lines 418–427 — Section 4.16: alpr_speed_rules
- Lines 428–443 — Section 4.17: alpr_speed_rule_events
- Lines 815–820 — Section 9: tests for ALPR migrators

### Files to create
```
src/main/java/com/videoanalytics/migrator/migration/impl/
  AlprListsMigrator.java
  AlprDetectionsMigrator.java
  AlprListEventsMigrator.java
  AlprHourlyStatsMigrator.java
  AlprSpeedRulesMigrator.java
  AlprSpeedRuleEventsMigrator.java

src/test/java/com/videoanalytics/migrator/migration/impl/
  AlprListsMigratorTest.java
  AlprDetectionsMigratorTest.java
  AlprListEventsMigratorTest.java
  AlprHourlyStatsMigratorTest.java
  AlprSpeedRulesMigratorTest.java
  AlprSpeedRuleEventsMigratorTest.java
```

---

## Subtask 5 — Notifications, Stats & Port Logistics

### spec.md lines to read
- Lines 444–457 — Section 4.18: traffic_stat
- Lines 458–470 — Section 4.19: stats_traffic (merge)
- Lines 471–476 — Section 4.20: gender_age_stat
- Lines 477–488 — Section 4.21: gun_notifications
- Lines 489–498 — Section 4.22: hardhats_notifications
- Lines 499–510 — Section 4.23: smoke_fire_notifications
- Lines 511–523 — Section 4.24: object_in_zone_notifications
- Lines 524–535 — Section 4.25: zone_exit_notifications
- Lines 536–547 — Section 4.26: railroad_numbers
- Lines 548–557 — Section 4.27: port_logistics_container_numbers
- Lines 558–579 — Section 4.28: port_logistics_detections
- Lines 580–597 — Section 4.29: port_logistics_rules
- Lines 821–832 — Section 9: tests for these migrators

### Files to create
```
src/main/java/com/videoanalytics/migrator/migration/impl/
  TrafficStatMigrator.java
  StatsTrafficMigrator.java
  GenderAgeStatMigrator.java
  GunNotificationsMigrator.java
  HardhatsNotificationsMigrator.java
  SmokeFireNotificationsMigrator.java
  ObjectInZoneMigrator.java
  ZoneExitNotificationsMigrator.java
  RailroadNumbersMigrator.java
  PortLogisticsMigrator.java        (covers container_numbers, detections, rules)

src/test/java/com/videoanalytics/migrator/migration/impl/
  TrafficStatMigratorTest.java
  StatsTrafficMigratorTest.java
  GenderAgeStatMigratorTest.java
  GunNotificationsMigratorTest.java
  HardhatsNotificationsMigratorTest.java
  SmokeFireNotificationsMigratorTest.java
  ObjectInZoneMigratorTest.java
  ZoneExitNotificationsMigratorTest.java
  RailroadNumbersMigratorTest.java
  PortLogisticsContainerNumbersMigratorTest.java
  PortLogisticsDetectionsMigratorTest.java
  PortLogisticsRulesMigratorTest.java
```

---

## Subtask 6 — Face Lists, Image Organizer, Wiring & README

### spec.md lines to read
- Lines 598–648 — Section 4.30: face_lists + face_list_items mapping
- Lines 649–715 — Section 5: Face image processing (all steps + edge cases)
- Lines 717–758 — Section 6: Migration execution order (for wiring MigratorApplication)
- Lines 833–836 — Section 9: tests for FaceLists, FaceListItems, FaceImageOrganizer
- Lines 840–920 — Section 10: README requirements

### Files to create
```
src/main/java/com/videoanalytics/migrator/
  MigratorApplication.java            (final wiring — reads config, runs orchestrator)
  migration/impl/
    FaceListsMigrator.java
  facemigration/
    FaceImageOrganizer.java
  reader/
    SourceReader.java                 (interface)
    DatabaseReader.java
    SqlFileReader.java

src/test/java/com/videoanalytics/migrator/
  migration/impl/
    FaceListsMigratorTest.java
    FaceListItemsMigratorTest.java
  facemigration/
    FaceImageOrganizerTest.java

README.md
```