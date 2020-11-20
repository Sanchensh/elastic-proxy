package com.mrxu.netty.client;

import com.mrxu.netty.pojo.SessionContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
@ChannelHandler.Sharable
public class ProxyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object response) throws Exception {
        try {
            SessionContext sessionContext = ChannelUtils.getSessionContext(ctx.channel());
            //0拷贝写回数据
            sessionContext.getClientChannel().writeAndFlush(response);
            sessionContext.getFuture().cancel(false);
        } catch (Throwable throwable) {
            log.error("写回数据出错，错误信息:{}", ExceptionUtils.getStackTrace(throwable));
        }
    }
}
