package com.zyx.flink.common.sink.kafka;

import java.util.function.Function;

import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/28 22:04
 * @description:
 **/

public class KeyedFlinkKafkaPartitioner<T> extends FlinkKafkaPartitioner<T> {
    
    private static final long serialVersionUID = -8754255137231070149L;
    
    private final Function<T, String> uniqueKeyFunction;
    
    public KeyedFlinkKafkaPartitioner(
            final Function<T, String> uniqueKeyFunction) {
        this.uniqueKeyFunction = uniqueKeyFunction;
    }
    
    @Override
    public int partition(final T record, final byte[] key, final byte[] value,
            final String targetTopic, final int[] partitions) {
        return partitions[Math.abs(uniqueKeyFunction.apply(record).hashCode())
                % partitions.length];
    }
}
