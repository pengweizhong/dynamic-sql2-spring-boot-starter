package com.pengwz.dynamic.sql2.config;

import com.pengwz.dynamic.sql2.context.SchemaContextHolder;
import com.pengwz.dynamic.sql2.context.SqlContextConfigurer;
import com.pengwz.dynamic.sql2.context.SqlContextHelper;
import com.pengwz.dynamic.sql2.context.properties.SchemaProperties;
import com.pengwz.dynamic.sql2.context.properties.SqlContextProperties;
import com.pengwz.dynamic.sql2.core.SqlContext;
import com.pengwz.dynamic.sql2.datasource.DataSourceMapping;
import com.pengwz.dynamic.sql2.datasource.DataSourceUtils;
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
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SqlContextPropertiesBinding.class)
@DependsOn("dataSource")
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
    @ConditionalOnMissingBean
    public SqlContext sqlContext() {
        log.info("SqlContext init.");
//        if (sqlContextPropertiesBinding == null) {
//            throw new RuntimeException("SqlContext init failed, no sql context properties found.");
//        }
        SqlContextProperties sqlContextProperties = sqlContextPropertiesBinding.getConfig();
        if (sqlContextProperties == null) {
            sqlContextProperties = SqlContextProperties.defaultSqlContextProperties();
        }
        Map<String, DataSource> dataSourceBeanMap = applicationContext.getBeansOfType(DataSource.class);
        if (dataSourceBeanMap.size() > 1) {
            throw new IllegalStateException("More than one DataSource bean found in application context.");
        }
        Map.Entry<String, DataSource> next = dataSourceBeanMap.entrySet().iterator().next();
        String dataSourceName = next.getKey();
        DataSource dataSource = next.getValue();
        DataSourceMapping dataSourceMapping = new DataSourceMapping(dataSourceName, dataSource, true, null);
        DataSourceUtils.checkAndSave(sqlContextProperties, dataSourceMapping);
        SqlContextConfigurer sqlContextConfigurer = SqlContextHelper.createSqlContextConfigurer(sqlContextProperties);
        SqlContextHelper.addSchemaProperties(sqlContextProperties);
        return sqlContextConfigurer.getSqlContext();
    }

//    private SqlContext createSqlContext(SqlContextProperties sqlContextProperties) {
//        SqlContextConfigurer sqlContextConfigurer = new SqlContextConfigurer(sqlContextProperties, new DefaultSqlContext());
//        sqlContextConfigurer.initializeContext();
//        for (SchemaProperties schemaProperty : sqlContextConfigurer.getSqlContextProperties().getSchemaProperties()) {
//            SchemaContextHolder.addSchemaProperties(schemaProperty);
//        }
//        sqlContextProperties.getInterceptors().forEach(SqlInterceptorChain.getInstance()::addInterceptor);
//        return sqlContextConfigurer.getSqlContext();
//    }
}