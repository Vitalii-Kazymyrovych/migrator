package com.vezha.migrator.migration;

import java.util.List;

public interface TableMigrator {
    String tableName();

    default String getTargetTable() {
        return tableName();
    }

    default List<String> getSourceTables() {
        return List.of(tableName());
    }

    void migrate();
}
