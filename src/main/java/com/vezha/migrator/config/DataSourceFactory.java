package com.vezha.migrator.config;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {

    public DataSource forSource(ConfigModel.SourceConfig source) {
        if (source.getMysql().isEnabled()) {
            return buildMySql(source.getMysql());
        }
        if (source.getPostgres().isEnabled()) {
            return buildPostgres(source.getPostgres());
        }
        if (source.getMssql().isEnabled()) {
            return buildMsSql(source.getMssql());
        }
        if (source.getOracle().isEnabled()) {
            return buildOracle(source.getOracle());
        }
        if (source.getH2().isEnabled()) {
            return buildH2(source.getH2());
        }
        throw new IllegalArgumentException("No JDBC source enabled (sqlFile is not a JDBC source)");
    }

    public DataSource forDestination(ConfigModel.DestinationConfig destination) {
        if (destination.getPostgres().isEnabled()) {
            return buildPostgres(destination.getPostgres());
        }
        if (destination.getMysql().isEnabled()) {
            return buildMySql(destination.getMysql());
        }
        throw new IllegalArgumentException("No destination JDBC configuration enabled");
    }

    private DataSource buildMySql(ConfigModel.DbConnectionConfig config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:mysql://%s:%d/%s".formatted(config.getHost(), config.getPort(), config.getDatabase()));
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }

    private DataSource buildPostgres(ConfigModel.PostgresConfig config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://%s:%d/%s?currentSchema=%s".formatted(
                config.getHost(), config.getPort(), config.getDatabase(), config.getSchema()));
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }

    private DataSource buildMsSql(ConfigModel.DbConnectionConfig config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:sqlserver://%s:%d;databaseName=%s".formatted(config.getHost(), config.getPort(), config.getDatabase()));
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }

    private DataSource buildOracle(ConfigModel.OracleConfig config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:oracle:thin:@%s:%d/%s".formatted(config.getHost(), config.getPort(), config.getServiceName()));
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }

    private DataSource buildH2(ConfigModel.H2Config config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(config.getUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }
}
