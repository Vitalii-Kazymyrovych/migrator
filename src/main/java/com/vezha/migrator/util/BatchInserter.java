package com.vezha.migrator.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BatchInserter implements AutoCloseable {

    private final PreparedStatement preparedStatement;
    private final Connection connection;
    private final int batchSize;
    private int counter;

    public BatchInserter(PreparedStatement preparedStatement, int batchSize) throws SQLException {
        this(preparedStatement, preparedStatement.getConnection(), batchSize);
    }

    public BatchInserter(PreparedStatement preparedStatement, Connection connection, int batchSize) {
        this.preparedStatement = preparedStatement;
        this.connection = connection;
        this.batchSize = batchSize;
    }

    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
        counter++;
        if (counter >= batchSize) {
            flush();
        }
    }

    public void flush() throws SQLException {
        if (counter == 0) {
            return;
        }
        try {
            preparedStatement.executeBatch();
            counter = 0;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    @Override
    public void close() throws SQLException {
        flush();
    }
}
