package com.zyx.flink.common.config.environment;

import org.apache.flink.api.java.utils.ParameterTool;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.removeEndIgnoreCase;

/**
 * Created by zhangyongxiang on 2022/11/30 下午9:19
 **/
public class DefaultFlinkSystemEnvironment implements FlinkSystemEnvironment {
    
    public static final String FLINK_TASK_NAME_KEY = "flink.task.name";
    
    public static final String FLINK_TASK_PROFILE_KEY = "flink.task.profile";
    
    public static final String DEFAULT_TASK_PROFILE = "default";
    
    private static final long serialVersionUID = 317039273861897150L;
    
    private final Class<?> mainClass;
    
    private final String[] args;
    
    private final ParameterTool parameters;
    
    private final String flinkTaskName;
    
    private final String profile;
    
    public DefaultFlinkSystemEnvironment(final Class<?> mainClass,
            final String[] args) {
        this.mainClass = mainClass;
        this.args = args;
        this.parameters = ParameterTool.fromArgs(args);
        // get task name, remove suffixed task
        this.flinkTaskName = getArgument(FLINK_TASK_NAME_KEY, UPPER_CAMEL.to(
                LOWER_HYPHEN,
                removeEndIgnoreCase(mainClass.getSimpleName(), "task")));
        // get task specified profile to lookup appropriate property file
        this.profile = getArgument(FLINK_TASK_PROFILE_KEY,
                DEFAULT_TASK_PROFILE);
    }
    
    @Override
    public String getArgument(final String variableName) {
        return getArgument(variableName, null);
    }
    
    @Override
    public String getArgument(final String variableName,
            final String defaultValue) {
        // use command line args first, then system property, then system env,
        // or null returned
        return ofNullable(this.parameters.get(variableName))
                .orElseGet(() -> ofNullable(System.getProperty(variableName))
                        .orElseGet(() -> ofNullable(System.getenv(variableName))
                                .orElse(defaultValue)));
    }
    
    @Override
    public String getFlinkTaskName() {
        return this.flinkTaskName;
    }
    
    @Override
    public String getProfile() {
        return this.profile;
    }
}
