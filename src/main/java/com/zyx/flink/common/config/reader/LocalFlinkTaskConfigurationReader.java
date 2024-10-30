package com.zyx.flink.common.config.reader;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.zyx.flink.common.config.environment.FlinkSystemEnvironment;

import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.config.ErrCode.TASK_CONF_NOT_FOUND_IN_LOCAL;
import static com.zyx.flink.common.config.reader.SourceType.LOCAL;

/**
 * Created by zhangyongxiang on 2022/12/1 11:21 AM
 **/
@Slf4j
public class LocalFlinkTaskConfigurationReader
        extends AbstractFlinkTaskConfigurationReader {
    
    public static final String FLINK_LOCAL_CONFIG_PATH_KEY = "flink.local.config-path";
    
    private final String yamlPath;
    
    private static final long serialVersionUID = 174109306661057846L;
    
    public LocalFlinkTaskConfigurationReader(
            final FlinkSystemEnvironment flinkSystemEnvironment) {
        super(flinkSystemEnvironment);
        this.yamlPath = flinkSystemEnvironment.getArgument(
                FLINK_LOCAL_CONFIG_PATH_KEY, "/etc/" + getConfigName());
    }
    
    @Override
    public FlinkTaskPropertySource read() {
        final FlinkTaskPropertySource flinkTaskPropertySource = new FlinkTaskPropertySource(
                getFormat(), LOCAL);
        
        try {
            final String content = new String(
                    Files.readAllBytes(Paths.get(URI.create(this.yamlPath))));
            flinkTaskPropertySource.getContent().add(content);
        } catch (final Exception e) {
            log.error("local config file {} not found or fail to read",
                    this.yamlPath);
            System.exit(TASK_CONF_NOT_FOUND_IN_LOCAL.getCode());
        }
        log.info("read local config source file path: {}, content: {}",
                this.yamlPath, flinkTaskPropertySource);
        return flinkTaskPropertySource;
    }
    
    @Override
    public int getPriority() {
        return 0;
    }
}
