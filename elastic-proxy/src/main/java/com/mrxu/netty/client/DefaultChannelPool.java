package com.mrxu.netty.client;

import com.mrxu.netty.prop.PropertiesUtil;
import com.mrxu.netty.thread.DefaultThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
public enum DefaultChannelPool {
    //单例
    INSTANCE;
    //Channel池，key是host，value是该节点所有Channel集合
    private ConcurrentHashMap<String, ConcurrentLinkedDeque<Channel>> channelPool = new ConcurrentHashMap<>();
    //请求es的NioEventLoopGroup是共用的，如果每个节点都重新创建，可能服务器会出现 Too many open files 错误
    private NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(PropertiesUtil.properties.getClientThread(), DefaultThreadFactory.create("client_nio_event_loop_group", true));

    /**
     * @param channel 使用过的Channel
     * @param host    ip + ":" + port 也就是host
     */
    public void offer(Channel channel, String host) {
        ConcurrentLinkedDeque<Channel> channels = channelPool.get(host);
        if (Objects.isNull(channels)) {
            channels = new ConcurrentLinkedDeque<>();
            channelPool.put(host, channels);
        }
        if (channels.size() < PropertiesUtil.properties.getPoolSize()) { //成功将连接放回池中
            channels.offerFirst(channel);
        } else {//连接放回池中失败，则需要清除所有属性，避免jvm报错——GC overhead limit exceeded
            ChannelUtil.clearSessionContext(channel);
            channel.close();
        }
    }

    /**
     * 获取channel，
     *
     * @param ip   目标IP
     * @param port 目标端口
     * @return 如果是新连接，则返回ChannelFuture；如果是已有连接，则返回Channel
     */
    public ChannelDTO poll(String ip, int port, String host) {
        ConcurrentLinkedDeque<Channel> channels = channelPool.get(host);
        Channel channel = null;
        int i = 0;
        if (Objects.nonNull(channels)) {
            while (Objects.isNull(channel) && i++ < 10) {//避免轮询整个链表，最多尝试10次，其余的channel触发idle会自动剔除
                channel = channels.pollFirst();
                if (Objects.isNull(channel)) {
                    break;
                }
                if (!channel.isActive() || !channel.isOpen()) {// 是否是active状态，可能节点挂掉
                    channel = null;
                }
                if (Objects.nonNull(ChannelUtil.getSessionContext(channel))) {// 这里防止多线程情况下channel中的SessionContext被覆盖了
                    channel = null;
                }
            }
        }
        if (Objects.isNull(channel) || !channel.isActive() || !channel.isOpen()) {
            return new ChannelDTO(null, newChannel(ip, port));
        }
        return new ChannelDTO(channel, null);
    }

    /**
     * 删除池中指定的channel
     *
     * @param channel 指定的channel
     */
    public void removeChannel(Channel channel) {
        ConcurrentLinkedDeque<Channel> channels = channelPool.get(getHost(channel));
        if (Objects.nonNull(channels) && channels.size() != 0) {
            channels.removeIf(chan -> chan.id().asLongText().equals(channel.id().asLongText()));
        }
    }

    /**
     * 获取host => ip + ":" + port
     *
     * @param channel 目标channel
     * @return ip + ":" + port
     */
    private String getHost(Channel channel) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
        return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
    }

    /**
     * 配置channel
     *
     * @param ip   目标ip
     * @param port 端口
     * @return
     */
    private Bootstrap newChannel(String ip, int port) {
        Bootstrap bootstrap = newBootstrap(ip, port);
        bootstrap.handler(new Initializer());
//        ChannelFuture channelFuture = bootstrap.connect();//不使用bootstrap.connect().sync()是为了防止同步造成性能问题
//        return channelFuture;
        return bootstrap;//直接返回bootstrap是为了防止future已经isDone,再加上listener无效
    }

    /**
     * new一个bootstrap
     *
     * @param ip   目标ip
     * @param port 端口
     * @return boostrap配置
     */
    private Bootstrap newBootstrap(String ip, int port) {
        @SuppressWarnings("deprecation")
        Bootstrap bootstrap = new Bootstrap()
                .remoteAddress(new InetSocketAddress(ip, port))
                .group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.AUTO_CLOSE, false);
        return bootstrap;
    }

    private static class Initializer extends ChannelInitializer<Channel> {
        @Override
        protected void initChannel(Channel ch) {
            Integer idleTime = PropertiesUtil.properties.getIdleTime();
            ch.pipeline().addLast(new HttpClientCodec());
            ch.pipeline().addLast(new IdleStateHandler(idleTime, 0, 0, TimeUnit.SECONDS));
            ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 64));
            ch.pipeline().addLast(new HttpHandler());
        }
    }

    @Data
    @AllArgsConstructor
    public static class ChannelDTO {
        private Channel channel;
        private Bootstrap bootstrap;
    }
}
