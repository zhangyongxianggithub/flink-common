package com.zyx.flink.common.config.parser;

import java.io.Serializable;
import java.util.Optional;

import com.zyx.flink.common.config.reader.SourceFormat;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.config.reader.SourceFormat.JSON;
import static com.zyx.flink.common.config.reader.SourceFormat.PROPERTIES;
import static com.zyx.flink.common.config.reader.SourceFormat.YAML;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

/**
 * Created by zhangyongxiang on 2022/11/30 下午9:03
 **/
public interface PropertySourceParser
        extends AnnotatedObjectSupport, Serializable {
    
    Optional<String> getProperty(String key);
    
    <T> Optional<T> getTypedProperty(Class<T> type, String key);
    
    @Slf4j
    class PropertySourceParserBuilder {
        
        private SourceFormat format;
        
        private String[] propertyContents;
        
        public PropertySourceParserBuilder format(
                final SourceFormat format) {
            this.format = format;
            return this;
        }
        
        public PropertySourceParserBuilder content(final String... contents) {
            this.propertyContents = nullToEmpty(contents);
            return this;
        }
        
        public PropertySourceParser build() {
            if (this.format == YAML) {
                return new YamlPropertySourceParser(
                        Lists.newArrayList(this.propertyContents));
            }
            if (this.format == PROPERTIES) {
                return new PropertiesPropertySourceParser(
                        Lists.newArrayList(this.propertyContents));
            }
            if (this.format == JSON) {
                return new JsonPropertySourceParser(
                        Lists.newArrayList(this.propertyContents));
            }
            log.error("unknown property source format {}", this.format);
            throw new IllegalStateException("unknown property source format");
        }
    }
}
