package com.dynamic.sql2.convert;

import com.pengwz.dynamic.sql2.plugins.conversion.FetchResultConverter;
import com.pengwz.dynamic.sql2.utils.CollectionUtils;
import com.pengwz.dynamic.sql2.utils.ConverterUtils;
import com.pengwz.dynamic.sql2.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FetchResultConverterRegistrar {
    private static final Logger log = LoggerFactory.getLogger(FetchResultConverterRegistrar.class);
    private List<FetchResultConverter> fetchResultConverters;

    public FetchResultConverterRegistrar(List<FetchResultConverter> fetchResultConverters) {
        this.fetchResultConverters = fetchResultConverters;
    }

    public void registrarConverters() {
        if (CollectionUtils.isEmpty(fetchResultConverters)) {
            return;
        }
        for (FetchResultConverter fetchResultConverter : fetchResultConverters) {
            List<Class<?>> genericTypes = ReflectUtils.getGenericTypes(fetchResultConverter.getClass());
            if (CollectionUtils.isEmpty(genericTypes)) {
                throw new IllegalStateException(fetchResultConverter.getClass().getSimpleName() +
                        " converters must have explicit generic types");
            }
            log.debug("Add converter: {}", fetchResultConverter.getClass().getCanonicalName());
            ConverterUtils.putFetchResultConverter(genericTypes.get(0), fetchResultConverter);
        }
    }
}
