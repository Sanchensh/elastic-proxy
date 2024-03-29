package com.mrxu.netty.filter.request;

import com.mrxu.exception.CustomException;
import com.mrxu.model.ClusterNodeInfo;
import com.mrxu.netty.client.ChannelUtil;
import com.mrxu.netty.client.DefaultChannelPool;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.SessionContext;
import com.mrxu.netty.property.PropertiesUtil;
import com.mrxu.netty.thread.ProxyThreadPool;
import com.mrxu.netty.util.BeanUtils;
import com.mrxu.netty.util.ByteBufManager;
import com.mrxu.sniff.ClusterManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Objects;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public final class ESRequestCallFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + ESRequestCallFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(final AbstractFilterContext filterContext, final SessionContext sessionContext) {
        performRequestByNetty(sessionContext);
    }

    private static ClusterManager clusterManager = BeanUtils.getBean("ClusterManager", ClusterManager.class);

    public static void performRequestByNetty(final SessionContext sessionContext) {
        ProxyThreadPool.ThreadPool.submit(() -> callEs(sessionContext, 0));
    }

    /**
     * 重试
     * @param sessionContext
     * @param retryCount     当前重试了多少次
     */
    private static void callEs(final SessionContext sessionContext, int retryCount) {
        ClusterNodeInfo clusterNodeInfo = clusterManager.getActiveNode();
        if (Objects.isNull(clusterNodeInfo)) {
            ByteBufManager.close(sessionContext, new CustomException("所有es节点不可用", "所有es节点不可用，请联系管理员或重试或等待下次嗅探"));
            return;
        }
        FullHttpRequest fullHttpRequest = getRequest(sessionContext, clusterNodeInfo);
        //将host设置到context中，可以直接使用，避免字符串的拼接与拆解
        sessionContext.setRestRequestHost(clusterNodeInfo.getHost());
        DefaultChannelPool.ChannelDTO channelDTO = DefaultChannelPool.INSTANCE.poll(clusterNodeInfo.getIp(), clusterNodeInfo.getPort(), clusterNodeInfo.getHost());
        if (Objects.nonNull(channelDTO.getBootstrap())) {//如果是新建立的连接
            Bootstrap bootstrap = channelDTO.getBootstrap();
            bootstrap.connect().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess())
                    sendRequest(future.channel(), sessionContext, clusterNodeInfo, fullHttpRequest, retryCount);
                else
                    handleException(sessionContext, clusterNodeInfo, future.cause(), retryCount);
            });
        } else {//如果连接池有连接，并且返回，则直接用已有的连接调用
            Channel channel = channelDTO.getChannel();
            sendRequest(channel, sessionContext, clusterNodeInfo, fullHttpRequest, retryCount);
        }
    }

    private static void sendRequest(Channel channel, SessionContext sessionContext, ClusterNodeInfo clusterNodeInfo, FullHttpRequest fullHttpRequest, int retryCount) {
        ChannelUtil.attributeSessionContext(channel, sessionContext);
        sessionContext.setClientChannel(channel);
        channel.writeAndFlush(fullHttpRequest).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) handleException(sessionContext, clusterNodeInfo, future.cause(), retryCount);
        });
    }

    private static void handleException(SessionContext sessionContext, ClusterNodeInfo clusterNodeInfo, Throwable throwable, int retryCount) {
        log.error("{}节点请求失败，正在重试。错误信息:{}", sessionContext.getRestRequestHost(), ExceptionUtils.getStackTrace(throwable));
        clusterManager.addDeadNode(clusterNodeInfo);//将请求失败的节点放到黑名单中，等待嗅探的时候在考虑是否重新启用该节点
        if (retryCount < PropertiesUtil.properties.getRetry()) //如果当前请求失败，需要重试
            callEs(sessionContext, retryCount + 1);
        else
            ByteBufManager.close(sessionContext, new CustomException("当前es节点不可用", "当前es节点不可用，请重试")); //如果retry次数达到还无法正确响应，则给客户端返回错误信息
    }


    private static FullHttpRequest getRequest(SessionContext sessionContext, ClusterNodeInfo clusterNodeInfo) {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1,
                sessionContext.getRestRequestMethod(),
                sessionContext.getRestRequestUri(),
                StringUtils.isNotBlank(sessionContext.getSearchDTO().getJson()) ? wrappedBuffer(sessionContext.
                        getSearchDTO().
                        getJson().
                        getBytes(UTF_8)) : EMPTY_BUFFER);
        // 构建http请求
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        if (StringUtils.isNoneBlank(clusterNodeInfo.getAuthorization())) {
            request.headers().set(HttpHeaderNames.AUTHORIZATION, clusterNodeInfo.getAuthorization());
        }
        return request;
    }
}
