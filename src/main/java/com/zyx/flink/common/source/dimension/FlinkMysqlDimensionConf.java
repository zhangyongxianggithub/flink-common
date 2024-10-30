package com.zyx.flink.common.source.dimension;

import com.zyx.flink.common.config.parser.FlinkProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 16:42
 * @description:
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FlinkProperties(rootPath = "/spring/datasource")
public class FlinkMysqlDimensionConf {
    
    private String driverClassName;
    
    private String url;
    
    private String username;
    
    private String password;
    
}
