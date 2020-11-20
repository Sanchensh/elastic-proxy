package com.mrxu.netty.filter.last;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.SessionContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.*;

public class ResponseHeaderFilter extends AbstractFilter {
    public static String DEFAULT_NAME = PRE_FILTER_NAME + ResponseHeaderFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
        // 如果出错，也必要要往下走
        HttpHeaders headers = sessionContext.getResponseHttpHeaders();

        if (sessionContext.getRequest().getHttpVersion().isKeepAliveDefault()) {
            headers.remove(CONNECTION);
        } else {
            headers.set(CONNECTION, CLOSE);
        }
        // 对content-type的添加,如果是jsonp则返回script，如果不是，则返回json。
        if (headers.get(CONTENT_TYPE) == null) {// 如果用户设置了，以用户为准
            headers.set(CONTENT_TYPE, "application/json;charset=utf-8");
        }
        // 获取body
        ByteBuf byteBuf = sessionContext.getResponseBody();
        headers.set(CONTENT_LENGTH, byteBuf == null ? 0 : byteBuf.readableBytes());
        filterContext.fireNext(sessionContext);
    }
}
