package com.zyx.flink.common.sql;

/**
 * Created by zhangyongxiang on 2023/1/6 5:08 PM
 **/
public interface FlinkSqlFactory<T> {
    
    String getSql(T model);
    
}
