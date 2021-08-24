package com.mrxu.netty.client;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.SessionContext;
import com.mrxu.netty.SessionContextManager;
import com.mrxu.netty.timer.TimerController;
import com.mrxu.netty.util.ByteBufManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
@ChannelHandler.Sharable
public class HttpHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SessionContext sessionContext = ChannelUtil.getSessionContext(ctx.channel());
        //写回数据到客户端，并且需要关闭超时，同时需要清理SessionContext并将Channel放回到池中
        TimerController.stopTimer(sessionContext);
        sessionContext.getServerChannel().writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                DefaultChannelPool.INSTANCE.offer(ctx.channel(), sessionContext.getRestRequestHost());
                SessionContextManager.SINGLETON.setSessionContext(sessionContext);
            } else {
                log.error("写回数据给客户端出错，错误信息；{}", ExceptionUtils.getStackTrace(future.cause()));
                ctx.channel().closeFuture();
                ByteBufManager.close(sessionContext, new CustomException("返回数据出错", "es返回数据给客户端出错，请联系管理员"));
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("系统内部错误(call es error)，详细信息：{}", ExceptionUtils.getStackTrace(cause));
        SessionContext sessionContext = ChannelUtil.getSessionContext(ctx.channel());
        //先关闭超时，再写回数据
        TimerController.stopTimer(sessionContext);
        CustomException customException = new CustomException(INTERNAL_SERVER_ERROR.code(), "internal server error(call es error)", cause.getMessage());
        DefaultFullHttpResponse errorResponse = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.directBuffer().writeBytes(customException.getMessage().getBytes()));
        ctx.channel().closeFuture();
        //出现异常，关闭客户端Channel
        sessionContext.getServerChannel().writeAndFlush(errorResponse)
                .addListener(CLOSE)
                .addListener(future -> SessionContextManager.SINGLETON.setSessionContext(sessionContext));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        DefaultChannelPool.INSTANCE.removeChannel(ctx.channel());
    }

}
