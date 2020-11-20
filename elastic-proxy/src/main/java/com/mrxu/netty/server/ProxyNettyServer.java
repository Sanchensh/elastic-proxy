package com.mrxu.netty.server;

import com.mrxu.netty.handler.ProxyHttpHandler;
import com.mrxu.netty.prop.ProxySearchProperties;
import com.mrxu.netty.util.ByteBufManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ProxyNettyServer {

    //boss线程
    private NioEventLoopGroup bossGroup = new NioEventLoopGroup(ProxySearchProperties.BOSS_GROUP_SIZE,
            new DefaultThreadFactory("proxy_boss_thread", true));
    //worker线程
    public static NioEventLoopGroup workerGroup = new NioEventLoopGroup(ProxySearchProperties.WORKER_GROUP_SIZE,
            new DefaultThreadFactory("proxy_worker_thread", true));

    private ChannelFuture channelFuture;

    public ProxyNettyServer() {
    }

    public void startServer(int port) throws InterruptedException {
        ServerBootstrap insecure = new ServerBootstrap();
        insecure.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .option(ChannelOption.ALLOCATOR, ByteBufManager.byteBufAllocator)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, ByteBufManager.byteBufAllocator)
                .childHandler(initChildHandler());
        channelFuture = insecure.bind(port).sync();
        log.info("netty 服务启动完成，端口是：{}", port);
        channelFuture.channel().closeFuture().sync();

    }

    @SuppressWarnings("rawtypes")
    private ChannelInitializer initChildHandler() {
        final ProxyHttpHandler proxyHttpHandler = new ProxyHttpHandler();
        return new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(0, 0, ProxySearchProperties.HTTP_SERVER_KEEPALIVE_TIMEOUT, TimeUnit.MILLISECONDS));
                pipeline.addLast(new HttpResponseEncoder());
                pipeline.addLast(new HttpRequestDecoder(ProxySearchProperties.HTTP_SERVER_MAXINITIALLINELENGTH, ProxySearchProperties.HTTP_SERVER_MAXHEADERSIZE, 8192, ProxySearchProperties.HTTP_SERVER_VALIDATEHEADERS));
                pipeline.addLast(new HttpServerKeepAliveHandler());
                pipeline.addLast(new HttpObjectAggregator(ProxySearchProperties.HTTP_AGGREGATOR_MAXLENGTH));
                //日志打印，需要时开启
//                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                pipeline.addLast(proxyHttpHandler);
            }
        };
    }


}
