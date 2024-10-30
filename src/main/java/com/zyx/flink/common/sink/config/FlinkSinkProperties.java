package com.zyx.flink.common.sink.config;

import lombok.Data;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/26 16:20
 * @description:
 **/
@Data
public class FlinkSinkProperties<T> {
    
    private T sink;
}
