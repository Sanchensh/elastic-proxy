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
                DefaultFullHttpRequest fullRequest = context.getEsRequest(elasticSearchInfo);
                //获取channel
                Channel channel = elasticSearchInfo.acquire();
                context.setElasticSearchInfo(elasticSearchInfo);
                context.setEsChannel(channel);
                ChannelUtils.attributeChannel(channel,context);
                channel.writeAndFlush(fullRequest);
            } catch (Exception e) {
                log.error("proxy调用es出错，错误信息：{}", ExceptionUtils.getStackTrace(e));
                ProxyRunner.errorProcess(context, new CustomException("proxy调用es出错", e.getMessage()));
            }
        });
    }

}
