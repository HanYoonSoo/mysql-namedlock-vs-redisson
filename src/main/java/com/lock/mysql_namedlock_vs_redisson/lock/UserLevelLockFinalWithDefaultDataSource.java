package com.lock.mysql_namedlock_vs_redisson.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class UserLevelLockFinalWithDefaultDataSource {

    private static final String GET_LOCK = "SELECT GET_LOCK(?, ?)";
    private static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(?)";
    private static final String EXCEPTION_MESSAGE = "LOCK 을 수행하는 중에 오류가 발생하였습니다.";

    private final DataSource dataSource;

    public <T> T executeWithLock(String userLockName, int timeoutSeconds, Supplier<T> supplier) {
        try (Connection connection = dataSource.getConnection()) {
            boolean locked = false;
            try {
                getLock(connection, userLockName, timeoutSeconds);
                locked = true;
                return supplier.get();
            } finally {
                if (locked) {
                    releaseLock(connection, userLockName);
                }
            }
        } catch (SQLException | RuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void getLock(Connection connection, String userLockName, int timeoutSeconds) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(GET_LOCK)) {
            preparedStatement.setString(1, userLockName);
            preparedStatement.setInt(2, timeoutSeconds);

            checkResultSet(userLockName, preparedStatement, "GetLock");
        }
    }

    private void releaseLock(Connection connection, String userLockName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(RELEASE_LOCK)) {
            preparedStatement.setString(1, userLockName);

            checkResultSet(userLockName, preparedStatement, "ReleaseLock");
        }
    }

    private void checkResultSet(String userLockName, PreparedStatement preparedStatement, String type) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
                String message = EXCEPTION_MESSAGE + " type=" + type + ", userLockName=" + userLockName + ", result=null";
                log.error(message);
                throw new RuntimeException(message);
            }

            int result = resultSet.getInt(1);
            if (result != 1) {
                String message = EXCEPTION_MESSAGE + " type=" + type + ", userLockName=" + userLockName + ", result=" + result;
                log.error(message);
                throw new RuntimeException(message);
            }
        }
    }
}
