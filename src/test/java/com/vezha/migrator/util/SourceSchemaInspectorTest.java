package com.vezha.migrator.util;

import com.vezha.migrator.config.ConfigModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SourceSchemaInspectorTest {

    @TempDir
    Path tempDir;

    @Test
    void tableExistsReturnsTrueForPresentTable() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        when(sourceJdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'legacy'",
                String.class
        )).thenReturn(List.of("clients", "stream_groups"));

        SourceSchemaInspector inspector = new SourceSchemaInspector(sourceJdbc);
        inspector.load(mysqlConfig("legacy"));

        assertTrue(inspector.tableExists("STREAM_GROUPS"));
    }

    @Test
    void tableExistsReturnsFalseForAbsentTable() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        when(sourceJdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'legacy'",
                String.class
        )).thenReturn(List.of("clients"));

        SourceSchemaInspector inspector = new SourceSchemaInspector(sourceJdbc);
        inspector.load(mysqlConfig("legacy"));

        assertFalse(inspector.tableExists("stream_groups"));
    }

    @Test
    void usesCorrectSqlPerDbType() {
        JdbcTemplate sourceJdbc = mock(JdbcTemplate.class);
        when(sourceJdbc.queryForList(eq("SELECT table_name FROM information_schema.tables WHERE table_schema = 'mysql_db'"), eq(String.class)))
                .thenReturn(List.of("clients"));
        when(sourceJdbc.queryForList(eq("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"), eq(String.class)))
                .thenReturn(List.of("clients"));
        when(sourceJdbc.queryForList(eq("SELECT table_name FROM information_schema.tables WHERE table_catalog = 'mssql_db'"), eq(String.class)))
                .thenReturn(List.of("clients"));
        when(sourceJdbc.queryForList(eq("SELECT table_name FROM user_tables"), eq(String.class)))
                .thenReturn(List.of("CLIENTS"));
        when(sourceJdbc.queryForList(eq("SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC'"), eq(String.class)))
                .thenReturn(List.of("CLIENTS"));

        SourceSchemaInspector inspector = new SourceSchemaInspector(sourceJdbc);
        inspector.load(mysqlConfig("mysql_db"));
        inspector.load(postgresConfig("public"));
        inspector.load(mssqlConfig("mssql_db"));
        inspector.load(oracleConfig());
        inspector.load(h2Config());

        verify(sourceJdbc).queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = 'mysql_db'", String.class);
        verify(sourceJdbc).queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);
        verify(sourceJdbc).queryForList("SELECT table_name FROM information_schema.tables WHERE table_catalog = 'mssql_db'", String.class);
        verify(sourceJdbc).queryForList("SELECT table_name FROM user_tables", String.class);
        verify(sourceJdbc).queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC'", String.class);
    }

    @Test
    void parsesTableNamesFromSqlFileCreateStatements() throws Exception {
        Path sourceSql = Path.of("source.sql");
        Files.writeString(sourceSql,
                "CREATE TABLE clients (id INT);\n"
                        + "create table IF NOT EXISTS `alpr_notifications` (id INT);\n"
                        + "CREATE TABLE analytics.alpr_plates (id INT);\n"
                        + "CREATE TABLE \"stats_traffic_hourly\" (id INT);\n");

        try {
            SourceSchemaInspector inspector = new SourceSchemaInspector(mock(JdbcTemplate.class));
            inspector.load(sqlFileConfig());

            assertTrue(inspector.tableExists("clients"));
            assertTrue(inspector.tableExists("alpr_notifications"));
            assertTrue(inspector.tableExists("alpr_plates"));
            assertTrue(inspector.tableExists("stats_traffic_hourly"));
            assertFalse(inspector.tableExists("missing_table"));
        } finally {
            Files.deleteIfExists(sourceSql);
        }
    }

    private ConfigModel mysqlConfig(String database) {
        ConfigModel model = baseConfig();
        model.getSource().getMysql().setEnabled(true);
        model.getSource().getMysql().setDatabase(database);
        return model;
    }

    private ConfigModel postgresConfig(String schema) {
        ConfigModel model = baseConfig();
        model.getSource().getPostgres().setEnabled(true);
        model.getSource().getPostgres().setSchema(schema);
        return model;
    }

    private ConfigModel mssqlConfig(String database) {
        ConfigModel model = baseConfig();
        model.getSource().getMssql().setEnabled(true);
        model.getSource().getMssql().setDatabase(database);
        return model;
    }

    private ConfigModel oracleConfig() {
        ConfigModel model = baseConfig();
        model.getSource().getOracle().setEnabled(true);
        return model;
    }

    private ConfigModel h2Config() {
        ConfigModel model = baseConfig();
        model.getSource().getH2().setEnabled(true);
        return model;
    }

    private ConfigModel sqlFileConfig() {
        ConfigModel model = baseConfig();
        model.getSource().getSqlFile().setEnabled(true);
        return model;
    }

    private ConfigModel baseConfig() {
        ConfigModel model = new ConfigModel();
        model.setMigration(new ConfigModel.MigrationConfig());
        model.getMigration().setTables(new LinkedHashMap<>());
        return model;
    }
}
