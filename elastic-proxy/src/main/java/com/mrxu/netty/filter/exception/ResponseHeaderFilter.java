package com.mrxu.netty.filter.exception;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.SessionContext;
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
        HttpHeaders headers = sessionContext.getResponseHttpHeaders();
        if (sessionContext.getFullHttpRequest().protocolVersion().isKeepAliveDefault()) {
            headers.remove(CONNECTION);
        } else {
            headers.set(CONNECTION, CLOSE);
        }
        if (headers.get(CONTENT_TYPE) == null) {
            headers.set(CONTENT_TYPE, "application/json;charset=utf-8");
        }
        ByteBuf byteBuf = sessionContext.getErrorResponseBody();
        headers.set(CONTENT_LENGTH, byteBuf == null ? 0 : byteBuf.readableBytes());
        filterContext.fireNext(sessionContext);
    }
}
