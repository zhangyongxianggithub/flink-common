package com.zyx.flink.common.config.environment;

import java.io.Serializable;

/**
 * Created by zhangyongxiang on 2022/11/30 下午9:17
 **/
public interface FlinkSystemEnvironment extends Serializable {
    
    String getArgument(String variableName);
    
    String getArgument(String variableName, String defaultValue);
    
    String getFlinkTaskName();
    
    String getProfile();
}
