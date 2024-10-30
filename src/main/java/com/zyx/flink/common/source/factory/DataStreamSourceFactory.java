package com.zyx.flink.common.source.factory;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 21:07
 * @description:
 **/

public interface DataStreamSourceFactory<T> {
    
    DataStreamSource<T> createStream(StreamExecutionEnvironment env);
    
    String streamSourceName(final StreamExecutionEnvironment env);
}
