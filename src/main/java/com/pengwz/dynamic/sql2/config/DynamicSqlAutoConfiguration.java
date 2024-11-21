package com.pengwz.dynamic.sql2.config;

import com.pengwz.dynamic.sql2.context.SqlContextHelper;
import com.pengwz.dynamic.sql2.context.properties.SqlContextProperties;
import com.pengwz.dynamic.sql2.core.SqlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SqlContextPropertiesBinding.class)
public class DynamicSqlAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(DynamicSqlAutoConfiguration.class);

    private final SqlContextPropertiesBinding sqlContextPropertiesBinding;

    public DynamicSqlAutoConfiguration(SqlContextPropertiesBinding sqlContextPropertiesBinding) {
        log.info("DynamicSqlAutoConfiguration init.");
        this.sqlContextPropertiesBinding = sqlContextPropertiesBinding;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlContext sqlContext() {
        log.info("SqlContext init.");
        SqlContextProperties sqlContextProperties = sqlContextPropertiesBinding.getSqlContextProperties();
        return SqlContextHelper.createSqlContext(sqlContextProperties);
    }
}