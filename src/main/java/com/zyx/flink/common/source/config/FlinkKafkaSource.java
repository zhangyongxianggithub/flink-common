package com.zyx.flink.common.source.config;

import java.util.List;
import java.util.Properties;

import com.zyx.flink.common.config.parser.FlinkProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static jodd.util.StringPool.EQUALS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/26 15:23
 * @description:
 **/
@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FlinkProperties(rootPath = "/flink/kafka/source")
public class FlinkKafkaSource {
    
    private String topic;
    
    private String bootstrapServers;
    
    private List<String> configuration = Lists.newLinkedList();
    
    public Properties getKafkaProperties() {
        
        final Properties properties = new Properties();
        configuration.forEach(configItem -> properties.put(
                trim(substringBefore(configItem, EQUALS)),
                trim(substringAfter(configItem, EQUALS))));
        properties.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return properties;
    }
    
    public boolean isValid() {
        return isNotBlank(bootstrapServers) && isNotBlank(topic);
    }
    
}
