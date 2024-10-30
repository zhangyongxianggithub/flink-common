package com.zyx.flink.common.source.dimension;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/29 14:46
 * @description:
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlinkStandaloneRedisDimensionConf {
    
    private boolean enable = false;
    
    private String host;
    
    private Integer port;
    
    private String password;
    
    private int database = 0;
}
