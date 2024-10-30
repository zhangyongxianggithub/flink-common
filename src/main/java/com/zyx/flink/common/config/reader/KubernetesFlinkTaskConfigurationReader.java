package com.zyx.flink.common.config.reader;

import java.io.IOException;
import java.util.Optional;

import com.zyx.flink.common.config.environment.FlinkSystemEnvironment;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.util.Config;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.config.ErrCode.K8S_RES_CLIENT_INITIAL_ERROR;
import static com.zyx.flink.common.config.ErrCode.TASK_CONF_NOT_FOUND_IN_K8S;
import static com.zyx.flink.common.config.reader.SourceType.KUBERNETES;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;

/**
 * Created by zhangyongxiang on 2022/11/30 6:05 PM
 */
@Slf4j
public class KubernetesFlinkTaskConfigurationReader
        extends AbstractFlinkTaskConfigurationReader {
    
    private static final long serialVersionUID = -5504894209015206261L;
    
    public static final String NAMESPACE_ENV_KEY = "FLINK_POD_NAMESPACE";
    
    public static final String DEFAULT_NAMESPACE = "flink";
    
    public static final String FLINK_NAMESPACE_KEY = "flink.k8s.namespace";
    
    /**
     * config map label selector
     */
    private final String label;
    
    private transient CoreV1Api api;
    
    private final String namespace;
    
    public KubernetesFlinkTaskConfigurationReader(
            final FlinkSystemEnvironment flinkSystemEnvironment) {
        super(flinkSystemEnvironment);
        this.label = this.getProfile();
        this.namespace = flinkSystemEnvironment.getArgument(FLINK_NAMESPACE_KEY,
                Optional.ofNullable(System.getenv(NAMESPACE_ENV_KEY))
                        .orElse(DEFAULT_NAMESPACE));
        this.api = getKubeApiClient();
        log.info(
                "create flink task property source reader, flink name: {}, profile: {}, namespace: {}",
                flinkSystemEnvironment.getFlinkTaskName(),
                flinkSystemEnvironment.getProfile(), this.namespace);
    }
    
    private KubernetesFlinkTaskConfigurationReader(final String namespace,
            @NonNull final String flinkTaskName, @NonNull final String profile,
            @NonNull final String label, final SourceFormat format) {
        super(flinkTaskName, profile, format);
        this.label = label;
        this.namespace = namespace;
        this.api = getKubeApiClient();
        log.info(
                "create flink task property source reader, flink name: {}, profile: {}, namespace: {}",
                flinkTaskName, profile, this.namespace);
    }
    
    private KubernetesFlinkTaskConfigurationReader(final String namespace,
            @NonNull final String flinkTaskName, @NonNull final String profile,
            final SourceFormat format) {
        this(namespace, flinkTaskName, profile, profile, format);
    }
    
    private CoreV1Api getKubeApiClient() {
        if (isNull(this.api)) {
            synchronized (this) {
                if (isNull(this.api)) {
                    try {
                        this.api = new CoreV1Api(Config.defaultClient());
                    } catch (final IOException e) {
                        log.error(
                                "fail to create k8s flink configuration reader",
                                e);
                        System.exit(K8S_RES_CLIENT_INITIAL_ERROR.getCode());
                    }
                    
                }
            }
        }
        return this.api;
    }
    
    @Override
    public int getPriority() {
        return 0;
    }
    
    @Override
    public FlinkTaskPropertySource read() {
        final FlinkTaskPropertySource flinkTaskPropertySource = new FlinkTaskPropertySource(
                getFormat(), KUBERNETES);
        try {
            log.info("read namespace: {}, configmap: {}", this.namespace,
                    this.getFlinkTaskName());
            final V1ConfigMap configMap = getKubeApiClient()
                    .readNamespacedConfigMap(this.getFlinkTaskName(),
                            this.namespace, "true");
            Optional.ofNullable(
                    emptyIfNull(configMap.getData()).get(getConfigName()))
                    .ifPresent(flinkTaskPropertySource.getContent()::add);
            Optional.ofNullable(emptyIfNull(configMap.getData())
                    .get(getDefaultConfigName()))
                    .ifPresent(flinkTaskPropertySource.getContent()::add);
            log.info("read namespace: {}, configmap: {}, content:{}",
                    this.namespace, this.getFlinkTaskName(),
                    flinkTaskPropertySource);
        } catch (final ApiException e) {
            log.error(
                    "flink {} configuration not found, code: {}, body: {}, headers: {}, message: {}",
                    this.getFlinkTaskName(), e.getCode(), e.getResponseBody(),
                    e.getResponseHeaders(), e.getMessage(), e.getCause());
            System.exit(TASK_CONF_NOT_FOUND_IN_K8S.getCode());
        }
        return flinkTaskPropertySource;
    }
}
