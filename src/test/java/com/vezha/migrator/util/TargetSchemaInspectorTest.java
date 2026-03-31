package com.vezha.migrator.util;

import com.vezha.migrator.config.ConfigModel;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TargetSchemaInspectorTest {

    @Test
    void tableExistsReturnsTrueForPresentTable() {
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'videoanalytics'",
                String.class
        )).thenReturn(List.of("clients", "stream_groups"));

        TargetSchemaInspector inspector = new TargetSchemaInspector(destinationJdbc);
        inspector.load(postgresConfig("videoanalytics"));

        assertTrue(inspector.tableExists("STREAM_GROUPS"));
    }

    @Test
    void tableExistsReturnsFalseForAbsentTable() {
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'videoanalytics'",
                String.class
        )).thenReturn(List.of("clients"));

        TargetSchemaInspector inspector = new TargetSchemaInspector(destinationJdbc);
        inspector.load(postgresConfig("videoanalytics"));

        assertFalse(inspector.tableExists("stream_groups"));
    }

    @Test
    void usesPostgresAndMysqlQueryVariants() {
        JdbcTemplate destinationJdbc = mock(JdbcTemplate.class);
        when(destinationJdbc.queryForList(
                eq("SELECT table_name FROM information_schema.tables WHERE table_schema = 'videoanalytics'"),
                eq(String.class)
        )).thenReturn(List.of("clients"));
        when(destinationJdbc.queryForList(
                eq("SELECT table_name FROM information_schema.tables WHERE table_schema = 'videoanalytics_new'"),
                eq(String.class)
        )).thenReturn(List.of("clients"));

        TargetSchemaInspector inspector = new TargetSchemaInspector(destinationJdbc);
        inspector.load(postgresConfig("videoanalytics"));
        inspector.load(mysqlConfig("videoanalytics_new"));

        verify(destinationJdbc).queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'videoanalytics'",
                String.class
        );
        verify(destinationJdbc).queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'videoanalytics_new'",
                String.class
        );
    }

    private ConfigModel postgresConfig(String schema) {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());
        model.getDestination().getPostgres().setEnabled(true);
        model.getDestination().getPostgres().setSchema(schema);
        return model;
    }

    private ConfigModel mysqlConfig(String database) {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());
        model.getDestination().getMysql().setEnabled(true);
        model.getDestination().getMysql().setDatabase(database);
        return model;
    }
}
