package com.vezha.migrator.migration.impl;

import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

final class MigratorTestSupport {

    private MigratorTestSupport() {
    }

    static void assertSetterValues(ParameterizedPreparedStatementSetter<java.util.Map<String, Object>> setter,
                                   java.util.Map<String, Object> row,
                                   Object... expected) throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        setter.setValues(ps, row);
        for (int i = 0; i < expected.length; i++) {
            verify(ps).setObject(i + 1, expected[i]);
        }
    }
}
