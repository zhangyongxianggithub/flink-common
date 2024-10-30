package com.zyx.flink.common.env;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 20:54
 * @description:
 **/

public interface FlinkStreamEnvConfigurer {
    
    void configure(StreamExecutionEnvironment environment);
    
    default void customize(final StreamExecutionEnvironment environment) {}
    
    default void customize(final CheckpointConfig checkpointConfig) {}
    
    default void customize(final ExecutionConfig executionConfig) {}
    
}
