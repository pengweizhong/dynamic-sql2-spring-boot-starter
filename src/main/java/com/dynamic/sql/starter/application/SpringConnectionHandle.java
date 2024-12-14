package com.dynamic.sql.starter.application;

import com.dynamic.sql.datasource.connection.ConnectionHandle;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

public class SpringConnectionHandle implements ConnectionHandle {
    @Override
    public Connection getConnection(DataSource dataSource) {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void releaseConnection(DataSource dataSource, Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
