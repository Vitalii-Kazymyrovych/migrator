package com.vezha.migrator.reader;

import com.vezha.migrator.config.ConfigModel;
import com.vezha.migrator.config.DataSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseReader implements SourceReader {

    private final DataSourceFactory dataSourceFactory;

    public DatabaseReader(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public JdbcTemplate read(ConfigModel configModel) {
        return new JdbcTemplate(dataSourceFactory.forSource(configModel.getSource()));
    }
}
