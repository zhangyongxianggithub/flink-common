package com.zyx.flink.common.config.reader;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.zyx.flink.common.config.environment.FlinkSystemEnvironment;

import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.config.ErrCode.TASK_CONF_NOT_FOUND_IN_CLASSPATH;
import static com.zyx.flink.common.config.reader.SourceType.CLASSPATH;

/**
 * Created by zhangyongxiang on 2022/12/1 11:22 AM
 **/
@Slf4j
public class ClasspathFlinkTaskConfigurationReader
        extends AbstractFlinkTaskConfigurationReader {
    
    private static final long serialVersionUID = -4445035384620790999L;
    
    public static final String FLINK_CLASSPATH_CONFIG_RESOURCE = "flink.classpath.config-resource";
    
    private final String resourceName;
    
    public ClasspathFlinkTaskConfigurationReader(
            final FlinkSystemEnvironment flinkSystemEnvironment) {
        super(flinkSystemEnvironment);
        this.resourceName = flinkSystemEnvironment
                .getArgument(FLINK_CLASSPATH_CONFIG_RESOURCE, getConfigName());
    }
    
    @Override
    public FlinkTaskPropertySource read() {
        final FlinkTaskPropertySource flinkTaskPropertySource = new FlinkTaskPropertySource(
                getFormat(), CLASSPATH);
        try {
            final InputStream inputStream = this.getClass().getClassLoader()
                    .getResourceAsStream(this.resourceName);
            final String content = IOUtils.toString(inputStream,
                    Charset.defaultCharset());
            flinkTaskPropertySource.getContent().add(content);
        } catch (final Exception e) {
            log.error("fail to read classpath resource config source {}",
                    this.resourceName);
            System.exit(TASK_CONF_NOT_FOUND_IN_CLASSPATH.getCode());
        }
        log.info("read flink property source, resource name: {}, content: {}",
                this.resourceName, flinkTaskPropertySource);
        return flinkTaskPropertySource;
    }
    
    @Override
    public int getPriority() {
        return 0;
    }
}
