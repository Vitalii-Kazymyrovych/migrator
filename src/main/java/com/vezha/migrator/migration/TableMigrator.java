package com.vezha.migrator.migration;

public interface TableMigrator {
    String tableName();

    default String getTargetTable() {
        return tableName();
    }

    void migrate();
}
