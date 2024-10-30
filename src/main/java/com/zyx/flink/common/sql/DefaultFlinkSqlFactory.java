package com.zyx.flink.common.sql;

import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import static com.zyx.flink.common.config.ErrCode.SQL_TEMPLATE_ERROR;
import static freemarker.template.Configuration.VERSION_2_3_29;
import static java.util.Locale.CHINA;
import static jodd.util.StringPool.SLASH;

/**
 * Created by zhangyongxiang on 2023/1/6 5:09 PM
 **/
@Slf4j
public class DefaultFlinkSqlFactory<T> implements FlinkSqlFactory<T> {
    
    private static final String SQL_TEMPLATE_FILE = "flink.sql.ftl";
    
    private final String sqlTemplatePath;
    
    public DefaultFlinkSqlFactory(final String sqlTemplatePath) {
        this.sqlTemplatePath = sqlTemplatePath;
    }
    
    public DefaultFlinkSqlFactory() {
        this(SQL_TEMPLATE_FILE);
    }
    
    @Override
    public String getSql(final T model) {
        // destroy immediately after return don't need to make it single
        final Configuration cfg = new Configuration(VERSION_2_3_29);
        cfg.setClassForTemplateLoading(this.getClass(), SLASH);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(
                TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(true);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setLocale(CHINA);
        try {
            log.info("load flink sql template from {}", this.sqlTemplatePath);
            final Template sqlTemplate = cfg.getTemplate(this.sqlTemplatePath);
            final StringWriter writer = new StringWriter();
            sqlTemplate.process(model, writer);
            return writer.toString();
        } catch (final Exception e) {
            log.error(SQL_TEMPLATE_ERROR.getMessage(), e);
            System.exit(SQL_TEMPLATE_ERROR.getCode());
        }
        return null;
    }
}
