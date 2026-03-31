# Technical Task: VideoAnalytics DB Migration Tool (v2)

## Overview

A standalone Java Spring Boot CLI application that migrates data from the **old** VideoAnalytics MySQL database schema to the **new** PostgreSQL schema. Configured entirely via `config.json` in the same directory as the `.jar`. Also handles face image renaming and folder organisation as a separate optional step.

---

## 1. Project Structure

```
db-migrator/
├── src/
│   ├── main/java/com/videoanalytics/migrator/
│   │   ├── MigratorApplication.java
│   │   ├── config/
│   │   │   ├── AppConfig.java
│   │   │   ├── ConfigModel.java
│   │   │   └── DataSourceFactory.java
│   │   ├── reader/
│   │   │   ├── SourceReader.java               # interface
│   │   │   ├── SqlFileReader.java
│   │   │   └── DatabaseReader.java
│   │   ├── migration/
│   │   │   ├── MigrationOrchestrator.java
│   │   │   ├── TableMigrator.java              # interface
│   │   │   └── impl/
│   │   │       ├── ClientsMigrator.java
│   │   │       ├── StreamsMigrator.java
│   │   │       ├── StreamGroupsMigrator.java   # writes to BOTH stream_groups AND analytics_groups
│   │   │       ├── AnalyticsMigrator.java
│   │   │       ├── UsersMigrator.java
│   │   │       ├── RolesMigrator.java
│   │   │       ├── ServersMigrator.java
│   │   │       ├── AuditTrailMigrator.java
│   │   │       ├── EventManagerMigrator.java
│   │   │       ├── SettingsMigrator.java
│   │   │       ├── AlprListsMigrator.java
│   │   │       ├── AlprListItemsMigrator.java
│   │   │       ├── AlprDetectionsMigrator.java
│   │   │       ├── AlprListEventsMigrator.java
│   │   │       ├── AlprHourlyStatsMigrator.java
│   │   │       ├── AlprSpeedRulesMigrator.java
│   │   │       ├── AlprSpeedRuleEventsMigrator.java
│   │   │       ├── TrafficStatMigrator.java
│   │   │       ├── StatsTrafficMigrator.java
│   │   │       ├── GenderAgeStatMigrator.java
│   │   │       ├── GunNotificationsMigrator.java
│   │   │       ├── HardhatsNotificationsMigrator.java
│   │   │       ├── SmokeFireNotificationsMigrator.java
│   │   │       ├── ObjectInZoneMigrator.java
│   │   │       ├── ZoneExitNotificationsMigrator.java
│   │   │       ├── RailroadNumbersMigrator.java
│   │   │       ├── PortLogisticsMigrator.java
│   │   │       └── FaceListsMigrator.java
│   │   ├── facemigration/
│   │   │   └── FaceImageOrganizer.java
│   │   └── util/
│   │       ├── IdToUuidResolver.java
│   │       ├── StreamToAnalyticsResolver.java  # stream_id -> analytics_id(s)
│   │       └── BatchInserter.java
│   └── test/...
├── config.json
├── pom.xml
└── README.md
```

---

## 2. config.json Specification

Placed in the same directory as the `.jar` at runtime. All blocks are always present; only the one with `"enabled": true` in each section is used.

```json
{
  "source": {
    "sqlFile": {
      "enabled": false,
      "path": "/path/to/dump.sql"
    },
    "mysql": {
      "enabled": true,
      "host": "localhost",
      "port": 3306,
      "database": "videoanalytics",
      "username": "root",
      "password": "secret"
    },
    "postgres": {
      "enabled": false,
      "host": "localhost",
      "port": 5432,
      "database": "videoanalytics",
      "schema": "public",
      "username": "postgres",
      "password": "secret"
    },
    "mssql": {
      "enabled": false,
      "host": "localhost",
      "port": 1433,
      "database": "videoanalytics",
      "username": "sa",
      "password": "secret"
    },
    "oracle": {
      "enabled": false,
      "host": "localhost",
      "port": 1521,
      "serviceName": "ORCL",
      "username": "system",
      "password": "secret"
    },
    "h2": {
      "enabled": false,
      "url": "jdbc:h2:file:/path/to/db",
      "username": "sa",
      "password": ""
    }
  },

  "destination": {
    "postgres": {
      "enabled": true,
      "host": "localhost",
      "port": 5432,
      "database": "videoanalytics_new",
      "schema": "videoanalytics",
      "username": "postgres",
      "password": "secret"
    },
    "mysql": {
      "enabled": false,
      "host": "localhost",
      "port": 3306,
      "database": "videoanalytics_new",
      "username": "root",
      "password": "secret"
    }
  },

  "migration": {
    "batchSize": 1000,
    "tables": {
      "clients":                        { "enabled": true },
      "streams":                        { "enabled": true },
      "stream_groups":                  { "enabled": true },
      "analytics":                      { "enabled": true },
      "users":                          { "enabled": true },
      "roles":                          { "enabled": true },
      "servers":                        { "enabled": true },
      "audit_trail":                    { "enabled": true },
      "event_manager":                  { "enabled": true },
      "settings":                       { "enabled": true },
      "alpr_lists":                     { "enabled": true },
      "alpr_list_items":                { "enabled": true },
      "alpr_detections":                { "enabled": true },
      "alpr_list_events":               { "enabled": true },
      "alpr_hourly_statistics":         { "enabled": true },
      "alpr_speed_rules":               { "enabled": true },
      "alpr_speed_rule_events":         { "enabled": true },
      "traffic_stat":                   { "enabled": true },
      "stats_traffic":                  { "enabled": true },
      "gender_age_stat":                { "enabled": true },
      "gun_notifications":              { "enabled": true },
      "hardhats_notifications":         { "enabled": true },
      "smoke_fire_notifications":       { "enabled": true },
      "object_in_zone_notifications":   { "enabled": true },
      "zone_exit_notifications":        { "enabled": true },
      "railroad_numbers":               { "enabled": true },
      "port_logistics":                 { "enabled": true },
      "face_lists": {
        "enabled": false,
        "imageFolderPath": "/path/to/old/face_images"
      }
    }
  }
}
```

**Validation rules (fail fast on startup):**
- Exactly one source must have `"enabled": true`.
- Exactly one destination must have `"enabled": true`.
- If `face_lists.enabled = true`, `imageFolderPath` must be non-empty and point to an existing directory.
- All required credential fields for the enabled source/destination must be non-blank.

---

## 3. Pre-Migration Resolver Setup

Before any migrator runs, two resolvers are loaded into memory from the source DB:

### `IdToUuidResolver`
```
Map<Integer, UUID> streamIdToUuid = SELECT id, uuid FROM streams
```
Used to resolve old integer stream IDs to UUIDs wherever `stream_uuid` is referenced in the new schema.

### `StreamToAnalyticsResolver`
The resolver builds:
```
Map<Integer, List<AnalyticsEntry>> streamIdToAnalytics =
    SELECT stream_id, id, plugin_name FROM analytics
    ORDER BY id ASC
    -- grouped as stream_id -> [AnalyticsEntry(id, plugin_name), ...] (ascending by id)
```

Methods:
- `getFirstByPlugin(streamId, pluginName)` — filters candidates by `plugin_name`, returns the lowest matching `analytics_id` as `Optional<Integer>`. If no match -> log WARN with `stream_id` and `plugin_name`, return empty Optional.
- `getAll(streamId)` — returns all analytics IDs for a stream regardless of plugin, as `List<Integer>`. Used by `alpr_lists` and `face_lists` when resolving `streams -> analytics_ids` (where all analytics on a stream should be included).

---

## 4. Complete Table-by-Table Migration Mapping

### 4.1 `clients` -> `videoanalytics.clients`
Direct copy. All columns are identical.

---

### 4.2 `streams` -> `videoanalytics.streams`
Direct copy. Old `uuid` is `varchar(55)`; new is `uuid` — cast with `UUID.fromString()`.
All other columns are identical.

---

### 4.3 `stream_groups` -> `videoanalytics.stream_groups` AND `videoanalytics.analytics_groups`

**`analytics_groups` does not exist in the old schema.** In the old system, analytics were grouped implicitly by the stream they belonged to, and streams were grouped by `stream_groups`. The new schema introduced a parallel `analytics_groups` table with the same tree structure (`id, name, parent_id, plugin_name, client_id`).

One `stream_groups` row produces **two** inserts:
1. Into `videoanalytics.stream_groups` — direct copy of `id, parent_id, name, client_id`.
2. Into `videoanalytics.analytics_groups` — same `id, parent_id, name, client_id`, plus `plugin_name` (absent in old source) defaulted to `''`.

**Important:** Use `INSERT ... OVERRIDING SYSTEM VALUE` (PostgreSQL) or the destination DB's equivalent to preserve original `id` values in both tables, because `analytics.group_id` will reference them.

| Old column | `stream_groups` target | `analytics_groups` target |
|---|---|---|
| `id` | `id` | `id` |
| `parent_id` | `parent_id` | `parent_id` |
| `name` | `name` | `name` |
| `client_id` | `client_id` | `client_id` |
| *(absent)* | — | `plugin_name` -> `''` |

---

### 4.4 `analytics` -> `videoanalytics.analytics`

| Old column | New column | Transformation |
|---|---|---|
| `id` | `id` | Direct (preserve original ID) |
| `type` | `type` | Direct |
| `plugin_name` | `plugin_name` | Direct |
| `name` | `name` | Direct |
| `created_at` | `created_at` | Direct |
| `status` | `status` | Direct |
| `client_id` | `client_id` | Direct |
| `stream` | `stream` | Direct |
| `module` | `module` | Direct |
| `last_gpu_id` | `last_gpu_id` | Direct |
| `desired_server_id` | `desired_server_id` | Direct |
| `disable_balancing` (bit) | `disable_balancing` (bool) | Cast bit(1) -> bool |
| `start_signature` | `start_signature` | Direct |
| `allowed_server_ids` | `allowed_server_ids` | Direct |
| `restrictions` | `restrictions` | Direct |
| `events_holder` | `events_holder` | Direct |
| `start_at` | `start_at` | Direct |
| `stream_id` (int) | `stream_uuid` (uuid) | Resolve via `IdToUuidResolver`. If NULL or not found -> NULL; log WARN. |
| `topic` | *(dropped)* | No equivalent in new schema. |
| *(absent)* | `uuid` | Generate `UUID.randomUUID()` per row. |
| `stream_id` (int) | `group_id` (int) | Look up `streams.parent_id` WHERE `streams.id = analytics.stream_id`. That `parent_id` is the `stream_groups.id`, which equals the `analytics_groups.id` created in 4.3. If `stream_id` is NULL or stream not found -> default `group_id = 0`. |

**`group_id` resolution detail:**
```
streamGroupId = SELECT parent_id FROM streams WHERE id = analytics.stream_id
analytics.group_id = streamGroupId (or 0 if not found)
```

---

### 4.5 `users` -> `videoanalytics.users`

| Old column | New column | Transformation |
|---|---|---|
| All shared columns | same name | Direct copy |
| `role_id` (int) | `role_ids` (text) | Wrap as JSON array: `"[<role_id>]"`. If `role_id = 0` -> use `"[]"`. |

---

### 4.6 `roles` -> `videoanalytics.roles`

| Old column | New column | Transformation |
|---|---|---|
| `id, role_name, permissions, client_id` | same | Direct copy |
| *(absent)* | `description` | Default NULL |

---

### 4.7 `servers` -> `videoanalytics.servers`

| Old column | New column | Transformation |
|---|---|---|
| `id, name` | same | Direct copy |
| *(absent)* | `is_external_address_enabled` | Default false |
| *(absent)* | `address` | Default NULL |
| *(absent)* | `port` | Default NULL |

---

### 4.8 `audit_trail` -> `videoanalytics.audit_trail`

| Old column | New column | Transformation |
|---|---|---|
| `id` | `id` | Direct |
| `created_at` | `created_at` | Direct |
| `session_id` | `session_id` | Old NOT NULL -> new nullable. Direct copy. |
| `user_id` | `user_id` | Direct |
| `user_ip` | `user_ip` | Old NOT NULL -> new nullable. Direct copy. |
| `source_id` | `source_id` | Direct |
| `message` | `message` | Direct |
| `client_id` | `client_id` | Direct |
| `event_category` (int) | `event_category_id` (uuid) | Deterministic: `UUID.nameUUIDFromBytes(("event_category:" + value).getBytes(UTF_8))` |
| `event_action` (int) | `event_action_id` (uuid) | Deterministic: `UUID.nameUUIDFromBytes(("event_action:" + value).getBytes(UTF_8))` |
| `stream_id` | *(dropped)* | No equivalent in new schema. |
| `analytics_id` | *(dropped)* | No equivalent in new schema. |

---

### 4.9 `event_manager` -> `videoanalytics.event_manager`

Old PK `id` is a varchar UUID string. New table has serial int PK `id` (auto-generated) plus unique varchar column `uuid`.

| Old column | New column | Transformation |
|---|---|---|
| `id` (varchar PK) | `uuid` (varchar UNIQUE) | Copy old PK value to `uuid` column. |
| *(absent)* | `id` (serial) | Let DB auto-generate; do not insert. |
| `title, description, created_at, nodes, client_id` | same | Direct copy |

---

### 4.10 `settings` -> `videoanalytics.system_settings`

| Old column | New column | Transformation |
|---|---|---|
| `Variable_name` | `variable_name` | Direct copy (cosmetic column rename) |
| `Value` | `value` | Direct copy |
| *(absent)* | `id` (serial) | Let DB auto-generate; do not insert. |

---

### 4.11 `alpr_lists` -> `videoanalytics.alpr_lists`

| Old column | New column | Transformation |
|---|---|---|
| `id, name, comment, events_holder, status, created_at, color, client_id` | same | Direct copy |
| `streams` (nullable text, JSON array of int stream IDs) | `analytics_ids` (nullable text) | Resolve each stream_id to all matching analytics IDs via `StreamToAnalyticsResolver.getAll(sid)`; flatten to a single JSON int array. If old field is NULL or resolves to empty -> NULL. |
| `send_internal_notifications` (bit) | `send_internal_notifications` (bool) | Cast bit -> bool |
| `enabled` (bit) | `enabled` (bool) | Cast bit -> bool |
| `list_permissions` (nullable text) | `list_permissions` (NOT NULL text) | If NULL -> default `''` |
| *(absent)* | `show_popup_for_internal_notifications` | Default false |

**Resolution pseudocode:**
```java
List<Integer> streamIds = parseJsonIntArray(row.streams); // empty if null
List<Integer> analyticsIds = streamIds.stream()
    .flatMap(sid -> streamToAnalyticsResolver.getAll(sid).stream())
    .distinct().collect(toList());
String result = analyticsIds.isEmpty() ? null : toJsonArray(analyticsIds);
```

---

### 4.12 `alpr_list_items` -> `videoanalytics.alpr_list_items`
Direct copy. Schemas are identical.

---

### 4.13 `alpr_plates` -> `videoanalytics.alpr_detections`

| Old column | New column | Transformation |
|---|---|---|
| `id, plate_number, arabic_number, adr, box, plate_image, frame_image, make_model_id, vehicle_type, created_at, color_id, direction, country, pattern, client_id` | same | Direct copy |
| `stream_id` + `va_id` | `analytics_id` | `StreamToAnalyticsResolver.getFirstByPlugin(stream_id, "AlprAnalyticsModule")`. If not found -> use `va_id` as fallback and log WARN with row id. |
| `list_items` | *(dropped)* | No equivalent in new schema. |
| *(absent)* | `latitude` | Default 0 |
| *(absent)* | `longitude` | Default 0 |

---

### 4.14 `alpr_notifications` -> `videoanalytics.alpr_list_events`

New table is fully denormalised. Requires a JOIN with old `alpr_plates`.

| Source | New column | Transformation |
|---|---|---|
| `alpr_notifications.id` | `id` | Direct |
| `alpr_notifications.plate_id` | `detection_id` | Direct (FK rename) |
| `alpr_notifications.list_id, list_item_id, list_item_name, status, accepted_by, created_at` | same | Direct copy |
| `alpr_plates.plate_number, arabic_number, adr, box, plate_image, frame_image, make_model_id, vehicle_type, color_id, direction, country, pattern, client_id` | same | JOIN on `alpr_plates.id = alpr_notifications.plate_id` |
| `alpr_plates.stream_id` + `alpr_plates.va_id` | `analytics_id` | `StreamToAnalyticsResolver.getFirstByPlugin(stream_id, "AlprAnalyticsModule")`. Fallback: `va_id`. |
| *(absent in old plates)* | `latitude` | Default NULL |
| *(absent in old plates)* | `longitude` | Default NULL |

If no matching `alpr_plates` row exists for a notification -> log ERROR with notification `id` and skip that row.

---

### 4.15 `alpr_stats_hourly` -> `videoanalytics.alpr_hourly_statistics`

| Old column | New column | Transformation |
|---|---|---|
| `id, total, numbers, make_models, created_at, client_id` | same | Direct copy |
| `stream_id` (int) | `analytics_id` (int) | `StreamToAnalyticsResolver.getFirstByPlugin(stream_id, "AlprAnalyticsModule")`. If not found -> log WARN and skip row. |

---

### 4.16 `alpr_speed_rules` -> `videoanalytics.alpr_speed_rules`

| Old column | New column | Transformation |
|---|---|---|
| `id, name, speed_limit, speed_unit, distance, distance_unit, min_speed, min_speed_unit, max_duration, events_holder, client_id` | same | Direct copy |
| `stream_id1` | `analytics_id1` | `StreamToAnalyticsResolver.getFirstByPlugin(stream_id1, "AlprAnalyticsModule")`. If not found -> log WARN, use 0. |
| `stream_id2` | `analytics_id2` | `StreamToAnalyticsResolver.getFirstByPlugin(stream_id2, "AlprAnalyticsModule")`. If not found -> log WARN, use 0. |

---

### 4.17 `alpr_speed_rule_events` -> `videoanalytics.alpr_speed_rule_events`

**Major structural mismatch.** Old schema stores full per-event detail (plate data, frames, timestamps). New schema stores only rule reference, one speed value, and two detection FKs.

| Old column | New column | Transformation |
|---|---|---|
| `id` | `id` | Direct |
| `rule_id` | `rule_id` | Direct |
| `speed_limit` | `speed_value` | Rename. NOTE: old `speed_limit` is the rule's threshold copied per event; new `speed_value` is the measured speed. These are semantically different values. Log INFO once at start of this migrator. |
| `speed_unit` | `speed_unit` | Direct |
| *(no detection FKs in old)* | `detection1_id` | Default NULL — old schema has no link to individual detections. |
| *(no detection FKs in old)* | `detection2_id` | Default NULL. |
| `plate_number, make_model_id, color_id, direction, country, country_pattern, stream1_frame, stream2_frame, stream1_timestamp, stream2_timestamp, client_id` | *(all dropped)* | No equivalent in new schema. Log INFO once: these columns cannot be migrated and the data is lost. |

---

### 4.18 `traffic_stat` -> `videoanalytics.traffic_stat`

Old and new **both** have `stream_id` + `va_id`. No resolution needed.

| Old column | New column | Transformation |
|---|---|---|
| `id, stream_id, va_id, line, type, count, direction, created_at, client_id, frame_image, object_image, notification_status, accepted_by` | same | Direct copy |
| *(absent)* | `x1, y1, x2, y2` | Default 0 |
| *(absent)* | `confidence` | Default 0 |
| *(absent)* | `line_object` | Default NULL |
| *(absent)* | `latitude, longitude` | Default NULL |

---

### 4.19 `stats_traffic_hourly` + `stats_traffic_minutely` -> `videoanalytics.stats_traffic_minutely`

New schema has **no `stats_traffic_hourly` table**. Both old tables are merged into the single new `stats_traffic_minutely`.

Both old tables have identical columns: `id, va_id, line, type, count, direction, created_at, client_id, present`.
New `stats_traffic_minutely` has the same columns.

**ID collision warning:** Do NOT preserve original IDs — both old tables have independent auto-increment sequences whose values will collide. Let the destination DB generate new serial IDs for all rows. Log row counts from each source table.

No stream_id present in either old table — direct copy of all columns except `id`.

---

### 4.20 `gender_age_stat` -> `videoanalytics.gender_age_stat`

Old and new both have `stream_id` + `va_id`. Direct copy of all columns. Column `date` maps to `date`.

---

### 4.21 `gun_notifications` -> `videoanalytics.gun_notifications`

| Old column | New column | Transformation |
|---|---|---|
| `id, frame_image, thumbnail_image, objects, zone, created_at, client_id, status, accepted_by, va_id` | same | Direct copy |
| `stream_id` | *(dropped)* | No equivalent. |
| *(absent)* | `latitude, longitude` | Default NULL |

`gun_type_mapping` -> direct copy (schemas identical).

---

### 4.22 `hardhats_notifications` -> `videoanalytics.hardhats_notifications`

| Old column | New column | Transformation |
|---|---|---|
| `id, status, accepted_by, objects, va_id, frame_image, thumbnail_image, created_at, zone, client_id` | same | Direct copy |
| `stream_id` | *(dropped)* | No equivalent. |
| *(absent)* | `latitude, longitude` | Default NULL |

---

### 4.23 `smoke_fire_notifications` -> `videoanalytics.smoke_fire_notifications`

| Old column | New column | Transformation |
|---|---|---|
| `id, status, accepted_by, objects, va_id, frame_image, thumbnail_image, zone, created_at, client_id` | same | Direct copy |
| `stream_id` | *(dropped)* | No equivalent. |
| *(absent)* | `latitude, longitude` | Default NULL |

`smoke_fire_type_mapping` -> direct copy (schemas identical).

---

### 4.24 `object_in_zone_notifications` -> `videoanalytics.object_in_zone_notifications`

| Old column | New column | Transformation |
|---|---|---|
| `id, status, accepted_by, frame_image, thumbnail_image, zone, dwell_time, trigger, notification_type, action_type, resolution, created_at, client_id` | same | Direct copy |
| `va_id` | `analytics_id` | Direct copy (column renamed; semantically identical). |
| `stream_id` | *(dropped)* | No equivalent. |
| *(absent)* | `latitude, longitude` | Default NULL |

`object_in_zone_object_type` -> direct copy (schemas identical).

---

### 4.25 `zone_exit_notifications` -> `videoanalytics.zone_exit_notifications`

| Old column | New column | Transformation |
|---|---|---|
| `id, object_id, zone_id, seconds_in_zone, object_type, notification_type, created_at, client_id` | same | Direct copy |
| `va_id` | `analytics_id` | Direct copy (renamed). |
| `stream_id` | *(dropped)* | No equivalent. |

`zone_exit_notifications_object_type` -> direct copy (schemas identical).

---

### 4.26 `railroad_numbers` -> `videoanalytics.railroad_numbers`

| Old column | New column | Transformation |
|---|---|---|
| `id, number, box, number_image, frame_image, created_at, direction, client_id, iso_code` | same | Direct copy |
| `stream_id` | `analytics_id` | `StreamToAnalyticsResolver.getFirstByPlugin(stream_id, "RailroadsAnalyticsModule")`. If not found -> log WARN, skip row. |
| *(absent)* | `zone` | Default NULL |
| *(absent)* | `latitude, longitude` | Default NULL |
| *(absent)* | `average_character_height` | Default NULL |

---

### 4.27 `port_logistics_container_numbers` -> `videoanalytics.port_logistics_container_numbers`

| Old column | New column | Transformation |
|---|---|---|
| `id, number, iso, box, number_image, frame_image, detection_id` | same | Direct copy |
| `recognized_at` (nullable) | `recognized_at` (NOT NULL) | If NULL -> use CURRENT_TIMESTAMP; log WARN with row id. |
| *(absent)* | `analytics_id` | Default 0 |

---

### 4.28 `port_logistics_detections` -> `videoanalytics.port_logistics_detections`

| Old column | New column | Transformation |
|---|---|---|
| `id, trailer_number, trailer_arabic_number, trailer_box, trailer_plate_image, trailer_frame_image, adr, pattern, country, state, make_model_id, vehicle_type, color_id, client_id, rule_id, created_at, trailer_plate_recognized_at, back_plate_number, back_plate_arabic_number, back_plate_box, back_plate_image, back_plate_frame_image, back_plate_recognized_at` | same | Direct copy |
| `truck_number` (NOT NULL in old) | `truck_number` (nullable in new) | Direct copy |
| `truck_arabic_number` (NOT NULL) | `truck_arabic_number` (nullable) | Direct copy |
| `truck_box` (NOT NULL) | `truck_box` (nullable) | Direct copy |
| `truck_plate_image` (NOT NULL) | `truck_plate_image` (nullable) | Direct copy |
| `truck_frame_image` (NOT NULL) | `truck_frame_image` (nullable) | Direct copy |
| `front_plate_recognized_at` (NOT NULL in old) | `front_plate_recognized_at` (nullable in new) | Direct copy |
| *(absent)* | `direction` (varchar NOT NULL) | Default `'unknown'` |
| *(absent)* | `front_plate_angle_degrees` | Default NULL |
| *(absent)* | `back_plate_angle_degrees` | Default NULL |
| *(absent)* | `trailer_plate_angle_degrees` | Default NULL |
| *(absent)* | `overview_snapshots` | Default NULL |
| *(absent)* | `front_plate_analytics_id` | Default 0 |
| *(absent)* | `back_plate_analytics_id` | Default 0 |
| *(absent)* | `trailer_plate_analytics_id` | Default 0 |

---

### 4.29 `port_logistics_rules` -> `videoanalytics.port_logistics_rules`

| Old column | New column | Transformation |
|---|---|---|
| `id, name, container_analytics_ids, created_at, start_at, status, client_id, buffer_time` | same | Direct copy |
| `front_lpr_analytics_id` | `entry_lpr_analytics_id` | Rename (both are analytics IDs; no resolution needed) |
| `back_lpr_analytics_id` | `exit_lpr_analytics_id` | Rename |
| `buffet_time` (old typo field, NOT NULL) | *(dropped)* | No equivalent. |
| *(absent)* | `overview_stream_configs` (NOT NULL) | Default `''` |
| *(absent)* | `entry_lpr_analytics_angle_degrees` (NOT NULL) | Default 0 |
| *(absent)* | `exit_lpr_analytics_angle_degrees` (NOT NULL) | Default 0 |
| *(absent)* | `is_reverse_enabled` (NOT NULL) | Default false |
| *(absent)* | `group_id` (NOT NULL DEFAULT 0) | Default 0 |

Note: Old schema has no `port_logistics_rule_groups` — that table will be empty after migration.

---

### 4.30 `face_lists` -> `videoanalytics.face_lists` *(conditional)*

Only when `migration.tables.face_lists.enabled = true`.

| Old column | New column | Transformation |
|---|---|---|
| `id, name, comment, min_confidence, events_holder, status, created_at, client_id, color, time_attendance` | same | Direct copy |
| `send_internal_notifications` (bit) | `send_internal_notifications` (bool) | Cast bit -> bool |
| `streams` (nullable text, JSON array of stream IDs) | `analytics_ids` (nullable text) | Same resolution as 4.11 |
| `list_permissions` (nullable) | `list_permissions` (NOT NULL) | If NULL -> default `''` |
| `enabled` (bit) | *(dropped)* | New `face_lists` has no `enabled` column. |
| *(absent)* | `show_popup_for_internal_notifications` | Default false |

**`face_list_items` DB migration** (also conditional on face_lists enabled):

| Old column | New column | Transformation |
|---|---|---|
| `id, name, comment, status, created_at, created_by, closed_at, list_id, expiration_settings, client_id` | same | Direct copy |
| *(absent)* | `expiration_settings_enabled` (bool NOT NULL DEFAULT false) | Default false |
| *(absent)* | `expiration_settings_action` (varchar NOT NULL DEFAULT 'none') | Default `'none'` |
| *(absent)* | `expiration_settings_list_id` (nullable) | Default NULL |
| *(absent)* | `expiration_settings_date` (nullable) | Default NULL |
| *(absent)* | `expiration_settings_events_holder` (nullable) | Default NULL |

**`face_list_items_images` is NOT migrated to the DB.** Used only for file processing (Section 5).

---

### 4.31 Tables present only in new schema (will be empty after migration)

Log INFO for each:

| New table | Reason empty |
|---|---|
| `api_tokens` | New feature; no source. |
| `cleaning_settings` | New feature; no source. |
| `sounds_settings` | New feature; no source. |
| `plugin_configurations` | New feature; no source. |
| `port_logistics_rule_groups` | New concept; no source. |

---

### 4.32 Tables in old schema with no migration target

| Old table | Action |
|---|---|
| `traffic_lights_detections` | Skip. Absent in new schema. Log INFO. |

---

## 5. Face Image Processing

Runs only when `migration.tables.face_lists.enabled = true`, after all DB migrations complete.

### Step 1 — Load data

```sql
SELECT fli.id        AS item_id,
       fli.name      AS person_name,
       fli.list_id,
       flii.id       AS image_id,
       flii.path     AS image_path
FROM face_list_items fli
JOIN face_list_items_images flii ON flii.list_item_id = fli.id
ORDER BY flii.id ASC;

SELECT id, name FROM face_lists;
```

### Step 2 — Build maps

- Extract filename from `image_path`: substring after last `/`. Example: `"face_lists/0/0/1725440323601.jpg"` -> `"1725440323601.jpg"`.
- `Map<Integer, String> itemIdToFirstImage`: keyed by `item_id`; insert only if absent (lowest `image_id` = first image wins).
- `Map<Integer, String> itemIdToPersonName`
- `Map<Integer, Integer> itemIdToListId`
- `Map<Integer, String> listIdToName`

### Step 3 — Sanitise names

Replace all chars in `\ / : * ? " < > |` with `_` in both person names and list names.

Handle collisions **per list**: if two items in the same list produce the same sanitised name, append `_<item_id>` to each colliding entry.

### Step 4 — Rename images in `imageFolderPath`

For each entry in `itemIdToFirstImage`:
- Source: `imageFolderPath / originalFilename`
- Target: `imageFolderPath / (sanitisedPersonName + extension)`
- If source missing -> log WARN, skip.
- If target already exists -> log WARN, skip.
- `Files.move(source, target, StandardCopyOption.ATOMIC_MOVE)`

### Step 5 — Create `Lists/` folders

```
listsRoot = <jar_directory> / "Lists"
```

For each list referenced by any `face_list_items` row: create `listsRoot / sanitisedListName /`.

### Step 6 — Move images into list folders

For each `item_id` that was successfully renamed:
- Move `imageFolderPath / renamedFilename` -> `listsRoot / sanitisedListName / renamedFilename`.
- If source missing (skipped in step 4) -> log WARN, skip.

### Edge Cases

| Case | Handling |
|---|---|
| Person has no images | Skip. |
| Multiple images per person | Only lowest `face_list_items_images.id` used. |
| Sanitised name collision within a list | Append `_<item_id>` to each colliding name. |
| Image file not on disk | Log WARN, skip. |
| Target file already exists | Log WARN, skip. |

---

## 6. Migration Execution Order

```
 1.  clients
 2.  servers
 3.  roles
 4.  streams
 5.  stream_groups          <- also writes analytics_groups
 6.  analytics
 7.  users
 8.  audit_trail
 9.  event_manager
10.  settings               -> system_settings
11.  alpr_lists
12.  alpr_list_items
13.  alpr_detections        <- was alpr_plates
14.  alpr_list_events       <- was alpr_notifications, JOINs alpr_plates
15.  alpr_hourly_statistics <- was alpr_stats_hourly
16.  alpr_speed_rules
17.  alpr_speed_rule_events
18.  traffic_stat
19.  stats_traffic          <- merges stats_traffic_hourly + stats_traffic_minutely
20.  gender_age_stat
21.  gun_notifications
22.  gun_type_mapping
23.  hardhats_notifications
24.  smoke_fire_notifications
25.  smoke_fire_type_mapping
26.  object_in_zone_notifications
27.  object_in_zone_object_type
28.  zone_exit_notifications
29.  zone_exit_notifications_object_type
30.  railroad_numbers
31.  port_logistics_container_numbers
32.  port_logistics_detections
33.  port_logistics_rules
34.  face_lists             <- conditional
35.  face_list_items        <- conditional (DB only)
36.  Face image processing  <- conditional
37.  Sequence reset         <- always, see 7.1
```

---

## 7. Batching & Performance

- JDBC `PreparedStatement` with `addBatch()` / `executeBatch()`. Batch size from config (default 1000).
- Each migrator in a single transaction; batch failure rolls back whole table, logs ERROR with row range.
- Resolvers loaded once before any migration runs.
- Large tables use keyset pagination: `WHERE id > :lastId ORDER BY id LIMIT :batchSize`.
- `alpr_list_events` paginates by `alpr_notifications.id`.

---

## 7.1 Post-Migration Sequence Reset

After all migrators complete, the `MigrationOrchestrator` resets identity sequences for every table where original IDs were preserved, so that the next real insert does not collide.

**PostgreSQL destination** — query `MAX(id)` in Java, then call `setval`:

```java
long maxId = jdbcTemplate.queryForObject(
    "SELECT COALESCE(MAX(id), 0) FROM videoanalytics." + table, Long.class);
jdbcTemplate.execute(
    "SELECT setval(pg_get_serial_sequence('videoanalytics." + table + "', 'id'), " + maxId + ")");
```

**MySQL destination** — query `MAX(id)` in Java, then execute a literal `ALTER TABLE`:

```java
long maxId = jdbcTemplate.queryForObject(
    "SELECT COALESCE(MAX(id), 0) FROM " + table, Long.class);
jdbcTemplate.execute(
    "ALTER TABLE " + table + " AUTO_INCREMENT = " + (maxId + 1));
```

Apply to every table **except** these three, where IDs were not preserved during migration:
- `event_manager` — serial `id` was auto-generated; no reset needed.
- `system_settings` — same.
- `stats_traffic_minutely` — IDs were not preserved during the merge of two old tables.

---

## 8. Logging

- SLF4J + Logback. Log file: `migration.log` in the jar directory.
- Startup: active source, active destination, enabled tables list.
- Per table: start, rows read, rows inserted, rows skipped/warned, duration.
- Resolution warnings include table name, row id, field name, unresolved value.
- Finish: summary of migrated / skipped / failed / empty tables.

---

## 9. Unit Testing (100% business logic coverage)

Use Mockito for DB calls; H2 for integration-level tests. No Docker/Testcontainers.

| Test class | Key scenarios |
|---|---|
| `AppConfigTest` | Valid config; no enabled source -> throws; no enabled dest -> throws; two enabled sources -> throws; face_lists enabled without path -> throws; path not existing -> throws. |
| `DataSourceFactoryTest` | Correct JDBC URL for each DB type. |
| `IdToUuidResolverTest` | Known ID -> correct UUID; unknown ID -> empty Optional. |
| `StreamToAnalyticsResolverTest` | getAll returns full list for stream; getAll returns empty for unknown stream; getFirstByPlugin returns correct id when multiple plugins on same stream; getFirstByPlugin returns empty when plugin not present on that stream; WARN logged on plugin miss. |
| `BatchInserterTest` | Flush at batchSize; flush remainder on close; exception -> rollback + rethrow. |
| `StreamGroupsMigratorTest` | Each source row produces one insert to stream_groups AND one to analytics_groups; plugin_name = ''. |
| `AnalyticsMigratorTest` | stream_id -> stream_uuid via IdToUuidResolver; NULL stream_id -> NULL + WARN; disable_balancing bit->bool; topic not inserted; uuid generated per row; group_id from streams.parent_id; missing stream -> group_id=0. |
| `UsersMigratorTest` | role_id=5 -> "[5]"; role_id=0 -> "[]". |
| `AuditTrailMigratorTest` | event_category int -> UUID deterministic; event_action int -> UUID deterministic; stream_id and analytics_id not inserted; session_id and user_ip copied. |
| `EventManagerMigratorTest` | Old varchar id -> uuid column; serial id not set. |
| `SettingsMigratorTest` | Variable_name -> variable_name; Value -> value; id not set. |
| `AlprListsMigratorTest` | streams JSON resolved to analytics_ids via getAll; NULL streams -> NULL; bit->bool; null list_permissions -> ''; show_popup defaulted. |
| `AlprDetectionsMigratorTest` | stream_id+va_id->analytics_id via getFirstByPlugin("AlprAnalyticsModule"); fallback to va_id on miss+WARN; lat/lng default 0; list_items not inserted. |
| `AlprListEventsMigratorTest` | JOIN with alpr_plates; plate columns populated; analytics_id resolved via getFirstByPlugin("AlprAnalyticsModule"); fallback to va_id; missing plate -> ERROR+skip; lat/lng NULL. |
| `AlprHourlyStatsMigratorTest` | stream_id->analytics_id via getFirstByPlugin("AlprAnalyticsModule"); not found->WARN+skip. |
| `AlprSpeedRulesMigratorTest` | stream_id1/2->analytics_id1/2 via getFirstByPlugin("AlprAnalyticsModule"); not found->WARN+default 0. |
| `AlprSpeedRuleEventsMigratorTest` | speed_limit->speed_value; detection ids NULL; dropped columns absent; INFO logged once about semantic difference and data loss. |
| `TrafficStatMigratorTest` | stream_id+va_id both kept; absent columns default 0/NULL. |
| `StatsTrafficMigratorTest` | Both old tables merged; original IDs NOT preserved (auto-generated); row counts per source logged. |
| `GenderAgeStatMigratorTest` | stream_id+va_id both kept; direct copy. |
| `GunNotificationsMigratorTest` | stream_id dropped; va_id kept; lat/lng NULL. |
| `HardhatsNotificationsMigratorTest` | Same as gun. |
| `SmokeFireNotificationsMigratorTest` | Same as gun. |
| `ObjectInZoneMigratorTest` | va_id->analytics_id; stream_id dropped; lat/lng NULL. |
| `ZoneExitNotificationsMigratorTest` | va_id->analytics_id; stream_id dropped. |
| `RailroadNumbersMigratorTest` | stream_id->analytics_id via getFirstByPlugin("RailroadsAnalyticsModule"); not found->WARN+skip; absent columns NULL. |
| `PortLogisticsContainerNumbersMigratorTest` | NULL recognized_at->CURRENT_TIMESTAMP+WARN; analytics_id default 0. |
| `PortLogisticsDetectionsMigratorTest` | direction defaulted 'unknown'; absent cols defaulted; NOT NULL old columns copy cleanly. |
| `PortLogisticsRulesMigratorTest` | front/back->entry/exit rename; buffet_time dropped; absent cols defaulted. |
| `FaceListsMigratorTest` | streams->analytics_ids via getAll; enabled not inserted; bit->bool; null list_permissions->''; show_popup default. |
| `FaceListItemsMigratorTest` | All old columns copied; new expiration sub-columns default correctly. |
| `FaceImageOrganizerTest` | Path strip (face_lists/0/0/x.jpg->x.jpg); first-image-wins; rename to sanitised name; list folder created; moved to correct list; missing file->WARN+skip; target exists->WARN+skip; illegal chars->_; collision within list->append _item_id; person with no image->skip. |
| `MigrationOrchestratorTest` | Correct execution order; disabled table skipped; per-table exception caught+logged, does not abort others; new-only tables logged INFO; sequence reset called for each applicable table after all migrators complete; event_manager, system_settings, stats_traffic_minutely excluded from sequence reset. |

---

## 10. README.md Requirements

1. Prerequisites (Java 17+).
2. How to run: `java -jar db-migrator.jar` (config.json must be in same folder).
3. Annotated `config.json` example.
4. **Source DB extraction queries** — per DB type, verify row counts and key linkages before running:

**MySQL:**
```sql
-- Row counts
SELECT table_name, table_rows
FROM information_schema.tables
WHERE table_schema = 'videoanalytics';

-- Unresolvable analytics (will lose stream linkage)
SELECT COUNT(*) AS unresolvable_analytics
FROM analytics WHERE stream_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM streams WHERE id = analytics.stream_id);

-- Orphan alpr_plates (no stream)
SELECT COUNT(*) AS unresolvable_plates
FROM alpr_plates
WHERE NOT EXISTS (SELECT 1 FROM streams WHERE id = alpr_plates.stream_id);

-- Orphan alpr_notifications (no plate)
SELECT COUNT(*) AS orphan_notifications
FROM alpr_notifications
WHERE NOT EXISTS (SELECT 1 FROM alpr_plates WHERE id = alpr_notifications.plate_id);

-- Speed rule events count (detail will be lost)
SELECT COUNT(*) AS speed_events_losing_detail FROM alpr_speed_rule_events;
```

**PostgreSQL (as source):**
```sql
SELECT schemaname, tablename, n_live_tup AS approx_rows
FROM pg_stat_user_tables
WHERE schemaname = 'videoanalytics'
ORDER BY tablename;

SELECT COUNT(*) AS unresolvable_analytics
FROM videoanalytics.analytics a
WHERE a.stream_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM videoanalytics.streams s WHERE s.id = a.stream_id);
```

**MSSQL:**
```sql
SELECT t.name, p.rows
FROM sys.tables t JOIN sys.partitions p ON t.object_id = p.object_id
WHERE p.index_id IN (0,1);

SELECT COUNT(*) AS unresolvable_plates
FROM alpr_plates ap
WHERE NOT EXISTS (SELECT 1 FROM streams s WHERE s.id = ap.stream_id);
```

**Oracle:**
```sql
SELECT table_name, num_rows FROM user_tables ORDER BY table_name;

SELECT COUNT(*) AS unresolvable_plates
FROM alpr_plates ap
WHERE NOT EXISTS (SELECT 1 FROM streams s WHERE s.id = ap.stream_id);
```

**H2:**
```sql
SELECT TABLE_NAME, ROW_COUNT_ESTIMATE
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'PUBLIC';
```

5. **Data loss section** — explicitly documents:
    - `alpr_speed_rule_events`: plate/frame/timestamp detail lost; `speed_value` contains old rule threshold, not measured speed.
    - `traffic_lights_detections`: skipped entirely (no target table).
    - `port_logistics_rule_groups`, `api_tokens`, `cleaning_settings`, `sounds_settings`, `plugin_configurations`: empty after migration.
    - `analytics_groups.plugin_name` will be blank for all migrated rows.
6. Face image migration guide with before/after folder structure example.
7. Troubleshooting section.

---

## 11. Dependencies (pom.xml)

```xml
spring-boot-starter-jdbc
spring-boot-starter-logging
mysql:mysql-connector-java
org.postgresql:postgresql
com.microsoft.sqlserver:mssql-jdbc
com.oracle.database.jdbc:ojdbc11
com.h2database:h2
com.fasterxml.jackson.core:jackson-databind
org.springframework.boot:spring-boot-starter-test
org.mockito:mockito-core
```

---

## 12. Token-Saving Subtask Breakdown for AI Agent

Subtask 1: ConfigModel, AppConfig, DataSourceFactory, IdToUuidResolver, StreamToAnalyticsResolver, BatchInserter, MigrationOrchestrator (shell + sequence reset logic) + all corresponding tests.
Subtask 2: All simple direct migrators: clients, streams, roles, servers, event_manager, settings, alpr_list_items, gun_type_mapping, smoke_fire_type_mapping, object_in_zone_object_type, zone_exit_notifications_object_type + all corresponding tests.
Subtask 3: stream_groups (dual-write to both stream_groups and analytics_groups), analytics, users, audit_trail + all corresponding tests.
Subtask 4: All ALPR migrators: alpr_lists, alpr_detections, alpr_list_events, alpr_hourly_statistics, alpr_speed_rules, alpr_speed_rule_events + all corresponding tests.
Subtask 5: All remaining migrators: traffic_stat, stats_traffic (merge of two old tables), gender_age_stat, all notification migrators (gun_notifications, hardhats_notifications, smoke_fire_notifications, object_in_zone_notifications, zone_exit_notifications), railroad_numbers, all port logistics migrators (port_logistics_container_numbers, port_logistics_detections, port_logistics_rules) + all corresponding tests.
Subtask 6: face_lists, face_list_items, FaceImageOrganizer, SqlFileReader, wiring in MigratorApplication, README + all corresponding tests.

**General tips:**
- Maintain a `AGENTS.md` in the repo root with: package structure, resolver API signatures, key config model fields. AI Agent reads this automatically without consuming prompt tokens.
- Start each subtask prompt with: "Existing classes: [list]. Implement: [list]. Spec context: [paste relevant sections only]."
- Never implement more than 5-6 migrators per session.
- For tests always specify: "Use Mockito for DB. Use H2 for integration. No Docker."
- After each subtask, commit and summarise new classes added to paste into the next session's context.
