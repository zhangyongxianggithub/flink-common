package com.zyx.flink.common.config.reader;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.compress.utils.Lists;

import lombok.Data;

/**
 * Created by zhangyongxiang on 2022/11/30 6:02 PM
 **/
@Data
public class FlinkTaskPropertySource implements Serializable {
    
    private static final long serialVersionUID = -751549938162377522L;
    
    private SourceFormat format;
    
    private SourceType sourceType;
    
    private List<String> content = Lists.newArrayList();
    
    public FlinkTaskPropertySource(final SourceFormat format,
            final SourceType sourceType) {
        this.format = format;
        this.sourceType = sourceType;
    }
}
