package com.zyx.flink.common.config;

import java.io.Serializable;

import com.zyx.flink.common.config.environment.AnnotationConfigurationFlinkTaskEnvironment;
import com.zyx.flink.common.config.environment.DefaultFlinkSystemEnvironment;
import com.zyx.flink.common.config.environment.FlinkSystemEnvironment;
import com.zyx.flink.common.config.environment.FlinkTaskEnvironment;
import com.zyx.flink.common.config.reader.ClasspathFlinkTaskConfigurationReader;
import com.zyx.flink.common.config.reader.EmptyFlinkTaskConfigurationReader;
import com.zyx.flink.common.config.reader.FlinkTaskConfigurationReader;
import com.zyx.flink.common.config.reader.KubernetesFlinkTaskConfigurationReader;
import com.zyx.flink.common.config.reader.LocalFlinkTaskConfigurationReader;
import com.zyx.flink.common.config.reader.SourceType;
import com.zyx.flink.common.config.reader.SpringCloudFlinkTaskConfigurationReader;

import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.config.reader.SourceType.resolve;

/**
 * Created by zhangyongxiang on 2022/11/30 下午9:07
 **/
@Slf4j
public class FlinkTaskEnvironmentBuilder implements Serializable {
    
    public static final String FLINK_ENVIRONMENT_KEY = "flink.environment";
    
    private static final long serialVersionUID = 5176291002855824758L;
    
    private final SourceType environment;
    
    private final FlinkSystemEnvironment flinkSystemEnvironment;

    public FlinkTaskEnvironmentBuilder(final Class<?> mainClass,
            final String[] args) {
        this.flinkSystemEnvironment = new DefaultFlinkSystemEnvironment(
                mainClass, args);
        final String env = this.flinkSystemEnvironment
                .getArgument(FLINK_ENVIRONMENT_KEY);
        this.environment = resolve(env);
        log.info("current flink task run environment: {}", this.environment);
    }
    
    public FlinkTaskEnvironment build() {
        final FlinkTaskConfigurationReader flinkTaskConfigurationReader;
        switch (this.environment) {
            case KUBERNETES:
                flinkTaskConfigurationReader = new KubernetesFlinkTaskConfigurationReader(
                        this.flinkSystemEnvironment);
                break;
            case SPRING_CLOUD:
                flinkTaskConfigurationReader = new SpringCloudFlinkTaskConfigurationReader(
                        this.flinkSystemEnvironment);
                break;
            case EMPTY:
                flinkTaskConfigurationReader = new EmptyFlinkTaskConfigurationReader(
                        this.flinkSystemEnvironment);
                break;
            case CLASSPATH:
                flinkTaskConfigurationReader = new ClasspathFlinkTaskConfigurationReader(
                        this.flinkSystemEnvironment);
                break;
            case LOCAL:
                flinkTaskConfigurationReader = new LocalFlinkTaskConfigurationReader(
                        this.flinkSystemEnvironment);
                break;
            default:
                log.error("unsupported flink environment, check your argument: "
                        + FLINK_ENVIRONMENT_KEY);
                throw new IllegalArgumentException(
                        "unknown flink environment: " + this.environment);
        }
        return new AnnotationConfigurationFlinkTaskEnvironment(
                this.flinkSystemEnvironment,
                flinkTaskConfigurationReader.read());
    }
}
