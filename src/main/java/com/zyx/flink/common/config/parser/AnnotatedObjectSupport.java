package com.zyx.flink.common.config.parser;

import java.io.Serializable;

/**
 * Created by zhangyongxiang on 2022/12/1 11:42 AM
 **/
public interface AnnotatedObjectSupport extends Serializable {
    
    <T> T createAnnotatedPropertiesObject(Class<T> propertiesObjectClass);
}
