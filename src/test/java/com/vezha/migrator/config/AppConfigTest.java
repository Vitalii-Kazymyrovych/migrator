package com.vezha.migrator.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConfigTest {

    private final AppConfig appConfig = new AppConfig();

    @Test
    void validConfigPassesValidation() {
        ConfigModel config = validConfig();
        assertDoesNotThrow(() -> appConfig.validate(config));
    }

    @Test
    void noEnabledSourceThrows() {
        ConfigModel config = validConfig();
        config.getSource().getMysql().setEnabled(false);
        assertThrows(IllegalArgumentException.class, () -> appConfig.validate(config));
    }

    @Test
    void noEnabledDestinationThrows() {
        ConfigModel config = validConfig();
        config.getDestination().getPostgres().setEnabled(false);
        assertThrows(IllegalArgumentException.class, () -> appConfig.validate(config));
    }

    @Test
    void twoEnabledSourcesThrow() {
        ConfigModel config = validConfig();
        config.getSource().getPostgres().setEnabled(true);
        config.getSource().getPostgres().setHost("localhost");
        config.getSource().getPostgres().setPort(5432);
        config.getSource().getPostgres().setDatabase("va");
        config.getSource().getPostgres().setSchema("public");
        config.getSource().getPostgres().setUsername("u");
        config.getSource().getPostgres().setPassword("p");

        assertThrows(IllegalArgumentException.class, () -> appConfig.validate(config));
    }

    @Test
    void faceListsEnabledWithoutPathThrows() {
        ConfigModel config = validConfig();
        ConfigModel.TableConfig faceConfig = new ConfigModel.TableConfig();
        faceConfig.setEnabled(true);
        config.getMigration().getTables().put("face_lists", faceConfig);

        assertThrows(IllegalArgumentException.class, () -> appConfig.validate(config));
    }

    @Test
    void faceListsEnabledWithMissingFolderThrows() {
        ConfigModel config = validConfig();
        ConfigModel.TableConfig faceConfig = new ConfigModel.TableConfig();
        faceConfig.setEnabled(true);
        faceConfig.setImageFolderPath("/missing/folder");
        config.getMigration().getTables().put("face_lists", faceConfig);

        assertThrows(IllegalArgumentException.class, () -> appConfig.validate(config));
    }

    @Test
    void loadFromJsonParsesAndValidates() throws IOException {
        Path tempDir = Files.createTempDirectory("cfg");
        Path imageDir = Files.createDirectories(tempDir.resolve("images"));
        Path configFile = tempDir.resolve("config.json");

        String json = """
                {
                  "source": {
                    "sqlFile": {"enabled": false, "path": ""},
                    "mysql": {"enabled": true, "host": "localhost", "port": 3306, "database": "videoanalytics", "username": "root", "password": "secret"},
                    "postgres": {"enabled": false, "host": "", "port": 5432, "database": "", "schema": "", "username": "", "password": ""},
                    "mssql": {"enabled": false, "host": "", "port": 1433, "database": "", "username": "", "password": ""},
                    "oracle": {"enabled": false, "host": "", "port": 1521, "serviceName": "", "username": "", "password": ""},
                    "h2": {"enabled": false, "url": "", "username": "", "password": ""}
                  },
                  "destination": {
                    "postgres": {"enabled": true, "host": "localhost", "port": 5432, "database": "videoanalytics_new", "schema": "videoanalytics", "username": "postgres", "password": "secret"},
                    "mysql": {"enabled": false, "host": "", "port": 3306, "database": "", "username": "", "password": ""}
                  },
                  "migration": {
                    "batchSize": 1000,
                    "tables": {
                      "clients": {"enabled": true},
                      "face_lists": {"enabled": true, "imageFolderPath": "%s"}
                    }
                  }
                }
                """.formatted(imageDir.toString().replace("\\", "\\\\"));

        Files.writeString(configFile, json);
        assertDoesNotThrow(() -> appConfig.load(configFile));
    }

    private ConfigModel validConfig() {
        ConfigModel config = new ConfigModel();
        config.setSource(new ConfigModel.SourceConfig());
        config.setDestination(new ConfigModel.DestinationConfig());
        config.setMigration(new ConfigModel.MigrationConfig());
        config.getMigration().setTables(new LinkedHashMap<>());

        config.getSource().getMysql().setEnabled(true);
        config.getSource().getMysql().setHost("localhost");
        config.getSource().getMysql().setPort(3306);
        config.getSource().getMysql().setDatabase("videoanalytics");
        config.getSource().getMysql().setUsername("root");
        config.getSource().getMysql().setPassword("secret");

        config.getDestination().getPostgres().setEnabled(true);
        config.getDestination().getPostgres().setHost("localhost");
        config.getDestination().getPostgres().setPort(5432);
        config.getDestination().getPostgres().setDatabase("videoanalytics_new");
        config.getDestination().getPostgres().setSchema("videoanalytics");
        config.getDestination().getPostgres().setUsername("postgres");
        config.getDestination().getPostgres().setPassword("secret");

        ConfigModel.TableConfig clients = new ConfigModel.TableConfig();
        clients.setEnabled(true);
        config.getMigration().getTables().put("clients", clients);

        return config;
    }
}
