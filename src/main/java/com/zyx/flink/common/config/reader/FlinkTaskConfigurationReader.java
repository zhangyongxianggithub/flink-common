package com.zyx.flink.common.config.reader;

import java.io.Serializable;

/**
 * Created by zhangyongxiang on 2022/11/30 6:00 PM
 **/
public interface FlinkTaskConfigurationReader extends Serializable, Priority {
    
    FlinkTaskPropertySource read();
}
