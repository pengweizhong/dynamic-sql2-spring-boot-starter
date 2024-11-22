package com.pengwz.dynamic.sql2.application;

import com.pengwz.dynamic.sql2.mapper.MapperProxyFactory;
import com.pengwz.dynamic.sql2.mapper.MapperRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.beans.Introspector;
import java.util.List;

public class MapperBeanDefinitionRegistrar implements ApplicationContextAware {
    public MapperBeanDefinitionRegistrar() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof GenericApplicationContext) {
            GenericApplicationContext genericContext = (GenericApplicationContext) applicationContext;
            List<MapperRegistry<?>> mapperRegistrys = MapperProxyFactory.getMapperRegistrys();
            for (MapperRegistry<?> mapperRegistry : mapperRegistrys) {
                String beanName = Introspector.decapitalize(mapperRegistry.getProxyMapper().getClass().getSimpleName());
                genericContext.getBeanFactory().registerSingleton(beanName, mapperRegistry.getProxyMapper());
            }
        }
    }
}