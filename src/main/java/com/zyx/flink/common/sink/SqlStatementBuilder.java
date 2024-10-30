package com.zyx.flink.common.sink;

import org.apache.flink.connector.jdbc.JdbcStatementBuilder;

/**
 * @version 1.0
 * @name: zhangyongxiang
 * @author: zyxuestc66@gmail.com
 * @date 2022/10/11 15:52
 * @description:
 **/

public interface SqlStatementBuilder<T> extends JdbcStatementBuilder<T> {
    
    String getSql();
}
