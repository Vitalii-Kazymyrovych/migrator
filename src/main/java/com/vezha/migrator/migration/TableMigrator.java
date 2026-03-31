package com.vezha.migrator.migration;

public interface TableMigrator {
    String tableName();
    void migrate();
}
