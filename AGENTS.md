# AI Agent Instructions

## Project
Java 17 / Spring Boot CLI application for VideoAnalytics database migration (old MySQL schema → new PostgreSQL schema).
Build: ./mvnw -B test (run after every code change)

## Rules
- Full spec: until/spec.md — read ONLY the sections relevant to the current task
- Don't modify: spec.md, README.md without direct request
- Tests must not call external services or real databases
- config.json is gitignored, never commit it

## Testing
- Use Mockito for DB interactions
- Use in-memory H2 for integration-level migrator tests
- No Docker, no Testcontainers
- If tests fail: review exception → fix code → run tests → repeat

## Package structure
com.vezha.migrator
├── config/         AppConfig, ConfigModel, DataSourceFactory
├── reader/         SourceReader (interface), SqlFileReader, DatabaseReader
├── migration/      MigrationOrchestrator, TableMigrator (interface)
│   └── impl/       One migrator class per table — see spec.md §1
├── facemigration/  FaceImageOrganizer
└── util/           IdToUuidResolver, StreamToAnalyticsResolver, BatchInserter

## Key resolver APIs
StreamToAnalyticsResolver:
- getFirstByPlugin(int streamId, String pluginName) → Optional<Integer>
- getAll(int streamId) → List<Integer>

IdToUuidResolver:
- getUuid(int streamId) → Optional<UUID>

## Plugin name constants (old analytics.plugin_name values)
AlprAnalyticsModule, FaceAnalyticsModule, RailroadsAnalyticsModule,
TrafficAnalyticsModule, GenderAgeAnalyticsModule, GunDetectionAnalyticsModule,
HardhatsAnalyticsModule, SmokeAndFireAnalyticsModule, ObjectInZoneModule,
PoseEstimationAnalyticsModule, ForestFireAnalyticsModule, MilitaryAnalyticsModule,
MotionAnalyticsModule, HeatmapAnalyticsModule, SmartTrackingAnalyticsModule,
SmartVaModule, TrafficLightsAnalyticsModule

## Stream → analytics_id resolution per table
- alpr_stats_hourly       → getFirstByPlugin(stream_id, "AlprAnalyticsModule")
- alpr_speed_rules        → getFirstByPlugin(stream_id1/2, "AlprAnalyticsModule")
- railroad_numbers        → getFirstByPlugin(stream_id, "RailroadsAnalyticsModule")
- alpr_detections         → va_id directly (fallback: getFirstByPlugin)
- alpr_list_events        → va_id directly (fallback: getFirstByPlugin)
- alpr_lists / face_lists → getAll() for all streams, flatten to analytics_ids JSON array

## Tables where original IDs are NOT preserved (auto-generated)
- event_manager (serial id auto-generated; old varchar id → uuid column)
- system_settings (serial id auto-generated)
- stats_traffic_minutely (merged from two tables; IDs would collide)

## Post-migration
After every migration run, reset PostgreSQL sequences for all identity columns
except the three tables above. See spec.md §7.1.

## Token discipline
- Write each file completely in one operation, never partially
- Do not re-read a file you wrote in the same session
- Run ./mvnw -B test only once after all files in the current task are written
- Do not explore project structure — it is fully described in this file

## How to read the spec
- `spec.md` contains the full technical specification — DO NOT read it top to bottom
- `index.md` maps each subtask to exact spec line ranges and files to create
- At the start of every task: read `index.md` first, then read ONLY the spec lines listed for your current subtask
- Never read spec sections not listed in your subtask's index entry
```
And then start each subtask prompt with:
```
Read index.md, find the entry for Subtask N, then read only the listed
spec.md line ranges. Then implement the listed files.
