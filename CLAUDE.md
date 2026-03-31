# Claude Code Instructions

## Project
Java 17 / Spring Boot CLI application for VideoAnalytics database migration (old MySQL schema → new PostgreSQL schema).
Build: ./mvnw -B test (run after every code change)

## Rules
- Full spec: TECHNICAL_TASK.md — read ONLY the sections relevant to the current task
- Don't modify: TECHNICAL_TASK.md, README.md without direct request
- Don't do: git add, git commit, git push — I handle git myself
- Tests must not call external services or real databases
- config.json is gitignored, never commit it

## Testing
- Use Mockito for DB interactions
- Use in-memory H2 for integration-level migrator tests
- No Docker, no Testcontainers
- If tests fail: review exception → fix code → run tests → repeat

## File editing
- NEVER use string replacement on multi-line blocks (PowerShell Replace, python str.replace, sed multi-line)
- For changes spanning more than 3 lines: rewrite the ENTIRE method or ENTIRE file
- If an edit fails on first attempt: rewrite the whole file, don't debug whitespace
- Maximum 2 attempts for any single file edit. If both fail — rewrite the file from scratch.
- NEVER write PowerShell scripts. Use only: Read/Write tool, or bash -c with simple commands.
- NEVER use bash/python/powershell to read or edit Java files. Use ONLY built-in Read/Write/Update tools.

## Anti-loop rule
- If the same operation (edit, search, build) fails twice with similar errors: STOP and explain the problem to me. Do NOT retry more than twice.

## Package structure
com.videoanalytics.migrator
├── config/         AppConfig, ConfigModel, DataSourceFactory
├── reader/         SourceReader (interface), SqlFileReader, DatabaseReader
├── migration/      MigrationOrchestrator, TableMigrator (interface)
│   └── impl/       One migrator class per table — see TECHNICAL_TASK.md §1
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
except the three tables above. See TECHNICAL_TASK.md §7.1.