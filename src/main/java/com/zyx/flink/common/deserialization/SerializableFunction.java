package com.zyx.flink.common.deserialization;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/9/29 03:03
 * @description:
 **/

public interface SerializableFunction<S, T>
        extends Function<S, T>, Serializable {}
