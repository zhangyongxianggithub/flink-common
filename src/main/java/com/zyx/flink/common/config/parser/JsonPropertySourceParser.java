package com.zyx.flink.common.config.parser;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

/**
 * Created by zhangyongxiang on 2022/12/1 11:40 AM
 **/
public class JsonPropertySourceParser implements PropertySourceParser {
    
    private static final long serialVersionUID = -5692166109213542783L;
    
    private final ImmutableList<String> propertyContents;
    
    public JsonPropertySourceParser(final List<String> propertyContents) {
        this.propertyContents = ImmutableList
                .copyOf(emptyIfNull(propertyContents));
    }
    
    @Override
    public <T> T createAnnotatedPropertiesObject(
            final Class<T> propertiesObjectClass) {
        return null;
    }
    
    @Override
    public Optional<String> getProperty(final String key) {
        return Optional.empty();
    }
    
    @Override
    public <T> Optional<T> getTypedProperty(final Class<T> type,
            final String key) {
        return Optional.empty();
    }
}
