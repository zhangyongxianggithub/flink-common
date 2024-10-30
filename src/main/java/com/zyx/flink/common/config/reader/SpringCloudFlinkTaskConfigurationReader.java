package com.zyx.flink.common.config.reader;

import org.apache.commons.lang3.StringUtils;

import com.zyx.flink.common.config.environment.FlinkSystemEnvironment;

import feign.Feign;
import feign.codec.StringDecoder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.config.reader.SourceType.SPRING_CLOUD;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class SpringCloudFlinkTaskConfigurationReader
        extends AbstractFlinkTaskConfigurationReader {
    
    public static final String FLINK_CONFIG_SERVER_KEY = "flink.config.server";
    
    public static final String DEFAULT_CONFIG_SERVER = "http://10.162.195.165:8862";
    
    private static final long serialVersionUID = -5504894209015206265L;
    
    public static final String HTTP_PROTOCOL = "http";
    
    public static final String HTTP_PROTOCOL_URI_PREFIX = HTTP_PROTOCOL + "://";
    
    private final String configServer;
    
    private final String label;
    
    public SpringCloudFlinkTaskConfigurationReader(
            final FlinkSystemEnvironment flinkSystemEnvironment) {
        super(flinkSystemEnvironment);
        final String server = flinkSystemEnvironment
                .getArgument(FLINK_CONFIG_SERVER_KEY, DEFAULT_CONFIG_SERVER);
        this.configServer = rectify(server);
        this.label = getProfile();
        log.info("create flink task properties reader by system environment, "
                + "config server: {}, flink name: {}, profile: {}, label: {}",
                this.configServer, this.getFlinkTaskName(), this.getProfile(),
                this.label);
    }
    
    private SpringCloudFlinkTaskConfigurationReader(
            @NonNull final String configServer,
            @NonNull final String flinkTaskName, @NonNull final String profile,
            @NonNull final String label, final SourceFormat format) {
        super(flinkTaskName, profile, format);
        this.configServer = configServer;
        this.label = label;
        log.info("create flink task properties reader by args, "
                + "config server: {}, flink name: {}, profile: {}, label: {}",
                configServer, flinkTaskName, profile, label);
    }
    
    private SpringCloudFlinkTaskConfigurationReader(
            @NonNull final String configServer,
            @NonNull final String flinkTaskName, @NonNull final String profile,
            final SourceFormat format) {
        this(configServer, flinkTaskName, profile, profile, format);
    }
    
    private String rectify(final String server) {
        
        if (!StringUtils.startsWith(server, HTTP_PROTOCOL)) {
            return HTTP_PROTOCOL_URI_PREFIX + server;
        }
        return server;
    }
    
    @Override
    public int getPriority() {
        return -1;
    }
    
    @Override
    public FlinkTaskPropertySource read() {
        final SpringCloudConfigClient springCloudConfigClient = Feign.builder()
                .decoder(new StringDecoder())
                .target(SpringCloudConfigClient.class, this.configServer);
        final FlinkTaskPropertySource flinkTaskPropertySource = new FlinkTaskPropertySource(
                getFormat(), SPRING_CLOUD);
        final String content;
        if (isNotBlank(this.label)) {
            content = springCloudConfigClient.fetchFlinkTaskConfig(
                    this.getFlinkTaskName(), this.getProfile(), this.label);
        } else {
            content = springCloudConfigClient.fetchFlinkTaskConfig(
                    this.getFlinkTaskName(), this.getProfile());
        }
        if (isNotBlank(content)) {
            flinkTaskPropertySource.getContent().add(content);
        }
        log.info(
                "fetch flink task {}'s label {} config from config center {} successfully:\n{}",
                this.getFlinkTaskName(), this.label, this.configServer,
                content);
        return flinkTaskPropertySource;
        
    }
}
