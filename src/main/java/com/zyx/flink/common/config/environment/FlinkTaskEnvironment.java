package com.zyx.flink.common.config.environment;

import java.io.Serializable;
import java.util.Optional;

/**
 * Created by zhangyongxiang on 2022/11/30 下午8:59
 **/
public interface FlinkTaskEnvironment extends Serializable {
    
    <T> T createPropertiesObject(Class<T> propertiesObjectClass);
    
    String getFlinkTaskName();
    
    String getProfile();
    
    Optional<String> getProperty(String key);
    
    <T> Optional<T> getTypedProperty(Class<T> type, String key);
}
