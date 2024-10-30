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
@FlinkProperties(rootPath = "/flink/doris/sink")
public class FlinkDorisSink {
    
    private boolean enable = false;
    
    /**
     * doris fe server address
     */
    private String server;
    
    /**
     * doris username
     */
    private String username;
    
    /**
     * doris password
     */
    private String password;
    
    /**
     * doris db name used
     */
    private String db;
    
    /**
     * doris table name used
     */
    private String table;
}
