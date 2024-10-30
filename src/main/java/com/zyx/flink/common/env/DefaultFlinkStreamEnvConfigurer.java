package com.zyx.flink.common.env;

import java.util.Optional;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.zyx.flink.common.config.environment.FlinkTaskEnvironment;

import lombok.extern.slf4j.Slf4j;

import static org.apache.flink.streaming.api.CheckpointingMode.EXACTLY_ONCE;
import static org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 20:55
 * @description:
 **/
@Slf4j
public class DefaultFlinkStreamEnvConfigurer
        implements FlinkStreamEnvConfigurer {
    
    public static final String FLINK_CHECKPOINT_DIR = "flink.storage-dir";
    
    private final FlinkTaskEnvironment flinkTaskEnvironment;
    
    public DefaultFlinkStreamEnvConfigurer(
            final FlinkTaskEnvironment flinkTaskEnvironment) {
        this.flinkTaskEnvironment = flinkTaskEnvironment;
    }
    
    @Override
    public void configure(final StreamExecutionEnvironment env) {
        env.enableCheckpointing(Time.minutes(10).toMilliseconds());
        env.getConfig().enableForceKryo();
        env.getCheckpointConfig().setCheckpointingMode(EXACTLY_ONCE);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(
                Runtime.getRuntime().availableProcessors());
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(10);
        env.getCheckpointConfig()
                .setCheckpointTimeout(Time.seconds(120).toMilliseconds());
        env.getCheckpointConfig()
                .enableExternalizedCheckpoints(DELETE_ON_CANCELLATION);
        env.setRestartStrategy(RestartStrategies.failureRateRestart(3,
                Time.minutes(5), Time.seconds(10)));
        final Optional<String> dir = this.flinkTaskEnvironment
                .getProperty(FLINK_CHECKPOINT_DIR);
        if (dir.isPresent()) {
            log.info("enable checkpoint storage, dir: {}", dir.get());
            env.getCheckpointConfig().setCheckpointStorage(dir.get());
        }
        // the order of call matter the config of env
        customize(env);
        customize(env.getConfig());
        customize(env.getCheckpointConfig());
    }
}
