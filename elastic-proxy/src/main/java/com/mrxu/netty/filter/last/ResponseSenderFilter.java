package com.mrxu.netty.filter.last;

import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.SessionContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import static com.mrxu.netty.util.ByteBufManager.close;


//直接打印错误信息，需要转化为es错误格式
@Slf4j
public class ResponseSenderFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + ResponseSenderFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(AbstractFilterContext filterContext, SessionContext sessionContext) {
        // 保证只会发送一次
        if (sessionContext.isBodySend()) {
            log.error(sessionContext.getRequest().getClientIp(), "body sent, close channel", sessionContext.getThrowable());
            sessionContext.getClientChannel().close();
            return;
        }
        sessionContext.setBodySend(true);
        try {
            HttpHeaders headers = sessionContext.getResponseHttpHeaders();
            ByteBuf body = sessionContext.getResponseBody();
            FullHttpResponse fullResponse = getFullHttpResponse(sessionContext.getHttpCode(), body, sessionContext.getResponseHttpVersion());
            fullResponse.headers().add(headers);
            // 发送信息
            sessionContext.getClientChannel().writeAndFlush(fullResponse);
        } catch (Exception e) {
            log.error(sessionContext.getRequest().getClientIp(), e.getMessage(), e);
        } finally {
            close(sessionContext);
        }
    }

    public FullHttpResponse getFullHttpResponse(int httpCode, ByteBuf body, HttpVersion httpVersion) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(httpCode);
        return new DefaultFullHttpResponse(httpVersion, status, body == null ? Unpooled.EMPTY_BUFFER : body, false);
    }

//    public static void printLog(SessionContext context) {
//        //有异常情况下不记录时间日志
//        if (Objects.isNull(context.getThrowable())) {
//            long took_1 = context.getTimeOfProxyToEs() - context.getTimeOfClientToProxy();
//            long took_2 = context.getTimeOfEsToProxy() - context.getTimeOfProxyToEs();
//            long took_3 = context.getTimeOfProxyToClient() - context.getTimeOfEsToProxy();
//            //当有处理时间大于一秒的，就记录日志
//            if (took_1 >= 100 || took_2 >= 1000 || took_3 >= 100) {
//                log.info("proxy处理client请求耗时：{}ms，es返回数据给proxy耗时：{}ms，proxy返回数据给client耗时：{}ms，请求的app id是：{}", took_1, took_2, took_3, context.getAppId());
//            }
//        }
//    }
}
