package com.vezha.migrator.util;

import com.vezha.migrator.config.ConfigModel;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TargetSchemaInspector {

    private final JdbcTemplate destinationJdbcTemplate;
    private Set<String> tables = Set.of();

    public TargetSchemaInspector(JdbcTemplate destinationJdbcTemplate) {
        this.destinationJdbcTemplate = destinationJdbcTemplate;
    }

    public void load(ConfigModel configModel) {
        String query;
        if (configModel.getDestination().getPostgres().isEnabled()) {
            String schema = configModel.getDestination().getPostgres().getSchema();
            query = "SELECT table_name FROM information_schema.tables WHERE table_schema = '" + schema + "'";
        } else if (configModel.getDestination().getMysql().isEnabled()) {
            String database = configModel.getDestination().getMysql().getDatabase();
            query = "SELECT table_name FROM information_schema.tables WHERE table_schema = '" + database + "'";
        } else {
            tables = Set.of();
            return;
        }

        List<String> tableNames = destinationJdbcTemplate.queryForList(query, String.class);
        Set<String> loaded = new HashSet<>();
        for (String tableName : tableNames) {
            loaded.add(tableName.toLowerCase(Locale.ROOT));
        }
        tables = loaded;
    }

    public boolean tableExists(String tableName) {
        return tables.contains(tableName.toLowerCase(Locale.ROOT));
    }
}
