package com.zyx.flink.common.config.environment;

import java.util.Optional;

import com.zyx.flink.common.config.parser.PropertySourceParser;
import com.zyx.flink.common.config.reader.FlinkTaskPropertySource;

import lombok.NonNull;

/**
 * Created by zhangyongxiang on 2022/11/30 下午9:01
 **/
public class AnnotationConfigurationFlinkTaskEnvironment
        implements FlinkTaskEnvironment {
    
    private static final long serialVersionUID = -1875168895705490850L;
    
    private final FlinkSystemEnvironment flinkSystemEnvironment;
    
    private final PropertySourceParser propertySourceParser;
    
    public AnnotationConfigurationFlinkTaskEnvironment(
            @NonNull final FlinkSystemEnvironment flinkSystemEnvironment,
            @NonNull final FlinkTaskPropertySource flinkTaskPropertySource) {
        this.flinkSystemEnvironment = flinkSystemEnvironment;
        
        this.propertySourceParser = new PropertySourceParser.PropertySourceParserBuilder()
                .format(flinkTaskPropertySource.getFormat())
                .content(flinkTaskPropertySource.getContent()
                        .toArray(new String[0]))
                .build();
    }
    
    @Override
    public <T> T createPropertiesObject(final Class<T> propertiesObjectClass) {
        return this.propertySourceParser
                .createAnnotatedPropertiesObject(propertiesObjectClass);
    }
    
    @Override
    public String getFlinkTaskName() {
        return this.flinkSystemEnvironment.getFlinkTaskName();
    }
    
    @Override
    public String getProfile() {
        return this.flinkSystemEnvironment.getProfile();
    }
    
    @Override
    public Optional<String> getProperty(final String key) {
        return this.propertySourceParser.getProperty(key);
    }
    
    @Override
    public <T> Optional<T> getTypedProperty(final Class<T> type,
            final String key) {
        return this.propertySourceParser.getTypedProperty(type, key);
    }
}
