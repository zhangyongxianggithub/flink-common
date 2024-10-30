package com.zyx.flink.common.utils;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import static jodd.util.StringPool.EQUALS;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Created by zhangyongxiang on 2023/1/5 7:50 PM
 **/
public final class PropertiesUtils {
    
    private PropertiesUtils() {}
    
    public static Pair<String, String> splitProperty(final String property) {
        if (isNotBlank(property)) {
            return Pair.of(
                    trim(defaultIfNull(substringBefore(property, EQUALS),
                            EMPTY)),
                    trim(defaultIfNull(substringAfter(property, EQUALS),
                            EMPTY)));
        }
        return Pair.of(EMPTY, EMPTY);
    }
    
    public static Map<String, String> splitProperties(
            final Collection<String> properties) {
        final Map<String, String> map = Maps.newHashMap();
        if (isNotEmpty(properties)) {
            properties.forEach(property -> {
                final Pair<String, String> keyValue = splitProperty(property);
                if (isNotBlank(keyValue.getKey())) {
                    map.put(keyValue.getKey(), keyValue.getValue());
                }
            });
        }
        return map;
    }
    
}
