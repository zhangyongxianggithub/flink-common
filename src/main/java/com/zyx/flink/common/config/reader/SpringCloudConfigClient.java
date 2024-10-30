package com.zyx.flink.common.config.reader;

import feign.Param;
import feign.RequestLine;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 15:45
 * @description:
 **/

public interface SpringCloudConfigClient {
    
    @RequestLine("GET /{label}/{flinkTaskName}-{profile}.yml")
    String fetchFlinkTaskConfig(@Param("flinkTaskName") String flinkTaskName,
            @Param("profile") String profile, @Param("label") String label);
    
    @RequestLine("GET /{profile}/{flinkTaskName}-{profile}.yml")
    String fetchFlinkTaskConfig(@Param("flinkTaskName") String flinkTaskName,
            @Param("profile") String profile);
}
