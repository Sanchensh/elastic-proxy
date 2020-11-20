package com.mrxu.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;

@Data
public class ProxyChannelPool {
    private String ip;
    private int port;
    private FixedChannelPool fixedChannelPool;

    public ProxyChannelPool(String ip, int port, int poolSize) {
        this.ip = ip;
        this.port = port;
        this.fixedChannelPool = new FixedChannelPool(getBootstrap(), new ProxyChannelPoolHandler(), poolSize > 0 ? poolSize : 10);
    }

    public ProxyChannelPool(String ip, int port) {
        this(ip, port, 10);
    }

    private Bootstrap getBootstrap() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .remoteAddress(ip, port)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        return bootstrap;
    }
}
