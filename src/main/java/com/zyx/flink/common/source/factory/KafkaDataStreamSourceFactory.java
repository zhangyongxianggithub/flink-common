package com.zyx.flink.common.source.factory;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;

import com.zyx.flink.common.config.environment.FlinkTaskEnvironment;
import com.zyx.flink.common.source.config.FlinkKafkaSource;

import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.config.ErrCode.MISSING_KAFKA_CONF_ERROR;
import static java.util.Objects.nonNull;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 21:08
 * @description:
 **/
@Slf4j
public abstract class KafkaDataStreamSourceFactory<K, V>
        implements DataStreamSourceFactory<Tuple2<K, V>> {
    
    private final FlinkTaskEnvironment flinkTaskEnvironment;
    
    protected KafkaDataStreamSourceFactory(
            final FlinkTaskEnvironment flinkTaskEnvironment) {
        this.flinkTaskEnvironment = flinkTaskEnvironment;
    }
    
    @Override
    public DataStreamSource<Tuple2<K, V>> createStream(
            final StreamExecutionEnvironment env) {
        final FlinkKafkaSource flinkKafkaSource = this.flinkTaskEnvironment
                .createPropertiesObject(FlinkKafkaSource.class);
        if (nonNull(flinkKafkaSource) && flinkKafkaSource.isValid()) {
            log.info("kafka source topic: {},  config: {}",
                    flinkKafkaSource.getTopic(),
                    flinkKafkaSource.getKafkaProperties());
            final FlinkKafkaConsumer<Tuple2<K, V>> alarmConsumer = new FlinkKafkaConsumer<>(
                    flinkKafkaSource.getTopic(), schema(),
                    flinkKafkaSource.getKafkaProperties());
            alarmConsumer.setCommitOffsetsOnCheckpoints(
                    env.getCheckpointConfig().isCheckpointingEnabled());
            return env.addSource(alarmConsumer, streamSourceName(env));
        } else {
            log.error(
                    "missing flink kafka configuration, can't create data stream source");
            System.exit(MISSING_KAFKA_CONF_ERROR.getCode());
        }
        return null;
    }
    
    protected abstract KafkaDeserializationSchema<Tuple2<K, V>> schema();
    
    @Override
    public String streamSourceName(final StreamExecutionEnvironment env) {
        return this.flinkTaskEnvironment.getFlinkTaskName() + ".kafka.source";
    }
}
