package com.vezha.migrator.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigModel {

    private SourceConfig source = new SourceConfig();
    private DestinationConfig destination = new DestinationConfig();
    private MigrationConfig migration = new MigrationConfig();

    public SourceConfig getSource() {
        return source;
    }

    public void setSource(SourceConfig source) {
        this.source = source;
    }

    public DestinationConfig getDestination() {
        return destination;
    }

    public void setDestination(DestinationConfig destination) {
        this.destination = destination;
    }

    public MigrationConfig getMigration() {
        return migration;
    }

    public void setMigration(MigrationConfig migration) {
        this.migration = migration;
    }

    public static class SourceConfig {
        private SqlFileConfig sqlFile = new SqlFileConfig();
        private DbConnectionConfig mysql = new DbConnectionConfig();
        private PostgresConfig postgres = new PostgresConfig();
        private DbConnectionConfig mssql = new DbConnectionConfig();
        private OracleConfig oracle = new OracleConfig();
        private H2Config h2 = new H2Config();

        public SqlFileConfig getSqlFile() { return sqlFile; }
        public void setSqlFile(SqlFileConfig sqlFile) { this.sqlFile = sqlFile; }
        public DbConnectionConfig getMysql() { return mysql; }
        public void setMysql(DbConnectionConfig mysql) { this.mysql = mysql; }
        public PostgresConfig getPostgres() { return postgres; }
        public void setPostgres(PostgresConfig postgres) { this.postgres = postgres; }
        public DbConnectionConfig getMssql() { return mssql; }
        public void setMssql(DbConnectionConfig mssql) { this.mssql = mssql; }
        public OracleConfig getOracle() { return oracle; }
        public void setOracle(OracleConfig oracle) { this.oracle = oracle; }
        public H2Config getH2() { return h2; }
        public void setH2(H2Config h2) { this.h2 = h2; }
    }

    public static class DestinationConfig {
        private PostgresConfig postgres = new PostgresConfig();
        private DbConnectionConfig mysql = new DbConnectionConfig();

        public PostgresConfig getPostgres() { return postgres; }
        public void setPostgres(PostgresConfig postgres) { this.postgres = postgres; }
        public DbConnectionConfig getMysql() { return mysql; }
        public void setMysql(DbConnectionConfig mysql) { this.mysql = mysql; }
    }

    public static class MigrationConfig {
        private int batchSize = 1000;
        private Map<String, TableConfig> tables = new LinkedHashMap<>();

        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
        public Map<String, TableConfig> getTables() { return tables; }
        public void setTables(Map<String, TableConfig> tables) { this.tables = tables; }
    }

    public static class TableConfig {
        private boolean enabled;
        private String imageFolderPath;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getImageFolderPath() { return imageFolderPath; }
        public void setImageFolderPath(String imageFolderPath) { this.imageFolderPath = imageFolderPath; }
    }

    public static class ConnectionConfig {
        private boolean enabled;
        private String username;
        private String password;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class DbConnectionConfig extends ConnectionConfig {
        private String host;
        private Integer port;
        private String database;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
    }

    public static class PostgresConfig extends DbConnectionConfig {
        private String schema;

        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }
    }

    public static class OracleConfig extends ConnectionConfig {
        private String host;
        private Integer port;
        private String serviceName;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    }

    public static class H2Config extends ConnectionConfig {
        private String url;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class SqlFileConfig {
        private boolean enabled;
        private String path;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}
