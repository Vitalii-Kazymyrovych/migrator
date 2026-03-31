# VideoAnalytics DB Migrator

Java 17+ CLI tool for migrating VideoAnalytics data from legacy schemas into the new schema.

## Prerequisites

- Java 17 or newer.

## Run

Place `config.json` in the same folder as `db-migrator.jar`, then run:

```bash
java -jar db-migrator.jar
```

## `config.json` example (annotated)

```json
{
  "source": {
    "sqlFile": {
      "enabled": false,
      "path": "./source.sql" // used only when source.sqlFile.enabled=true
    },
    "mysql": {
      "enabled": true,
      "host": "127.0.0.1",
      "port": 3306,
      "database": "videoanalytics",
      "username": "root",
      "password": "secret"
    },
    "postgres": {
      "enabled": false,
      "host": "127.0.0.1",
      "port": 5432,
      "database": "videoanalytics",
      "schema": "videoanalytics",
      "username": "postgres",
      "password": "secret"
    },
    "mssql": {
      "enabled": false,
      "host": "127.0.0.1",
      "port": 1433,
      "database": "videoanalytics",
      "username": "sa",
      "password": "secret"
    },
    "oracle": {
      "enabled": false,
      "host": "127.0.0.1",
      "port": 1521,
      "serviceName": "xe",
      "username": "system",
      "password": "secret"
    },
    "h2": {
      "enabled": false,
      "url": "jdbc:h2:~/videoanalytics",
      "username": "sa",
      "password": ""
    }
  },
  "destination": {
    "postgres": {
      "enabled": true,
      "host": "127.0.0.1",
      "port": 5432,
      "database": "videoanalytics_new",
      "schema": "videoanalytics",
      "username": "postgres",
      "password": "secret"
    },
    "mysql": {
      "enabled": false,
      "host": "127.0.0.1",
      "port": 3306,
      "database": "videoanalytics_new",
      "username": "root",
      "password": "secret"
    }
  },
  "migration": {
    "batchSize": 1000,
    "tables": {
      "clients": {"enabled": true},
      "streams": {"enabled": true},
      "face_lists": {
        "enabled": true,
        "imageFolderPath": "/opt/videoanalytics/face-images" // required if face_lists enabled
      }
    }
  }
}
```

## Source DB extraction and pre-check queries

Use the following checks to verify row counts and key linkages before running migration.

### MySQL

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

### PostgreSQL (as source)

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

### MSSQL

```sql
SELECT t.name, p.rows
FROM sys.tables t JOIN sys.partitions p ON t.object_id = p.object_id
WHERE p.index_id IN (0,1);

SELECT COUNT(*) AS unresolvable_plates
FROM alpr_plates ap
WHERE NOT EXISTS (SELECT 1 FROM streams s WHERE s.id = ap.stream_id);
```

### Oracle

```sql
SELECT table_name, num_rows FROM user_tables ORDER BY table_name;

SELECT COUNT(*) AS unresolvable_plates
FROM alpr_plates ap
WHERE NOT EXISTS (SELECT 1 FROM streams s WHERE s.id = ap.stream_id);
```

### H2

```sql
SELECT TABLE_NAME, ROW_COUNT_ESTIMATE
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'PUBLIC';
```
