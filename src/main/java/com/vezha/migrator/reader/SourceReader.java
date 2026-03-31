package com.vezha.migrator.reader;

import com.vezha.migrator.config.ConfigModel;
import org.springframework.jdbc.core.JdbcTemplate;

public interface SourceReader {
    JdbcTemplate read(ConfigModel configModel);
}
