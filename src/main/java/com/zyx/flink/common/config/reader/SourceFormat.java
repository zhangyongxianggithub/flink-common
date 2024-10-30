package com.zyx.flink.common.config.reader;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by zhangyongxiang on 2022/11/30 6:03 PM
 **/
public enum SourceFormat {
    
    YAML, PROPERTIES, JSON, UNKNOWN;
    
    public static SourceFormat resolve(final String format) {
        return Arrays.stream(SourceFormat.values()).filter(
                type -> StringUtils.equalsIgnoreCase(format, type.name()))
                .findAny().orElse(YAML);
    }
}
