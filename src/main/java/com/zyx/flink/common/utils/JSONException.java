package com.zyx.flink.common.utils;

/**
 * Created by zhangyongxiang on 2021/7/2 10:40 上午
 **/
public class JSONException extends RuntimeException {
    private static final long serialVersionUID = 2768476108237811802L;

    public JSONException() {}
    
    public JSONException(final String message) {
        super(message);
    }
    
    public JSONException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public JSONException(final Throwable cause) {
        super(cause);
    }
    
    public JSONException(final String message, final Throwable cause,
                         final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
