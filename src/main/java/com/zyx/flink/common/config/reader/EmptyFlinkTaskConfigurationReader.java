package com.zyx.flink.common.config.reader;

import com.zyx.flink.common.config.environment.FlinkSystemEnvironment;

import static com.zyx.flink.common.config.reader.SourceFormat.YAML;
import static com.zyx.flink.common.config.reader.SourceType.EMPTY;

/**
 * Created by zhangyongxiang on 2022/12/1 11:23 AM
 **/
public class EmptyFlinkTaskConfigurationReader
        extends AbstractFlinkTaskConfigurationReader {

    private static final long serialVersionUID = -384066860924164066L;

    public EmptyFlinkTaskConfigurationReader(
            final FlinkSystemEnvironment flinkSystemEnvironment) {
        super(flinkSystemEnvironment);
    }

    @Override
    public FlinkTaskPropertySource read() {
        return new FlinkTaskPropertySource(YAML, EMPTY);
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
