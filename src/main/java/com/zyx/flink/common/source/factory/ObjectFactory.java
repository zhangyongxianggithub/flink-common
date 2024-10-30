package com.zyx.flink.common.source.factory;

import com.zyx.flink.common.config.environment.FlinkTaskEnvironment;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 21:00
 * @description:
 **/

public interface ObjectFactory<T> {
    
    T createObject(FlinkTaskEnvironment flinkTaskEnvironment);
    
}
