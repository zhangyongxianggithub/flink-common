package com.zyx.flink.common.sink.config;

import com.zyx.flink.common.config.parser.FlinkProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/26 16:16
 * @description:
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FlinkProperties(rootPath = "/flink/mysql/sink")
public class FlinkMysqlSink {
    
    private boolean enable = false;
    
    private String driverClassName;
    
    private String url;
    
    private String username;
    
    private String password;
}
