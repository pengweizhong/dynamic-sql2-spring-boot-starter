package com.pengwz.dynamic.sql2.config;

import com.pengwz.dynamic.sql2.context.properties.SqlContextProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "com.pengwz.dynamic.sql2")
public class SqlContextPropertiesBinding {
    @NestedConfigurationProperty
    private SqlContextProperties sqlContextProperties = SqlContextProperties.defaultSqlContextProperties();

    public SqlContextProperties getSqlContextProperties() {
        return sqlContextProperties;
    }

    public void setSqlContextProperties(SqlContextProperties sqlContextProperties) {
        this.sqlContextProperties = sqlContextProperties;
    }
}
