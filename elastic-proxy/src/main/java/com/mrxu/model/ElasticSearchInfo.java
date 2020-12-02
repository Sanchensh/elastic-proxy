package com.mrxu.model;

import com.mrxu.netty.client.ChannelUtils;
import com.mrxu.netty.client.ProxyChannelPool;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public enum ElasticSearchInfo {
    Instance;
    private ProxyChannelPool proxyChannelPool = new ProxyChannelPool("192.168.37.143", 9200);

    public Channel acquire() throws InterruptedException, ExecutionException {
        int i = 0;
        Channel channel = getChannel();
        while (Objects.isNull(channel) && i < 8) {
            channel = getChannel();
            i++;
        }
        return channel;
    }

    public void release(Channel channel) {
        ChannelUtils.clearAttribute(channel);
        FixedChannelPool fixedChannelPool = proxyChannelPool.getFixedChannelPool();
        fixedChannelPool.release(channel);
    }

    private Channel getChannel() throws InterruptedException, ExecutionException {
        FixedChannelPool fixedChannelPool = proxyChannelPool.getFixedChannelPool();
        Channel channel = fixedChannelPool.acquire().sync().get();
        if (!ChannelUtils.completed(channel)) {
            System.out.println("当前channel正在使用");
            return null;
        }
        return channel;
    }

    public ProxyChannelPool getProxyChannelPool() {
        return proxyChannelPool;
    }

    public String getAuthorization() {
        return "";
    }
}
