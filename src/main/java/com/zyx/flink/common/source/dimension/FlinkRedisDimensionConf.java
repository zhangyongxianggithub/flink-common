package com.zyx.flink.common.source.dimension;

import com.zyx.flink.common.config.parser.FlinkProperties;
import com.zyx.flink.common.config.parser.PropertiesAvailable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/29 14:29
 * @description:
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FlinkProperties(rootPath = "/spring/redis")
public class FlinkRedisDimensionConf implements PropertiesAvailable {
    
    /**
     * standalone config
     */
    private FlinkStandaloneRedisDimensionConf standalone = new FlinkStandaloneRedisDimensionConf();
    
    /**
     * cluster config
     */
    private FlinkClusterRedisDimensionConf cluster = new FlinkClusterRedisDimensionConf();
    
    @Override
    public boolean isAvailable() {
        return true;
    }
}
