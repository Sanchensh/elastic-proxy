package com.mrxu.netty.filter.exception;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.SessionContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;

public class ErrorResponseHeaderFilter extends AbstractFilter {
    public static String DEFAULT_NAME = PRE_FILTER_NAME + ErrorResponseHeaderFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
        HttpHeaders headers = new DefaultHttpHeaders(false);
        if (sessionContext.getFullHttpRequest().protocolVersion().isKeepAliveDefault()) {
            headers.remove(HttpHeaderNames.CONNECTION);
        } else {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }
        if (headers.get(HttpHeaderNames.CONTENT_TYPE) == null) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        }
        ByteBuf byteBuf = sessionContext.getErrorResponseBody();
        headers.set(HttpHeaderNames.CONTENT_LENGTH, byteBuf == null ? 0 : byteBuf.readableBytes());
        sessionContext.setResponseHttpHeaders(headers);
        filterContext.fireNext(sessionContext);
    }
}
