package com.zyx.flink.common.config.reader;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.zyx.flink.common.config.environment.FlinkSystemEnvironment;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;

import static com.zyx.flink.common.config.environment.DefaultFlinkSystemEnvironment.DEFAULT_TASK_PROFILE;
import static com.zyx.flink.common.config.reader.SourceFormat.JSON;
import static com.zyx.flink.common.config.reader.SourceFormat.PROPERTIES;
import static com.zyx.flink.common.config.reader.SourceFormat.YAML;
import static com.zyx.flink.common.config.reader.SourceFormat.resolve;
import static jodd.util.StringPool.DASH;

/**
 * Created by zhangyongxiang on 2022/11/30 下午9:23
 **/
public abstract class AbstractFlinkTaskConfigurationReader
        implements FlinkTaskConfigurationReader {
    
    public static final String FLINK_CONFIG_FORMAT = "flink.config.format";
    
    public static final String YML_FILE_SUFFIX = ".yml";
    
    public static final String JSON_FILE_SUFFIX = ".json";
    
    public static final String PROPERTIES_FILE_SUFFIX = ".properties";
    
    @Getter
    private final String flinkTaskName;
    
    @Getter
    private final String profile;
    
    @Getter
    private final SourceFormat format;
    
    private static final Map<SourceFormat, String> CONFIG_FILE_NAME_SUFFIXES = ImmutableMap
            .of(JSON, JSON_FILE_SUFFIX, YAML, YML_FILE_SUFFIX, PROPERTIES,
                    PROPERTIES_FILE_SUFFIX);
    
    protected AbstractFlinkTaskConfigurationReader(
            @NonNull final String flinkTaskName, @NonNull final String profile,
            final SourceFormat format) {
        this.flinkTaskName = flinkTaskName;
        this.profile = profile;
        this.format = format;
    }
    
    protected AbstractFlinkTaskConfigurationReader(
            final FlinkSystemEnvironment flinkSystemEnvironment) {
        // default is yaml
        this(flinkSystemEnvironment.getFlinkTaskName(),
                flinkSystemEnvironment.getProfile(),
                resolve(flinkSystemEnvironment.getArgument(FLINK_CONFIG_FORMAT,
                        YAML.name())));
        
    }
    
    protected String getConfigName() {
        return genConfigFileName(getProfile(), CONFIG_FILE_NAME_SUFFIXES
                .getOrDefault(format, YML_FILE_SUFFIX));
    }
    
    protected String getDefaultConfigName() {
        return genConfigFileName(DEFAULT_TASK_PROFILE, CONFIG_FILE_NAME_SUFFIXES
                .getOrDefault(format, YML_FILE_SUFFIX));
    }
    
    private String genConfigFileName(final String profile,
            final String fileSuffix) {
        if (StringUtils.equals(getProfile(), DEFAULT_TASK_PROFILE)) {
            return getFlinkTaskName() + fileSuffix;
        }
        return getFlinkTaskName() + DASH + profile + fileSuffix;
    }
}
