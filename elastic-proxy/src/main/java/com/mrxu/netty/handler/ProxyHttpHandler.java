package com.mrxu.netty.handler;

import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.filter.DefaultFilterPipeLine;
import com.mrxu.netty.filter.error.HandleErrorFilter;
import com.mrxu.netty.pojo.ProxyHttpRequest;
import com.mrxu.netty.pojo.SessionContext;
import com.mrxu.netty.util.HttpCode;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;


import java.io.IOException;
import java.util.Objects;

import static com.mrxu.netty.util.ByteBufManager.deepSafeRelease;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
@ChannelHandler.Sharable
public class ProxyHttpHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SessionContext context = new SessionContext();
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            if (is100ContinueExpected(fullHttpRequest)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            ProxyHttpRequest request = new ProxyHttpRequest(fullHttpRequest);
            context.setClientChannel(ctx.channel());
            context.setRequest(request);
            ProxyRunner.run(context);
        } catch (Throwable t) {
            log.error("client请求有误，错误信息：{}" , ExceptionUtils.getStackTrace(t));
            if (Objects.nonNull(context)) {
                context.setThrowable(t);
                context.setHttpCode(HttpCode.HTTP_INTERNAL_SERVER_ERROR);
                DefaultFilterPipeLine.getInstance().get(HandleErrorFilter.DEFAULT_NAME).fireSelf(context);
            }
        } finally {
            deepSafeRelease(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) { // client is shutdown, log.warn
            //由于现在移动的haproxy是短连接，导致Connection reset by peer 异常特别多，如果是该异常，暂时不做打印。在中文环境下是远程主机强迫关闭了一个现有的连接，暂时不作考虑
            String detailMessage = cause.getMessage();
            if (detailMessage != null) {
                if (!detailMessage.contains("reset")) {
                    log.error("Unexpected IOException from downstream.Throwable Message:{}", cause.getMessage());
                }
            }
        } else if (cause instanceof TooLongFrameException) {
            FullHttpRequest httpRequest = new DefaultFullHttpRequest(HTTP_1_1, GET, "/bad-request", Unpooled.EMPTY_BUFFER, false);
            httpRequest.setDecoderResult(DecoderResult.failure(cause));
            channelRead(ctx, httpRequest);
            return;
        } else {
            log.error("[ExceptionCaught][" + ctx.channel().remoteAddress() + " -> " + ctx.channel().localAddress() + "]", cause);
        }
        // 错误的上报
        ctx.close();
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        // 如果是超时,则对连接进行关闭
        if (!(evt instanceof IdleStateEvent)) {
            super.userEventTriggered(ctx, evt);
            return;
        }
        Channel channel = ctx.channel();
        channel.close();
    }
}
