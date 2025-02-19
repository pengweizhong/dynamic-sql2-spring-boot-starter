package com.dynamic.sql.starter.convert;

import com.dynamic.sql.plugins.conversion.FetchResultConverter;
import com.dynamic.sql.utils.CollectionUtils;
import com.dynamic.sql.utils.ConverterUtils;
import com.dynamic.sql.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FetchResultConverterRegistrar {
    private static final Logger log = LoggerFactory.getLogger(FetchResultConverterRegistrar.class);
    private final List<FetchResultConverter<Object>> fetchResultConverters;

    public FetchResultConverterRegistrar(List<FetchResultConverter<Object>> fetchResultConverters) {
        this.fetchResultConverters = fetchResultConverters;
    }

    @SuppressWarnings("unchecked")
    public void registrarConverters() {
        if (CollectionUtils.isEmpty(fetchResultConverters)) {
            return;
        }
        for (FetchResultConverter<Object> fetchResultConverter : fetchResultConverters) {
            Class<Object> targetType = fetchResultConverter.getTargetType();
            if (targetType == null) {
                List<Class<?>> genericTypes = ReflectUtils.getGenericTypes(fetchResultConverter.getClass());
                if (CollectionUtils.isEmpty(genericTypes)) {
                    throw new IllegalStateException(fetchResultConverter.getClass().getSimpleName() +
                            " converters must have explicit generic types");
                }
                targetType = (Class<Object>) genericTypes.get(0);
            }
            log.debug("Add converter: {}", fetchResultConverter.getClass().getCanonicalName());
            ConverterUtils.putFetchResultConverter(targetType, fetchResultConverter);
        }
    }
}
