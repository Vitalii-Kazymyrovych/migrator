package com.vezha.migrator.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class AppConfig {

    private final ObjectMapper objectMapper;

    public AppConfig() {
        this(new ObjectMapper());
    }

    public AppConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ConfigModel load(Path configPath) {
        try {
            ConfigModel model = objectMapper.readValue(configPath.toFile(), ConfigModel.class);
            validate(model);
            return model;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read config: " + configPath, e);
        }
    }

    public void validate(ConfigModel model) {
        int enabledSources = countEnabledSources(model);
        if (enabledSources != 1) {
            throw new IllegalArgumentException("Exactly one source must be enabled, found: " + enabledSources);
        }

        int enabledDestinations = countEnabledDestinations(model);
        if (enabledDestinations != 1) {
            throw new IllegalArgumentException("Exactly one destination must be enabled, found: " + enabledDestinations);
        }

        validateRequiredCredentials(model);
        validateFaceListPath(model);
    }

    private int countEnabledSources(ConfigModel model) {
        ConfigModel.SourceConfig source = model.getSource();
        return (source.getSqlFile().isEnabled() ? 1 : 0)
                + (source.getMysql().isEnabled() ? 1 : 0)
                + (source.getPostgres().isEnabled() ? 1 : 0)
                + (source.getMssql().isEnabled() ? 1 : 0)
                + (source.getOracle().isEnabled() ? 1 : 0)
                + (source.getH2().isEnabled() ? 1 : 0);
    }

    private int countEnabledDestinations(ConfigModel model) {
        ConfigModel.DestinationConfig destination = model.getDestination();
        return (destination.getPostgres().isEnabled() ? 1 : 0)
                + (destination.getMysql().isEnabled() ? 1 : 0);
    }

    private void validateRequiredCredentials(ConfigModel model) {
        List<String> missing = new ArrayList<>();
        ConfigModel.SourceConfig source = model.getSource();
        ConfigModel.DestinationConfig destination = model.getDestination();

        if (source.getMysql().isEnabled()) {
            requireNonBlank(missing, source.getMysql().getHost(), "source.mysql.host");
            requireNonBlank(missing, source.getMysql().getDatabase(), "source.mysql.database");
            requireNonBlank(missing, source.getMysql().getUsername(), "source.mysql.username");
            requireNonBlank(missing, source.getMysql().getPassword(), "source.mysql.password");
            requireNonNull(missing, source.getMysql().getPort(), "source.mysql.port");
        }

        if (source.getPostgres().isEnabled()) {
            requireNonBlank(missing, source.getPostgres().getHost(), "source.postgres.host");
            requireNonBlank(missing, source.getPostgres().getDatabase(), "source.postgres.database");
            requireNonBlank(missing, source.getPostgres().getUsername(), "source.postgres.username");
            requireNonBlank(missing, source.getPostgres().getPassword(), "source.postgres.password");
            requireNonBlank(missing, source.getPostgres().getSchema(), "source.postgres.schema");
            requireNonNull(missing, source.getPostgres().getPort(), "source.postgres.port");
        }

        if (source.getMssql().isEnabled()) {
            requireNonBlank(missing, source.getMssql().getHost(), "source.mssql.host");
            requireNonBlank(missing, source.getMssql().getDatabase(), "source.mssql.database");
            requireNonBlank(missing, source.getMssql().getUsername(), "source.mssql.username");
            requireNonBlank(missing, source.getMssql().getPassword(), "source.mssql.password");
            requireNonNull(missing, source.getMssql().getPort(), "source.mssql.port");
        }

        if (source.getOracle().isEnabled()) {
            requireNonBlank(missing, source.getOracle().getHost(), "source.oracle.host");
            requireNonBlank(missing, source.getOracle().getServiceName(), "source.oracle.serviceName");
            requireNonBlank(missing, source.getOracle().getUsername(), "source.oracle.username");
            requireNonBlank(missing, source.getOracle().getPassword(), "source.oracle.password");
            requireNonNull(missing, source.getOracle().getPort(), "source.oracle.port");
        }

        if (source.getH2().isEnabled()) {
            requireNonBlank(missing, source.getH2().getUrl(), "source.h2.url");
            requireNonBlank(missing, source.getH2().getUsername(), "source.h2.username");
        }

        if (source.getSqlFile().isEnabled()) {
            requireNonBlank(missing, source.getSqlFile().getPath(), "source.sqlFile.path");
        }

        if (destination.getPostgres().isEnabled()) {
            requireNonBlank(missing, destination.getPostgres().getHost(), "destination.postgres.host");
            requireNonBlank(missing, destination.getPostgres().getDatabase(), "destination.postgres.database");
            requireNonBlank(missing, destination.getPostgres().getSchema(), "destination.postgres.schema");
            requireNonBlank(missing, destination.getPostgres().getUsername(), "destination.postgres.username");
            requireNonBlank(missing, destination.getPostgres().getPassword(), "destination.postgres.password");
            requireNonNull(missing, destination.getPostgres().getPort(), "destination.postgres.port");
        }

        if (destination.getMysql().isEnabled()) {
            requireNonBlank(missing, destination.getMysql().getHost(), "destination.mysql.host");
            requireNonBlank(missing, destination.getMysql().getDatabase(), "destination.mysql.database");
            requireNonBlank(missing, destination.getMysql().getUsername(), "destination.mysql.username");
            requireNonBlank(missing, destination.getMysql().getPassword(), "destination.mysql.password");
            requireNonNull(missing, destination.getMysql().getPort(), "destination.mysql.port");
        }

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing required config values: " + String.join(", ", missing));
        }
    }

    private void validateFaceListPath(ConfigModel model) {
        Map<String, ConfigModel.TableConfig> tables = model.getMigration().getTables();
        ConfigModel.TableConfig faceLists = tables.get("face_lists");
        if (faceLists != null && faceLists.isEnabled()) {
            if (isBlank(faceLists.getImageFolderPath())) {
                throw new IllegalArgumentException("migration.tables.face_lists.imageFolderPath must be non-empty");
            }
            Path folder = Path.of(faceLists.getImageFolderPath());
            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                throw new IllegalArgumentException("Face list image folder does not exist: " + folder);
            }
        }
    }

    private void requireNonBlank(List<String> missing, String value, String name) {
        if (isBlank(value)) {
            missing.add(name);
        }
    }

    private void requireNonNull(List<String> missing, Object value, String name) {
        if (Objects.isNull(value)) {
            missing.add(name);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Stream<String> enabledTables(ConfigModel configModel) {
        return configModel.getMigration().getTables().entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().isEnabled())
                .map(Map.Entry::getKey);
    }
}
