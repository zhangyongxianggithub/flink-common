package com.zyx.flink.common.config.reader;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by zhangyongxiang on 2022/11/30 6:03 PM
 **/
public enum SourceType {
    KUBERNETES, SPRING_CLOUD, EMPTY, LOCAL, CLASSPATH;
    
    public static SourceType resolve(String environment) {
        return Arrays.stream(SourceType.values()).filter(
                type -> StringUtils.equalsIgnoreCase(environment, type.name()))
                .findAny().orElse(EMPTY);
    }
}
