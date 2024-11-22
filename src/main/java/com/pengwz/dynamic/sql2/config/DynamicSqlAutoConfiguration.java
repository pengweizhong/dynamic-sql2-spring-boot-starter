package com.pengwz.dynamic.sql2.config;

import com.pengwz.dynamic.sql2.application.MapperBeanDefinitionRegistrar;
import com.pengwz.dynamic.sql2.context.SqlContextConfigurer;
import com.pengwz.dynamic.sql2.context.SqlContextHelper;
import com.pengwz.dynamic.sql2.context.properties.SqlContextProperties;
import com.pengwz.dynamic.sql2.core.SqlContext;
import com.pengwz.dynamic.sql2.datasource.DataSourceMapping;
import com.pengwz.dynamic.sql2.datasource.DataSourceUtils;
import com.pengwz.dynamic.sql2.interceptor.SqlInterceptorChain;
import com.pengwz.dynamic.sql2.mapper.MapperScanner;
import com.pengwz.dynamic.sql2.plugins.schema.DbSchemaMatcher;
import com.pengwz.dynamic.sql2.plugins.schema.impl.MysqlSchemaMatcher;
import com.pengwz.dynamic.sql2.plugins.schema.impl.OracleSchemaMatcher;
import com.pengwz.dynamic.sql2.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(SqlContextPropertiesBinding.class)
public class DynamicSqlAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(DynamicSqlAutoConfiguration.class);

    private final SqlContextPropertiesBinding sqlContextPropertiesBinding;
    private final ApplicationContext applicationContext;

    @Autowired
    public DynamicSqlAutoConfiguration(SqlContextPropertiesBinding sqlContextPropertiesBinding,
                                       ApplicationContext applicationContext) {
        log.info("DynamicSqlAutoConfiguration init.");
        this.sqlContextPropertiesBinding = sqlContextPropertiesBinding;
        this.applicationContext = applicationContext;
    }

    @Bean
    @DependsOn("sqlContext")
    public MapperBeanDefinitionRegistrar mapperBeanDefinitionRegistrar() {
        return new MapperBeanDefinitionRegistrar();
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlContext sqlContext() {
        log.info("SqlContext init.");
        SqlContextProperties sqlContextProperties = sqlContextPropertiesBinding.getConfig();
        if (sqlContextProperties == null) {
            sqlContextProperties = SqlContextProperties.defaultSqlContextProperties();
        }
        if (CollectionUtils.isEmpty(sqlContextProperties.getSchemaMatchers())) {
            Set<DbSchemaMatcher> dbSchemaMatchers = new LinkedHashSet<>();
            dbSchemaMatchers.add(new MysqlSchemaMatcher());
            dbSchemaMatchers.add(new OracleSchemaMatcher());
            sqlContextProperties.setSchemaMatchers(dbSchemaMatchers);
        } else {
            sqlContextProperties.getSchemaMatchers().add(new MysqlSchemaMatcher());
            sqlContextProperties.getSchemaMatchers().add(new OracleSchemaMatcher());
        }
        if (sqlContextProperties.getScanDatabasePackage() == null || sqlContextProperties.getScanDatabasePackage().length == 0) {
            Map.Entry<String, DataSource> dataSourceEntry = defaultDataSource();
            String dataSourceName = dataSourceEntry.getKey();
            DataSource dataSource = dataSourceEntry.getValue();
            DataSourceMapping dataSourceMapping = new DataSourceMapping(dataSourceName, dataSource, true, null);
            DataSourceUtils.checkAndSave(sqlContextProperties, dataSourceMapping);
        } else {
            DataSourceUtils.scanAndInitDataSource(sqlContextProperties);
        }
        SqlContextConfigurer sqlContextConfigurer = SqlContextHelper.createSqlContextConfigurer(sqlContextProperties);
        SqlContextHelper.addSchemaProperties(sqlContextProperties);
        //添加拦截器
        sqlContextProperties.getInterceptors().forEach(SqlInterceptorChain.getInstance()::addInterceptor);
        SqlContext sqlContext = sqlContextConfigurer.getSqlContext();
        //注入mapper
        String[] scanMapperPackage = sqlContextProperties.getScanMapperPackage();
        MapperScanner.scanAndInitMapper(scanMapperPackage, sqlContext);
        return sqlContext;
    }


    private Map.Entry<String, DataSource> defaultDataSource() {
        Map<String, DataSource> dataSourceBeanMap = applicationContext.getBeansOfType(DataSource.class);
        if (dataSourceBeanMap.size() > 1) {
            throw new IllegalStateException("More than one DataSource bean found in application context.");
        }
        return dataSourceBeanMap.entrySet().iterator().next();
    }
}