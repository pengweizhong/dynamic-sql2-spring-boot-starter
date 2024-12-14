package com.dynamic.sql.starter.application;


import com.dynamic.sql.plugins.schema.DbSchemaMatcher;

import java.util.List;

public class DbSchemaMatcherRegistrar {
    private final List<DbSchemaMatcher> schemaMatchers;

    public DbSchemaMatcherRegistrar(List<DbSchemaMatcher> schemaMatchers) {
        this.schemaMatchers = schemaMatchers;
    }

    public List<DbSchemaMatcher> getSchemaMatchers() {
        return schemaMatchers;
    }
}
