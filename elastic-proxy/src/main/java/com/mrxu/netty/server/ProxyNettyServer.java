package com.mrxu.netty.server;
import com.mrxu.netty.handler.ProxyServerHandler;
import com.mrxu.netty.prop.PropertiesUtil;
import com.mrxu.netty.thread.DefaultThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ProxyNettyServer {

    //boss线程
    private NioEventLoopGroup bossGroup = new NioEventLoopGroup(PropertiesUtil.properties.getBoss(), DefaultThreadFactory.create("proxy_boss_thread", true));
    //worker线程
    public static NioEventLoopGroup workerGroup = new NioEventLoopGroup(PropertiesUtil.properties.getWorker(), DefaultThreadFactory.create("proxy_worker_thread", true));

    public void startServer(int port) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .option(NioChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .childOption(NioChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(initChildHandler());
        System.out.println("proxy server start,port is " + port);
        log.info("proxy server start,port is {}", port);
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        channelFuture.channel().closeFuture().sync();
        log.info("proxy server stop");
    }

    @SuppressWarnings("rawtypes")
    private ChannelInitializer initChildHandler() {
        final ProxyServerHandler proxyServerHandler = new ProxyServerHandler();
        return new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(PropertiesUtil.properties.getServerKeepaliveTimeout(), 0, 0, TimeUnit.SECONDS));
                pipeline.addLast(new HttpResponseEncoder());
                pipeline.addLast(new HttpRequestDecoder(4096, 8192, 8192, Boolean.TRUE));
                pipeline.addLast(new HttpServerKeepAliveHandler());
                pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 64));
                pipeline.addLast(proxyServerHandler);
            }
        };
    }
}
