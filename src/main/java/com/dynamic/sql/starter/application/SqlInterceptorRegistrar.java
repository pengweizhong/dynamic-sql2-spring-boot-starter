package com.dynamic.sql.starter.application;

import com.dynamic.sql.interceptor.SqlInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SqlInterceptorRegistrar {
    private final List<SqlInterceptor> interceptors;

    @Autowired
    public SqlInterceptorRegistrar(List<SqlInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public List<SqlInterceptor> getInterceptors() {
        return interceptors;
    }
}
