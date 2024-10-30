package com.zyx.flink.common.config.parser;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static jodd.util.StringPool.SLASH;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.prependIfMissing;

/**
 * Created by zhangyongxiang on 2022/11/30 下午9:04
 **/
@Slf4j
public class YamlPropertySourceParser implements PropertySourceParser {
    
    private static final long serialVersionUID = -3190073587247566831L;
    
    private final ImmutableList<String> propertyContents;
    
    private transient YAMLMapper yamlMapper = new YAMLMapper();
    
    public YamlPropertySourceParser(final List<String> propertyContents) {
        this.propertyContents = ImmutableList
                .copyOf(emptyIfNull(propertyContents));
    }
    
    private YAMLMapper getYamlMapper() {
        
        if (isNull(this.yamlMapper)) {
            synchronized (this) {
                if (isNull(this.yamlMapper)) {
                    this.yamlMapper = new YAMLMapper();
                }
            }
        }
        return this.yamlMapper;
    }
    
    @Override
    public <T> T createAnnotatedPropertiesObject(
            final Class<T> propertiesObjectClass) {
        for (final String content : this.propertyContents) {
            final T configObject = createPropertiesObject(content,
                    propertiesObjectClass);
            if (nonNull(configObject)) {
                return configObject;
            }
        }
        return null;
    }
    
    protected <T> T createPropertiesObject(final String content,
            final Class<T> propertiesObjectClass) {
        if (isNotBlank(content)) {
            final FlinkProperties flinkProperties = propertiesObjectClass
                    .getAnnotation(FlinkProperties.class);
            if (nonNull(flinkProperties)) {
                try {
                    return this.getYamlMapper().readerFor(propertiesObjectClass)
                            .at(Optional.ofNullable(flinkProperties.rootPath())
                                    .filter(StringUtils::isNotBlank)
                                    .orElse(SLASH))
                            .readValue(content);
                } catch (final MismatchedInputException e) {
                    log.warn(
                            "property class {} config not found, check your configuration or ignore this warn",
                            propertiesObjectClass);
                } catch (final JsonProcessingException e) {
                    log.error("fail to parse {} in config",
                            propertiesObjectClass, e);
                }
            } else {
                log.error("annotation FlinkProperties not found in class {}",
                        propertiesObjectClass);
            }
        } else {
            log.error("empty config, none of properties exists");
        }
        return null;
    }
    
    @Override
    public Optional<String> getProperty(final String key) {
        for (final String content : this.propertyContents) {
            final Optional<String> configObject = getProperty(content, key);
            if (configObject.isPresent()) {
                return configObject;
            }
        }
        return empty();
    }
    
    protected Optional<String> getProperty(final String content,
            final String key) {
        if (isNotBlank(content)) {
            if (isNotBlank(key)) {
                try {
                    return Optional.of(this.getYamlMapper()
                            .readerFor(String.class)
                            .at(prependIfMissing(key.replace('.', '/'), SLASH))
                            .readValue(content));
                } catch (final MismatchedInputException e) {
                    log.warn(
                            "property {} config not found, check your configuration or ignore this warn",
                            key);
                } catch (final JsonProcessingException e) {
                    log.error("fail to parse property {} in config", key, e);
                }
            } else {
                log.error("property {} not found in config", key);
            }
        } else {
            log.error("empty config, none of properties exists");
        }
        return empty();
    }
    
    @Override
    public <T> Optional<T> getTypedProperty(final Class<T> type,
            final String key) {
        for (final String content : this.propertyContents) {
            final Optional<T> configObject = getTypedProperty(content, type,
                    key);
            if (configObject.isPresent()) {
                return configObject;
            }
        }
        return empty();
    }
    
    protected <T> Optional<T> getTypedProperty(final String content,
            final Class<T> type, final String key) {
        if (isNotBlank(content)) {
            if (isNotBlank(key)) {
                try {
                    return Optional.of(this.getYamlMapper().readerFor(type)
                            .at(prependIfMissing(key.replace('.', '/'), SLASH))
                            .readValue(content));
                } catch (final MismatchedInputException e) {
                    log.warn(
                            "property {} config not found, check your configuration or ignore this warn",
                            key);
                } catch (final JsonProcessingException e) {
                    log.error("fail to parse property {} in config", key, e);
                }
            } else {
                log.error("property {} not found in config", key);
            }
        } else {
            log.error("empty config, none of properties exists");
        }
        return empty();
    }
}
