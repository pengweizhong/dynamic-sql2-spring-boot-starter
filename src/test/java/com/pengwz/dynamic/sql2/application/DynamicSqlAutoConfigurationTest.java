package com.pengwz.dynamic.sql2.application;


import com.dynamic.sql.context.properties.SchemaProperties;
import com.dynamic.sql.context.properties.SqlLogProperties;
import com.dynamic.sql.enums.LogLevel;
import com.dynamic.sql.starter.application.DynamicSqlAutoConfiguration;
import org.springframework.context.annotation.Bean;

class DynamicSqlAutoConfigurationTest {

    public static void main(String[] args) {
        new DynamicSqlAutoConfiguration(null, null);
    }


    @Bean("schemaProperties")
    public SchemaProperties schemaProperties() {
        SchemaProperties schemaProperties = new SchemaProperties();
        // 配置数据源名称
        schemaProperties.setDataSourceName("configDataSource");
        // 配置全局默认数据源
        schemaProperties.setGlobalDefault(true);
        // 是否在查询中使用schema
        schemaProperties.setUseSchemaInQuery(false);
        // 将此包下的实体类与数据源进行绑定
        schemaProperties.setBindBasePackages("com.pengwz.entities.configdb");
        // 配置打印SQL
        SqlLogProperties sqlLogProperties = schemaProperties.getSqlLogProperties();
        // 是否打印数据源名称
        sqlLogProperties.setPrintDataSourceName(true);
        // 设置打印SQL的日志级别
        sqlLogProperties.setLevel(LogLevel.DEBUG);
        // 打印SQL的执行耗时
        sqlLogProperties.setPrintExecutionTime(true);
        return schemaProperties;
    }
}