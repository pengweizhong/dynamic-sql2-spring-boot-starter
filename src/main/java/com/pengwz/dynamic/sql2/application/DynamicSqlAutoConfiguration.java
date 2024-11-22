package com.pengwz.dynamic.sql2.application;

import com.pengwz.dynamic.sql2.config.SqlContextPropertiesBinding;
import com.pengwz.dynamic.sql2.context.SqlContextHelper;
import com.pengwz.dynamic.sql2.context.properties.SchemaProperties;
import com.pengwz.dynamic.sql2.context.properties.SqlContextProperties;
import com.pengwz.dynamic.sql2.core.SqlContext;
import com.pengwz.dynamic.sql2.datasource.DataSourceMapping;
import com.pengwz.dynamic.sql2.datasource.DataSourceUtils;
import com.pengwz.dynamic.sql2.interceptor.SqlInterceptor;
import com.pengwz.dynamic.sql2.interceptor.SqlInterceptorChain;
import com.pengwz.dynamic.sql2.plugins.pagination.PageInterceptorPlugin;
import com.pengwz.dynamic.sql2.utils.MapUtils;
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
import java.util.List;
import java.util.Map;

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
    public PageInterceptorPlugin pageInterceptorPlugin() {
        return new PageInterceptorPlugin();
    }

    @Bean
    @DependsOn("sqlContext")
    public MapperBeanDefinitionRegistrar mapperBeanDefinitionRegistrar() {
        return new MapperBeanDefinitionRegistrar();
    }

    @Bean
    public SqlInterceptorRegistrar sqlInterceptorRegistrar(List<SqlInterceptor> interceptors) {
        log.info("SqlInterceptorRegistrar init.");
        for (SqlInterceptor interceptor : interceptors) {
            log.info("Add SqlInterceptor for {}.", interceptor.getClass().getCanonicalName());
            SqlInterceptorChain.getInstance().addInterceptor(interceptor);
        }
        return new SqlInterceptorRegistrar(interceptors);
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlContext sqlContext() {
        log.info("SqlContext init.");
        SqlContextProperties sqlContextProperties = sqlContextPropertiesBinding.getConfig();
        if (sqlContextProperties == null) {
            return buildDefaultSqlContext();
        }
        throw new UnsupportedOperationException("todo");
    }

    private SqlContext buildDefaultSqlContext() {
        SqlContextProperties sqlContextProperties = SqlContextProperties.defaultSqlContextProperties();
        //优先设置数据源
        Map.Entry<String, DataSource> dataSourceEntry = defaultDataSource();
        String dataSourceName = dataSourceEntry.getKey();
        DataSource dataSource = dataSourceEntry.getValue();
        DataSourceMapping dataSourceMapping = new DataSourceMapping(dataSourceName, dataSource, true, null);
        DataSourceUtils.checkAndSave(sqlContextProperties, dataSourceMapping);
        SchemaProperties schemaProperties = sqlContextProperties.getSchemaProperties().get(0);
        schemaProperties.setDataSourceName(dataSourceName);
        schemaProperties.setUseSchemaInQuery(false);
        schemaProperties.setUseAsInQuery(true);
        SchemaProperties.PrintSqlProperties printSqlProperties = new SchemaProperties.PrintSqlProperties();
        printSqlProperties.setPrintSql(true);
        printSqlProperties.setPrintDataSourceName(false);
        schemaProperties.setPrintSqlProperties(printSqlProperties);
        sqlContextProperties.addSchemaProperties(schemaProperties);
//        sqlContextProperties.addInterceptor(new PageInterceptorPlugin());
        SqlContextHelper.addSchemaProperties(sqlContextProperties);
        return SqlContextHelper.createSqlContextConfigurer(sqlContextProperties).getSqlContext();
    }


    private Map.Entry<String, DataSource> defaultDataSource() {
        Map<String, DataSource> dataSourceBeanMap = applicationContext.getBeansOfType(DataSource.class);
        if (MapUtils.isEmpty(dataSourceBeanMap)) {
            throw new IllegalStateException("No data source detected.");
        }
        if (dataSourceBeanMap.size() > 1) {
            throw new IllegalStateException("More than one DataSource bean found in application context.");
        }
        return dataSourceBeanMap.entrySet().iterator().next();
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public SqlContext sqlContext() {
//        log.info("SqlContext init.");
//        SqlContextProperties sqlContextProperties = sqlContextPropertiesBinding.getConfig();
//        if (sqlContextProperties == null) {
//            sqlContextProperties = SqlContextProperties.defaultSqlContextProperties();
//        }
//        if (CollectionUtils.isEmpty(sqlContextProperties.getSchemaMatchers())) {
//            Set<DbSchemaMatcher> dbSchemaMatchers = new LinkedHashSet<>();
//            dbSchemaMatchers.add(new MysqlSchemaMatcher());
//            dbSchemaMatchers.add(new OracleSchemaMatcher());
//            sqlContextProperties.setSchemaMatchers(dbSchemaMatchers);
//        } else {
//            sqlContextProperties.getSchemaMatchers().add(new MysqlSchemaMatcher());
//            sqlContextProperties.getSchemaMatchers().add(new OracleSchemaMatcher());
//        }
//        if (sqlContextProperties.getScanDatabasePackage() == null || sqlContextProperties.getScanDatabasePackage().length == 0) {
//            Map.Entry<String, DataSource> dataSourceEntry = defaultDataSource();
//            String dataSourceName = dataSourceEntry.getKey();
//            DataSource dataSource = dataSourceEntry.getValue();
//            DataSourceMapping dataSourceMapping = new DataSourceMapping(dataSourceName, dataSource, true, null);
//            DataSourceUtils.checkAndSave(sqlContextProperties, dataSourceMapping);
//        } else {
//            DataSourceUtils.scanAndInitDataSource(sqlContextProperties);
//        }
//        SqlContextConfigurer sqlContextConfigurer = SqlContextHelper.createSqlContextConfigurer(sqlContextProperties);
//        SqlContextHelper.addSchemaProperties(sqlContextProperties);
//        //添加拦截器
//        sqlContextProperties.getInterceptors().forEach(SqlInterceptorChain.getInstance()::addInterceptor);
//        SqlContext sqlContext = sqlContextConfigurer.getSqlContext();
//        //注入mapper
//        String[] scanMapperPackage = sqlContextProperties.getScanMapperPackage();
//        MapperScanner.scanAndInitMapper(scanMapperPackage, sqlContext);
//        return sqlContext;
//    }


}