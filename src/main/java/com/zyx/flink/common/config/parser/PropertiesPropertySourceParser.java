package com.zyx.flink.common.config.parser;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

/**
 * Created by zhangyongxiang on 2022/11/30 下午9:06
 **/
@Slf4j
public class PropertiesPropertySourceParser implements PropertySourceParser {
    
    private static final long serialVersionUID = 1823285854676354190L;
    
    private final ImmutableList<String> propertyContents;
    
    public PropertiesPropertySourceParser(final List<String> propertyContents) {
        this.propertyContents = ImmutableList
                .copyOf(emptyIfNull(propertyContents));
        // TODO implement in the future
    }
    
    @Override
    public <T> T createAnnotatedPropertiesObject(
            final Class<T> propertiesObjectClass) {
        try {
            final T object = propertiesObjectClass.newInstance();
            return object;
        } catch (final Exception e) {
            
        }
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
