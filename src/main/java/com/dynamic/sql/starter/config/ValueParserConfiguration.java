package com.dynamic.sql.starter.config;

import com.dynamic.sql.plugins.resolve.DefaultValueParser;
import com.dynamic.sql.plugins.resolve.ValueParser;
import com.dynamic.sql.plugins.resolve.ValueParserRegistrar;
import com.dynamic.sql.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ValueParserConfiguration {

    @Autowired(required = false)
    private List<ValueParser> parsers;

    @Bean
    public ValueParserRegistrar valueParserRegistrar() {
        ValueParserRegistrar registrar = new ValueParserRegistrar();
        if (CollectionUtils.isEmpty(parsers)) {
            registrar.register(new DefaultValueParser());
        } else {
            parsers.forEach(registrar::register);
        }
        return registrar;
    }
}
