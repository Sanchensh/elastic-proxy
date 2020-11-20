package com.mrxu.netty.filter.rest;

import com.mrxu.exception.CustomException;
import com.mrxu.model.ElasticSearchInfo;
import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.client.ChannelUtils;
import com.mrxu.netty.pojo.SessionContext;
import com.mrxu.netty.thread.ProxyThreadPool;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class ESRequest {
    /**
     * 下面是netty_client调用es
     */
    public static void performRequestByNetty(final SessionContext context) {
        ProxyThreadPool.submit(() -> {
            try {
                ElasticSearchInfo elasticSearchInfo =  ElasticSearchInfo.Instance;
                //获取channel
                DefaultFullHttpRequest fullRequest = context.getFullRequest(elasticSearchInfo);
                FixedChannelPool fixedChannelPool = elasticSearchInfo.getProxyChannelPool().getFixedChannelPool();
                fixedChannelPool.acquire().addListener((FutureListener<Channel>) future -> {
                    if (future.isSuccess()){
                        Channel channel = future.getNow();
                        ChannelUtils.attributeChannel(context,channel);
                        channel.writeAndFlush(fullRequest).addListener(future1 -> fixedChannelPool.release(channel));
                    } else {
                        log.error("从连接池中获取channel失败");
                    }
                });
            } catch (Exception e) {
                log.error("proxy调用es出错，错误信息：{}", ExceptionUtils.getStackTrace(e));
                ProxyRunner.errorProcess(context, new CustomException("proxy调用es出错", e.getMessage()));
            }
        });
    }

}
