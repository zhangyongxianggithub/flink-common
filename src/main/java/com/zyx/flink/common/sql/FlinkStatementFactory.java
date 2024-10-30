package com.zyx.flink.common.sql;

import java.util.List;

import com.zyx.flink.common.config.environment.FlinkTaskEnvironment;

/**
 * Created by zhangyongxiang on 2023/1/6 2:29 AM
 **/
public interface FlinkStatementFactory<T> {
    
    List<String> getStatements(FlinkTaskEnvironment flinkTaskEnvironment,
            ModelSupplier<T> modelSupplier);
    
}
