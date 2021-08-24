package com.mrxu.netty;

import com.mrxu.netty.client.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelId;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author jlxu
 * @date 2021/8/24/14:27
 */

//in order to avoid creating too much SessionContext ,avoid frequently GC
public enum SessionContextManager {
    SINGLETON;
    private static final ConcurrentLinkedDeque<SessionContext> sessionContextCache = new ConcurrentLinkedDeque<>();

    static {
        for (int i = 0; i < 1000; i++) {
            sessionContextCache.add(new SessionContext());
        }
    }

    public SessionContext getSessionContext() {
        SessionContext sessionContext = sessionContextCache.pollFirst();
        if (Objects.isNull(sessionContext)) {
            return new SessionContext();
        }
        return sessionContext;
    }

    public SessionContext getSessionContext(long timeout, Channel serverChannel) {
        SessionContext sessionContext = sessionContextCache.pollFirst();
        if (Objects.isNull(sessionContext)) {
            return new SessionContext(timeout, serverChannel);
        }
        sessionContext.setServerChannel(serverChannel);
        sessionContext.setTimeout(timeout <= 0 ? 2000 : timeout);
        return sessionContext;
    }

    public SessionContext getSessionContext(Channel serverChannel) {
        return getSessionContext(2000, serverChannel);
    }

    public void setSessionContext(SessionContext sessionContext) {
        reset(sessionContext);
        if (sessionContextCache.size() < 1000) sessionContextCache.offerLast(sessionContext);
    }

    private void reset(SessionContext sessionContext){
        ChannelUtil.clearSessionContext(sessionContext.getClientChannel());
        sessionContext.setKey(DefaultChannelId.newInstance().asLongText());
        sessionContext.setServerChannel(null);
        sessionContext.setClientChannel(null);
        sessionContext.setThrowable(null);
        sessionContext.setPrintStackInfo(false);
        sessionContext.setClusterId(0);
        sessionContext.setIndexPattern("");
        sessionContext.setHttpResponseStatus(OK);
        sessionContext.setResponseHttpHeaders(null);
        sessionContext.setFullHttpRequest(null);
        sessionContext.setSearchDTO(null);
        sessionContext.setRestRequestHost("");
        sessionContext.setRestRequestUri("");
        sessionContext.setRestRequestMethod(null);
        sessionContext.setBodySend(false);
        sessionContext.setErrorResponseBody(null);
        sessionContext.setResponseHttpVersion(HTTP_1_1);
    }
}
