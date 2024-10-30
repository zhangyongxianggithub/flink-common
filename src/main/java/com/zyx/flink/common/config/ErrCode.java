package com.zyx.flink.common.config;

/**
 * Created by zhangyongxiang on 2023/1/6 5:21 PM
 **/
public enum ErrCode {
    /**
     * task run
     */
    TASK_RUN_ERROR(2, "stream task run failed"),
    /**
     * missing flink kafka configuration
     */
    MISSING_KAFKA_CONF_ERROR(3, "missing flink kafka configuration"),
    
    K8S_RES_CLIENT_INITIAL_ERROR(5, "fail to initialize k8s api client"),
    
    TASK_CONF_NOT_FOUND_IN_K8S(6, "task config not found in k8s configmap"),
    
    TASK_CONF_NOT_FOUND_IN_LOCAL(7, "task config not found in local"),
    
    TASK_CONF_NOT_FOUND_IN_CLASSPATH(8, "task config not found in classpath"),
    /**
     * fail generate sql script
     */
    SQL_TEMPLATE_ERROR(9,
            "fail to parse template or fail to interpolate model in template"),
    /**
     * empty sql
     */
    SQL_EMPTY_ERROR(10, "empty statement, nothing to be executed");
    
    private final int code;
    
    private final String message;
    
    ErrCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return this.code;
    }
    
    public String getMessage() {
        return this.message;
    }
}
