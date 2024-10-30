package com.zyx.flink.common.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.extern.slf4j.Slf4j;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

/**
 * Created by zhangyongxiang on 2021/6/10 7:55 下午
 **/
@Slf4j
public final class JsonUtils {
    
    public static final String DESERIALIZE_FROM_JSON_FAILED = "deserialize from json {} failed";
    
    public static final String SERIALIZE_FROM_JSON_FAILED = "serialize from json {} failed";
    
    private JsonUtils() {}
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
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
    
    public static <T> T fromJson(final String json,
            final TypeReference<T> type) {
        if (isNoneBlank(json) && nonNull(type)) {
            try {
                return MAPPER.readValue(json, type);
            } catch (final IOException e) {
                log.error(DESERIALIZE_FROM_JSON_FAILED, json, e);
                throw new JSONException(e);
            }
        }
        return null;
    }
    
    public static <T> T fromJson(final byte[] jsonByte, final Class<T> type,
            final T defaultValue) {
        if (ArrayUtils.isNotEmpty(jsonByte) && nonNull(type)) {
            try {
                return MAPPER.readValue(jsonByte, type);
            } catch (final IOException e) {
                log.error(DESERIALIZE_FROM_JSON_FAILED, jsonByte, e);
                throw new JSONException(e);
            }
        }
        return defaultValue;
    }
    
    public static <T> T fromJson(final String json, final Class<T> type) {
        if (isNoneBlank(json) && nonNull(type)) {
            try {
                return MAPPER.readValue(json, type);
            } catch (final IOException e) {
                log.error(DESERIALIZE_FROM_JSON_FAILED, json, e);
                throw new JSONException(e);
            }
        }
        return null;
    }
    
    public static <T> List<T> fromListJson(final String json,
            final Class<T> elementType) {
        if (isNoneBlank(json) && nonNull(elementType)) {
            try {
                return MAPPER.readValue(json, TypeFactory.defaultInstance()
                        .constructCollectionType(ArrayList.class, elementType));
            } catch (final IOException e) {
                log.error(DESERIALIZE_FROM_JSON_FAILED, json, e);
                throw new JSONException(e);
            }
        }
        return emptyList();
    }
}
