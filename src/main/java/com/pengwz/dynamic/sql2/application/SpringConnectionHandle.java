package com.pengwz.dynamic.sql2.application;

import com.pengwz.dynamic.sql2.datasource.connection.ConnectionHandle;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SpringConnectionHandle implements ConnectionHandle {
    @Override
    public Connection getConnection(DataSource dataSource) {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void releaseConnection(DataSource dataSource, Connection connection, ResultSet resultSet, Statement statement) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }
    }
}
