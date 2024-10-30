package com.zyx.flink.common.sink.kafka;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.util.function.SerializableSupplier;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import static com.zyx.flink.common.utils.JsonUtils.fromJson;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 19:59
 * @description:
 **/

public class StringKeyJsonValueDeserializationSchema<V>
        implements KafkaDeserializationSchema<Tuple2<String, V>> {
    
    private static final long serialVersionUID = 8547839568129343783L;
    
    private final Class<V> valueClass;
    
    private final SerializableSupplier<V> objectSupplier;
    
    public StringKeyJsonValueDeserializationSchema(final Class<V> valueClass,
            final SerializableSupplier<V> objectSupplier) {
        this.valueClass = valueClass;
        this.objectSupplier = objectSupplier;
    }
    
    @Override
    public boolean isEndOfStream(final Tuple2<String, V> nextElement) {
        return false;
    }
    
    @Override
    public Tuple2<String, V> deserialize(
            final ConsumerRecord<byte[], byte[]> record) throws Exception {
        final String key = new String(nullToEmpty(record.key()));
        final V value = fromJson(record.value(), valueClass,
                objectSupplier.get());
        return Tuple2.of(key, value);
    }
    
    @Override
    public TypeInformation<Tuple2<String, V>> getProducedType() {
        return new TupleTypeInfo<>(TypeExtractor.createTypeInfo(String.class),
                TypeExtractor.createTypeInfo(valueClass));
    }
}
