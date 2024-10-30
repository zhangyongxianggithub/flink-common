package com.zyx.flink.common.sql;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.zyx.flink.common.config.environment.FlinkTaskEnvironment;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.utils.JsonUtils.toJson;
import static java.util.stream.Collectors.toList;
import static jodd.util.StringPool.SEMICOLON;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * Created by zhangyongxiang on 2023/1/6 2:30 AM
 **/
@Slf4j
public class DefaultFlinkStatementFactory<T>
        implements FlinkStatementFactory<T> {
    
    private final String delimiter;
    
    private final FlinkSqlFactory<T> flinkSqlFactory;
    
    public DefaultFlinkStatementFactory() {
        this(new DefaultFlinkSqlFactory<>(), SEMICOLON);
    }
    
    public DefaultFlinkStatementFactory(
            final FlinkSqlFactory<T> flinkSqlFactory, final String delimiter) {
        this.flinkSqlFactory = flinkSqlFactory;
        this.delimiter = delimiter;
    }
    
    @Override
    public List<String> getStatements(
            final FlinkTaskEnvironment flinkTaskEnvironment,
            final ModelSupplier<T> modelSupplier) {
        final T model = modelSupplier.apply(flinkTaskEnvironment);
        log.info("flink sql template model tree: {}", toJson(model));
        final String sql = this.flinkSqlFactory.getSql(model);
        log.info("the whole sql script after interpolation: \n{}\n", sql);
        return Lists.newArrayList(split(sql, this.delimiter)).stream()
                .filter(StringUtils::isNotBlank).map(StringUtils::trim)
                .collect(toList());
    }
}
