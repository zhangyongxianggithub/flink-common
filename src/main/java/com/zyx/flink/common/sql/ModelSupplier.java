package com.zyx.flink.common.sql;

import com.zyx.flink.common.config.environment.FlinkTaskEnvironment;
import com.zyx.flink.common.deserialization.SerializableFunction;

/**
 * Created by zhangyongxiang on 2023/1/6 5:32 PM
 **/
public interface ModelSupplier<T>
        extends SerializableFunction<FlinkTaskEnvironment, T> {}
