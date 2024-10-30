package com.zyx.flink.common.sink;

import java.io.Serializable;
import java.util.Properties;
import java.util.function.Function;

import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.doris.flink.cfg.DorisSink;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import com.zyx.flink.common.sink.config.FlinkDorisSink;
import com.zyx.flink.common.sink.config.FlinkKafkaSink;
import com.zyx.flink.common.sink.config.FlinkMysqlSink;
import com.zyx.flink.common.sink.kafka.JsonSerializationSchema;
import com.zyx.flink.common.sink.kafka.KeyedFlinkKafkaPartitioner;
import com.zyx.flink.common.utils.JsonSnakeCaseUtils;
import com.zyx.flink.common.config.environment.FlinkTaskEnvironment;

import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static jodd.util.StringPool.DOT;
import static org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic.AT_LEAST_ONCE;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/28 20:32
 * @description:
 **/
@Slf4j
public class SinkManager<T> implements Serializable {
    
    private static final long serialVersionUID = -2543625989479205170L;
    
    private final FlinkTaskEnvironment flinkTaskEnvironment;
    
    public SinkManager(final FlinkTaskEnvironment flinkTaskEnvironment) {
        this.flinkTaskEnvironment = flinkTaskEnvironment;
    }
    
    public SinkManager<T> addKafkaSink(final DataStream<T> stream,
            final Function<T, String> key) {
        final FlinkKafkaSink flinkKafkaSink = this.flinkTaskEnvironment
                .createPropertiesObject(FlinkKafkaSink.class);
        if (nonNull(flinkKafkaSink) && flinkKafkaSink.isEnable()) {
            log.info("kafka sink topic: {},  config: {}",
                    flinkKafkaSink.getTopic(),
                    flinkKafkaSink.getKafkaProperties());
            
            final FlinkKafkaProducer<T> alarmProducer = new FlinkKafkaProducer<>(
                    flinkKafkaSink.getTopic(), new JsonSerializationSchema<>(),
                    flinkKafkaSink.getKafkaProperties(),
                    isNull(key) ? null : new KeyedFlinkKafkaPartitioner<>(key),
                    AT_LEAST_ONCE, stream.getParallelism());
            stream.addSink(alarmProducer)
                    .name(this.flinkTaskEnvironment.getFlinkTaskName()
                            + ".kafka.sink");
        } else {
            log.error(
                    "fail to add kafka sink, missing kafka configuration or disable");
        }
        return this;
        
    }
    
    public SinkManager<T> addDorisSink(final DataStream<T> stream) {
        final FlinkDorisSink flinkDorisSink = this.flinkTaskEnvironment
                .createPropertiesObject(FlinkDorisSink.class);
        if (nonNull(flinkDorisSink) && flinkDorisSink.isEnable()) {
            
            final DorisOptions.Builder dorisBuilder = DorisOptions.builder();
            dorisBuilder.setFenodes(flinkDorisSink.getServer())
                    .setTableIdentifier(flinkDorisSink.getDb() + DOT
                            + flinkDorisSink.getTable())
                    .setUsername(flinkDorisSink.getUsername())
                    .setPassword(flinkDorisSink.getPassword());
            
            final DorisExecutionOptions.Builder executionBuilder = DorisExecutionOptions
                    .builder();
            final Properties streamLoadSetting = new Properties();
            streamLoadSetting.setProperty("format", "json");
            // streamLoadSetting.setProperty("label",
            // flinkTaskEnvironment.getFlinkTaskName() + System.nanoTime());
            streamLoadSetting.setProperty("two_phase_commit", "false");
            streamLoadSetting.setProperty("strip_outer_array", "true");
            executionBuilder.setEnableDelete(true)
                    .setBatchSize(Integer.MAX_VALUE).setBatchIntervalMs(100L)
                    .setStreamLoadProp(streamLoadSetting);
            
            stream.map(JsonSnakeCaseUtils::toJson)
                    .addSink(DorisSink.sink(DorisReadOptions.builder().build(),
                            executionBuilder.build(), dorisBuilder.build()))
                    .name(this.flinkTaskEnvironment.getFlinkTaskName()
                            + ".doris.sink");
        } else {
            log.error(
                    "fail to add doris sink, missing doris configuration or disabled");
        }
        return this;
        
    }
    
    public SinkManager<T> addMysqlSink(final DataStream<T> stream,
            final SqlStatementBuilder<T> sqlStatementBuilder) {
        final FlinkMysqlSink flinkMysqlSink = this.flinkTaskEnvironment
                .createPropertiesObject(FlinkMysqlSink.class);
        if (nonNull(flinkMysqlSink) && flinkMysqlSink.isEnable()) {
            final JdbcConnectionOptions jdbcConnectionOptions = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl(flinkMysqlSink.getUrl())
                    .withDriverName(flinkMysqlSink.getDriverClassName())
                    .withUsername(flinkMysqlSink.getUsername())
                    .withPassword(flinkMysqlSink.getPassword()).build();
            stream.addSink(JdbcSink.sink(sqlStatementBuilder.getSql(),
                    sqlStatementBuilder, jdbcConnectionOptions))
                    .name(this.flinkTaskEnvironment.getFlinkTaskName()
                            + ".mysql.sink");
        } else {
            log.error(
                    "fail to add mysql sink, missing mysql configuration or disabled");
        }
        return this;
        
    }
    
}
