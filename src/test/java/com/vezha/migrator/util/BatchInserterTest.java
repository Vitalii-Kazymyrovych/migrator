package com.vezha.migrator.util;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchInserterTest {

    @Test
    void flushesWhenBatchSizeReached() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(preparedStatement.getConnection()).thenReturn(connection);

        BatchInserter inserter = new BatchInserter(preparedStatement, 2);
        inserter.addBatch();
        inserter.addBatch();

        verify(preparedStatement, times(2)).addBatch();
        verify(preparedStatement, times(1)).executeBatch();
    }

    @Test
    void flushesRemainderOnClose() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(preparedStatement.getConnection()).thenReturn(connection);

        BatchInserter inserter = new BatchInserter(preparedStatement, 10);
        inserter.addBatch();
        inserter.close();

        verify(preparedStatement, times(1)).executeBatch();
    }

    @Test
    void executeBatchExceptionRollsBackAndRethrows() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(preparedStatement.getConnection()).thenReturn(connection);
        when(preparedStatement.executeBatch()).thenThrow(new SQLException("boom"));

        BatchInserter inserter = new BatchInserter(preparedStatement, 1);

        assertThrows(SQLException.class, inserter::addBatch);
        verify(connection, times(1)).rollback();
    }
}
