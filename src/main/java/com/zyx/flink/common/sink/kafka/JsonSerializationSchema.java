package com.zyx.flink.common.sink.kafka;

import org.apache.flink.api.common.serialization.SerializationSchema;

import static com.zyx.flink.common.utils.JsonUtils.toJson;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/28 22:09
 * @description:
 **/

public class JsonSerializationSchema<T> implements SerializationSchema<T> {
    
    private static final long serialVersionUID = -7349228061878357860L;
    
    @Override
    public byte[] serialize(final T element) {
        return toJson(element).getBytes(UTF_8);
    }
}
