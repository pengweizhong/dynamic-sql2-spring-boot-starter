# Dynamic-SQL2 Spring Boot Starter

**Dynamic-SQL2 Spring Boot Starter** 是基于 Spring Boot
的自动配置模块，用于简化和增强 [dynamic-sql2](https://github.com/pengweizhong/dynamic-sql2) 的集成。提供了灵活、安全的
SQL 动态构建和执行能力，同时支持多种数据库的兼容性**，让开发者能够专注于业务逻辑。

## 功能特色

- **动态 SQL 构建**：支持动态生成子查询、窗口函数、CTE、递归查询等高级 SQL 功能。
- **数据库兼容性**：兼容 MySQL（完全适配）、Oracle(部分适配) 和 DB2（待适配） 数据库，屏蔽底层差异。
- **简化集成**：通过 Spring Boot 自动配置，快速上手，无需额外配置。
- **高可扩展性**：提供拦截器机制，支持 SQL 日志、审计等功能的自定义实现。
- **开箱即用**：支持 Spring JDBC，集成便捷，便于与现有应用无缝连接。

---

## 快速开始

### 1. 引入依赖

在项目的 `pom.xml` 文件中添加以下依赖：

```xml

<!--springboot 2.x-->
<dependency>
    <groupId>com.dynamic-sql</groupId>
    <artifactId>dynamic-sql2-spring-boot-starter</artifactId>
    <version>0.1.8</version>
</dependency>

<!--springboot 3.x-->
<dependency>
    <groupId>com.dynamic-sql</groupId>
    <artifactId>dynamic-sql2-spring-boot3-starter</artifactId>
    <version>0.1.8</version>
</dependency>

```

### 2. 配置

#### 2.1 配置文件

> 单数据源配置文件无需特殊处理，Dynamic-SQL2 会自动识别并加载数据源。

在 `application.yml` 中添加必要的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dynamic_sql2?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      # Hikari-specific settings
      pool-name: MyHikariCP
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000 # milliseconds
      connection-timeout: 30000 # milliseconds
      max-lifetime: 1800000 # milliseconds
      validation-timeout: 5000 # milliseconds
      leak-detection-threshold: 2000 # milliseconds
```

#### 2.2 配置代码

> 代码配置可以提供更多的灵活性，并很好的支持了多数据源的场景

```java
import com.dynamic.sql.context.properties.SchemaProperties;
import com.dynamic.sql.plugins.resolve.DefaultValueParser;
import com.dynamic.sql.plugins.resolve.ValueParser;
import com.dynamic.sql.plugins.resolve.ValueResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
//可选注解，当数据源配置类较多时建议添加
//@AutoConfigureBefore(DynamicSqlAutoConfiguration.class)
public class DataSourceConfig {

    // 可选配置 ValueParser
    @Bean
    public ValueParser valueParser() {
        // 默认的解析实现，也可以自定义实现 ValueParser 接口
        DefaultValueParser defaultValueParser = new DefaultValueParser();
        ValueResolver valueResolver = defaultValueParser.getValueResolver();
        Map<String, String> resolverConfig = valueResolver.getConfig();
        // 添加自定义的配置项，用于解析 Table 注解中的 schema 和表名占位符
        resolverConfig.put("com.profile.table.user", "t_user");
        resolverConfig.put("com.profile.schema.example_db", "t_example_db");
        return defaultValueParser;
    }

    @Bean("dataSource")
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        // 基本配置
        config.setJdbcUrl("jdbc:mysql://localhost:3306/dynamic_sql2?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // Hikari特定设置
        config.setPoolName("MyHikariCP");
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        config.setIdleTimeout(30000); // 毫秒
        config.setConnectionTimeout(30000); // 毫秒
        config.setMaxLifetime(1800000); // 毫秒
        config.setValidationTimeout(5000); // 毫秒
        config.setLeakDetectionThreshold(2000); // 毫秒
        return new HikariDataSource(config);
    }

    @Bean("transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SchemaProperties schemaProperties() {
        SchemaProperties schemaProperties = new SchemaProperties();
        schemaProperties.setDataSourceName("dataSource");
        schemaProperties.setGlobalDefault(true);
        schemaProperties.setUseSchemaInQuery(true);
        schemaProperties.getPrintSqlProperties().setPrintDataSourceName(false);
        schemaProperties.getPrintSqlProperties().setPrintSql(true);
        return schemaProperties;
    }

    @Bean("dataSource2")
    public DataSource dataSource2() {
        HikariConfig config = new HikariConfig();
        // 基本配置
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // Hikari特定设置
        config.setPoolName("MyHikariCP");
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        config.setIdleTimeout(30000); // 毫秒
        config.setConnectionTimeout(30000); // 毫秒
        config.setMaxLifetime(1800000); // 毫秒
        config.setValidationTimeout(5000); // 毫秒
        config.setLeakDetectionThreshold(2000); // 毫秒
        return new HikariDataSource(config);
    }

    @Bean
    public SchemaProperties schemaProperties2() {
        SchemaProperties schemaProperties = new SchemaProperties();
        schemaProperties.setDataSourceName("dataSource2");
        //设置此数据源绑定的实体类包路径，后续实体类无需在指定数据源Bean
        schemaProperties.setBindBasePackages("com.demo.demoproject.entities");
        schemaProperties.setGlobalDefault(false);
        schemaProperties.setUseSchemaInQuery(false);
        schemaProperties.getPrintSqlProperties().setPrintDataSourceName(false);
        schemaProperties.getPrintSqlProperties().setPrintSql(true);
        return schemaProperties;
    }


    @Bean("transactionManager2")
    @Primary
    public PlatformTransactionManager transactionManager2(@Qualifier("dataSource2") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}



```

### 3. 启用和使用

**启用：** 确保主类上有 `@SpringBootApplication` 注解，`dynamic-sql2` 将自动完成配置。  
**使用：** 在代码中注入 `SqlContext` Bean 即可，所有数据库交互均由此对象完成。

---

## 使用示例

> 参考：https://github.com/pengweizhong/dynamic-sql2

## 插件配置

### 启用拦截器

可以通过自定义 `SqlInterceptor` 来实现日志、审计、分页等功能：

```java
import com.dynamic.sql.core.database.PreparedSql;
import com.dynamic.sql.core.dml.SqlStatementWrapper;
import com.dynamic.sql.interceptor.ExecutionControl;
import com.dynamic.sql.interceptor.SqlInterceptor;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class CustomSqlInterceptor implements SqlInterceptor {
    @Override
    public ExecutionControl beforeExecution(SqlStatementWrapper sqlStatementWrapper, Connection connection) {
        return ExecutionControl.PROCEED;
    }

    @Override
    public void afterExecution(PreparedSql preparedSql, Object applyResult, Exception exception) {

    }

    @Override
    public int getOrder() {
        return 33333;
    }
}

```

### 自定义`Schema`匹配器

适用于那些支持MySQL、Oracle协议的数据库等等

```java
import com.dynamic.sql.enums.DbType;
import com.dynamic.sql.plugins.schema.DbSchemaMatcher;

@Component
public class OracleSchemaMatcher implements DbSchemaMatcher {
    @Override
    public String matchSchema(String url) {
        // 处理 Oracle 的 URL
        // Oracle SID URL 格式: jdbc:oracle:thin:@host:port:sid
        // Oracle 服务名称 URL 格式: jdbc:oracle:thin:@//host:port/service_name
        // 去掉jdbc:oracle:thin:@ 前缀
        String oracleUrl = url.substring("jdbc:oracle:thin:@".length());
        // 判断 URL 是否包含斜杠
        if (oracleUrl.startsWith("//")) {
            // 服务名称格式: jdbc:oracle:thin:@//host:port/service_name
            String[] parts = oracleUrl.substring(2).split("/");
            if (parts.length > 1) {
                return parts[1].split("\\?")[0];
            }
        } else {
            // SID 格式: jdbc:oracle:thin:@host:port:sid 或 jdbc:oracle:thin:@host:sid
            String[] parts = oracleUrl.split(":");
            if (parts.length == 3) {
                // host:port:sid 格式
                return parts[2];
            } else if (parts.length == 2) {
                // host:sid 格式
                return parts[1];
            }
        }
        return null;
    }

    @Override
    public boolean supports(DbType dbType) {
        return dbType.equals(DbType.ORACLE);
    }
}
```

---

## 贡献指南

欢迎贡献代码或提交问题！请参考以下流程：

1. Fork 仓库
2. 创建分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add your feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 创建 Pull Request

---

## Issue

如有任何问题或建议，请提交到 [Issue 页面](https://github.com/pengweizhong/dynamic-sql2-spring-boot-starter/issues)。