# Claude Code Instructions

## Project
Java 17 / Spring Boot CLI application for VideoAnalytics database migration (old MySQL schema → new PostgreSQL schema).
Build: After writing all files run: ./mvnw -B compile test-compile (run after finishing task)

## Rules
- Full spec: until/spec.md — read ONLY the sections relevant to the current task
- Don't modify: spec.md, README.md without direct request
- Don't do: git add, git commit, git push — I handle git myself
- Tests must not call external services or real databases
- config.json is gitignored, never commit it

## Testing
- Use Mockito for DB interactions
- Use in-memory H2 for integration-level migrator tests
- No Docker, no Testcontainers

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
- Run ./mvnw -B compile test-compile only once after all files in the current task are written
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