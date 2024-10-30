package com.zyx.flink.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.utils.JsonUtils.SERIALIZE_FROM_JSON_FAILED;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.Objects.nonNull;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/29 00:51
 * @description:
 **/
@Slf4j
public class JsonSnakeCaseUtils {
    
    private JsonSnakeCaseUtils() {}
    
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    
    static {
        MAPPER.findAndRegisterModules();
        MAPPER.disable(FAIL_ON_EMPTY_BEANS);
        MAPPER.disable(WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.disable(FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);
    }
    
    public static String toJson(final Object object) {
        if (nonNull(object)) {
            try {
                return MAPPER.writeValueAsString(object);
            } catch (final JsonProcessingException e) {
                log.error(SERIALIZE_FROM_JSON_FAILED, object, e);
                throw new JSONException(e);
            }
        }
        return null;
    }
}
