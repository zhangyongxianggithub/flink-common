package com.zyx.flink.common.config.parser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/27 16:37
 * @description:
 **/
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FlinkProperties {
    String rootPath() default EMPTY;
}
