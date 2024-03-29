package com.mrxu.netty.server;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.SessionContextManager;
import com.mrxu.netty.filter.ProxyRunner;
import com.mrxu.netty.SessionContext;
import com.mrxu.netty.timer.TimerController;
import com.mrxu.netty.util.ByteBufManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static com.mrxu.common.Constants.TIMEOUT;
import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
@ChannelHandler.Sharable
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            String timeout = fullHttpRequest.headers().get(TIMEOUT);
            SessionContext sessionContext = StringUtils.isNotBlank(timeout)
                    ? SessionContextManager.SINGLETON.getSessionContext(Long.parseLong(timeout), ctx.channel())
                    : SessionContextManager.SINGLETON.getSessionContext(ctx.channel());
            TimerController.startTimer(sessionContext);//该请求超时设置
            if (is100ContinueExpected(fullHttpRequest)) { //HTTP 100 Continue 信息型状态响应码表示目前为止一切正常, 客户端应该继续请求, 如果已完成请求则忽略.
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            sessionContext.setFullHttpRequest(fullHttpRequest);
            ProxyRunner.run(sessionContext);
        } finally {
            ByteBufManager.deepSafeRelease(msg);//释放请求数据，避免堆外内存泄露
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("系统内部错误(proxy server error)，详细信息：{}", ExceptionUtils.getStackTrace(cause));
        CustomException customException = new CustomException(INTERNAL_SERVER_ERROR.code(), "internal server error(proxy server error)", cause.getMessage());
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1,
                INTERNAL_SERVER_ERROR,
                Unpooled.directBuffer().writeBytes(customException.getMessage().getBytes()));
        ctx.writeAndFlush(defaultFullHttpResponse).addListener(CLOSE);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        // 如果是超时,则对连接进行关闭
        if (!(evt instanceof IdleStateEvent)) {
            super.userEventTriggered(ctx, evt);
            return;
        }
        ctx.channel().closeFuture();
    }
}
