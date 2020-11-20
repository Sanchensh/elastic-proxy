package com.mrxu.netty.client;

import com.mrxu.netty.pojo.SessionContext;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.Attribute;

import java.util.concurrent.ConcurrentHashMap;

public class ProxyChannelPoolHandler implements ChannelPoolHandler {
    @Override
    public void channelReleased(Channel ch) throws Exception {
//        ch.writeAndFlush(Unpooled.EMPTY_BUFFER); //flush掉所有写回的数据
    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {
//        TimeUnit.SECONDS.sleep(10);
    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        //创建的时候打标签
        ch.pipeline().addLast(new HttpClientCodec())
                .addLast(new HttpObjectAggregator(1024 * 1024 * 1024))
                .addLast(new ProxyClientHandler());
        Attribute<ConcurrentHashMap<String, SessionContext>> attr = ch.attr(ChannelUtils.PROXY_NETTY_CLIENT_ATTRIBUTE);
        ConcurrentHashMap<String, SessionContext> dataMap = new ConcurrentHashMap<>();
        attr.set(dataMap);
    }
}
