package com.vezha.migrator.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DataSourceFactoryTest {

    private final DataSourceFactory factory = new DataSourceFactory();

    @Test
    void buildsMysqlUrl() {
        ConfigModel.SourceConfig source = new ConfigModel.SourceConfig();
        source.getMysql().setEnabled(true);
        source.getMysql().setHost("localhost");
        source.getMysql().setPort(3306);
        source.getMysql().setDatabase("videoanalytics");
        source.getMysql().setUsername("root");
        source.getMysql().setPassword("secret");

        DriverManagerDataSource ds = assertDataSource(factory.forSource(source));
        assertEquals("jdbc:mysql://localhost:3306/videoanalytics", ds.getUrl());
    }

    @Test
    void buildsPostgresUrl() {
        ConfigModel.SourceConfig source = new ConfigModel.SourceConfig();
        source.getPostgres().setEnabled(true);
        source.getPostgres().setHost("localhost");
        source.getPostgres().setPort(5432);
        source.getPostgres().setDatabase("videoanalytics");
        source.getPostgres().setUsername("postgres");
        source.getPostgres().setPassword("secret");

        DriverManagerDataSource ds = assertDataSource(factory.forSource(source));
        assertEquals("jdbc:postgresql://localhost:5432/videoanalytics", ds.getUrl());
    }

    @Test
    void buildsMsSqlUrl() {
        ConfigModel.SourceConfig source = new ConfigModel.SourceConfig();
        source.getMssql().setEnabled(true);
        source.getMssql().setHost("localhost");
        source.getMssql().setPort(1433);
        source.getMssql().setDatabase("videoanalytics");
        source.getMssql().setUsername("sa");
        source.getMssql().setPassword("secret");

        DriverManagerDataSource ds = assertDataSource(factory.forSource(source));
        assertEquals("jdbc:sqlserver://localhost:1433;databaseName=videoanalytics", ds.getUrl());
    }

    @Test
    void buildsOracleUrl() {
        ConfigModel.SourceConfig source = new ConfigModel.SourceConfig();
        source.getOracle().setEnabled(true);
        source.getOracle().setHost("localhost");
        source.getOracle().setPort(1521);
        source.getOracle().setServiceName("ORCL");
        source.getOracle().setUsername("system");
        source.getOracle().setPassword("secret");

        DriverManagerDataSource ds = assertDataSource(factory.forSource(source));
        assertEquals("jdbc:oracle:thin:@localhost:1521/ORCL", ds.getUrl());
    }

    @Test
    void buildsH2Url() {
        ConfigModel.SourceConfig source = new ConfigModel.SourceConfig();
        source.getH2().setEnabled(true);
        source.getH2().setUrl("jdbc:h2:mem:testdb");
        source.getH2().setUsername("sa");
        source.getH2().setPassword("");

        DriverManagerDataSource ds = assertDataSource(factory.forSource(source));
        assertEquals("jdbc:h2:mem:testdb", ds.getUrl());
    }

    @Test
    void buildsDestinationMysqlUrl() {
        ConfigModel.DestinationConfig destination = new ConfigModel.DestinationConfig();
        destination.getMysql().setEnabled(true);
        destination.getMysql().setHost("localhost");
        destination.getMysql().setPort(3306);
        destination.getMysql().setDatabase("videoanalytics_new");
        destination.getMysql().setUsername("root");
        destination.getMysql().setPassword("secret");

        DriverManagerDataSource ds = assertDataSource(factory.forDestination(destination));
        assertEquals("jdbc:mysql://localhost:3306/videoanalytics_new", ds.getUrl());
    }

    private DriverManagerDataSource assertDataSource(DataSource dataSource) {
        return assertInstanceOf(DriverManagerDataSource.class, dataSource);
    }
}
