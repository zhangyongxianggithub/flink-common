package com.zyx.flink.common.source.dimension;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

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
public class FlinkClusterRedisDimensionConf {
    
    private boolean enable = false;
    
    private String password;
    
    private List<String> nodes = Lists.newArrayList();
    
}