package com.dynamic.sql2.application;

import com.dynamic.sql2.convert.FetchResultConverterRegistrar;
import com.pengwz.dynamic.sql2.context.SqlContextHelper;
import com.pengwz.dynamic.sql2.context.properties.SchemaProperties;
import com.pengwz.dynamic.sql2.context.properties.SchemaProperties.PrintSqlProperties;
import com.pengwz.dynamic.sql2.context.properties.SqlContextProperties;
import com.pengwz.dynamic.sql2.core.SqlContext;
import com.pengwz.dynamic.sql2.datasource.DataSourceMapping;
import com.pengwz.dynamic.sql2.datasource.DataSourceUtils;
import com.pengwz.dynamic.sql2.datasource.connection.ConnectionHolder;
import com.pengwz.dynamic.sql2.interceptor.SqlInterceptor;
import com.pengwz.dynamic.sql2.interceptor.SqlInterceptorChain;
import com.pengwz.dynamic.sql2.plugins.conversion.FetchResultConverter;
import com.pengwz.dynamic.sql2.plugins.pagination.PageInterceptorPlugin;
import com.pengwz.dynamic.sql2.plugins.schema.DbSchemaMatcher;
import com.pengwz.dynamic.sql2.plugins.schema.impl.MysqlSchemaMatcher;
import com.pengwz.dynamic.sql2.plugins.schema.impl.OracleSchemaMatcher;
import com.pengwz.dynamic.sql2.utils.CollectionUtils;
import com.pengwz.dynamic.sql2.utils.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnBean(DataSource.class)
//@EnableConfigurationProperties(SqlContextPropertiesBinding.class)
public class DynamicSqlAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(DynamicSqlAutoConfiguration.class);

    private final ApplicationContext applicationContext;
    private final List<SchemaProperties> schemaProperties;

    @Autowired
    public DynamicSqlAutoConfiguration(ApplicationContext applicationContext,
                                       List<SchemaProperties> schemaProperties) {
        this.applicationContext = applicationContext;
        this.schemaProperties = schemaProperties;
        final String version = "1.0.0";
        log.info("\n ____                              _          ____   ___  _\n" +//NOSONAR
                "|  _ \\ _   _ _ __   __ _ _ __ ___ (_) ___    / ___| / _ \\| |\n" +
                "| | | | | | | '_ \\ / _` | '_ ` _ \\| |/ __|___\\___ \\| | | | |\n" +
                "| |_| | |_| | | | | (_| | | | | | | | (_|_____|__) | |_| | |___\n" +
                "|____/ \\__, |_| |_|\\__,_|_| |_| |_|_|\\___|   |____/ \\__\\_\\_____|\n" +
                "       |___/                                     Version: " + version);
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
        return new SqlInterceptorRegistrar(interceptors);
    }

    @Bean("dbSchemaMatcherRegistrar")
    public DbSchemaMatcherRegistrar dbSchemaMatcherRegistrar(List<DbSchemaMatcher> schemaMatchers) {
        schemaMatchers.add(new MysqlSchemaMatcher());
        schemaMatchers.add(new OracleSchemaMatcher());
        return new DbSchemaMatcherRegistrar(schemaMatchers);
    }

    @Bean("fetchResultConverterRegistrar")
    public FetchResultConverterRegistrar fetchResultConverterRegistrar(List<FetchResultConverter> fetchResultConverters) {
        return new FetchResultConverterRegistrar(fetchResultConverters);
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn({"fetchResultConverterRegistrar", "dbSchemaMatcherRegistrar"})
    public SqlContext sqlContext(DbSchemaMatcherRegistrar dbSchemaMatcherRegistrar,
                                 SqlInterceptorRegistrar sqlInterceptorRegistrar) {
        log.info("SqlContext initialization completed.");
        SqlContextProperties sqlContextProperties = new SqlContextProperties();
        for (SqlInterceptor interceptor : sqlInterceptorRegistrar.getInterceptors()) {
            log.debug("Add SqlInterceptor for {}.", interceptor.getClass().getCanonicalName());
            SqlInterceptorChain.getInstance().addInterceptor(interceptor);
        }
        for (DbSchemaMatcher schemaMatcher : dbSchemaMatcherRegistrar.getSchemaMatchers()) {
            log.debug("Add DbSchemaMatcher for {}.", schemaMatcher.getClass().getCanonicalName());
            sqlContextProperties.getSchemaMatchers().add(schemaMatcher);
        }
        ConnectionHolder.setConnectionHandle(new SpringConnectionHandle());
        // 用户没有自定义配置  就走默认配置
        if (CollectionUtils.isEmpty(schemaProperties)) {
            return buildDefaultSqlContext(sqlContextProperties);
        }
        for (SchemaProperties schemaProperty : schemaProperties) {
            sqlContextProperties.addSchemaProperties(schemaProperty);
            DataSource dataSource = (DataSource) applicationContext.getBean(schemaProperty.getDataSourceName());
            DataSourceMapping dataSourceMapping = new DataSourceMapping(schemaProperty.getDataSourceName(),
                    dataSource, schemaProperty.isGlobalDefault(), schemaProperty.getBindBasePackages());
            DataSourceUtils.checkAndSave(sqlContextProperties, dataSourceMapping);
        }
        SqlContextHelper.addSchemaProperties(sqlContextProperties);
        FetchResultConverterRegistrar fetchResultConverterRegistrar =
                (FetchResultConverterRegistrar) applicationContext.getBean("fetchResultConverterRegistrar");
        fetchResultConverterRegistrar.registrarConverters();
        return SqlContextHelper.createSqlContextConfigurer(sqlContextProperties).getSqlContext();
    }

    private SqlContext buildDefaultSqlContext(SqlContextProperties sqlContextProperties) {
        //优先设置数据源
        Map.Entry<String, DataSource> dataSourceEntry = defaultDataSource();
        String dataSourceName = dataSourceEntry.getKey();
        DataSource dataSource = dataSourceEntry.getValue();
        DataSourceMapping dataSourceMapping = new DataSourceMapping(dataSourceName, dataSource, true, null);
        DataSourceUtils.checkAndSave(sqlContextProperties, dataSourceMapping);
        SchemaProperties schemaProperty = sqlContextProperties.getSchemaProperties().get(0);
        schemaProperty.setDataSourceName(dataSourceName);
        schemaProperty.setUseSchemaInQuery(false);
        schemaProperty.setUseAsInQuery(true);
        PrintSqlProperties printSqlProperties = new PrintSqlProperties();
        printSqlProperties.setPrintSql(true);
        printSqlProperties.setPrintDataSourceName(false);
        schemaProperty.setPrintSqlProperties(printSqlProperties);
        sqlContextProperties.addSchemaProperties(schemaProperty);
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


}