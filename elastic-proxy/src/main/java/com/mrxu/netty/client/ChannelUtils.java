package com.mrxu.netty.client;

import com.mrxu.netty.pojo.SessionContext;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChannelUtils {
    public static final AttributeKey<ConcurrentHashMap<String, SessionContext>> PROXY_NETTY_CLIENT_ATTRIBUTE = AttributeKey.valueOf("proxy_netty_client_attribute");

    public static SessionContext getSessionContext(Channel channel) {
        return channel.attr(PROXY_NETTY_CLIENT_ATTRIBUTE).get().get(channel.id().asLongText());
    }

    public static void attributeChannel(SessionContext context, Channel esChannel) {
        Attribute<ConcurrentHashMap<String, SessionContext>> attr = esChannel.attr(ChannelUtils.PROXY_NETTY_CLIENT_ATTRIBUTE);
        ConcurrentHashMap<String, SessionContext> attrMap = attr.get();
        attrMap.put(esChannel.id().asLongText(), context);
    }

    public static void release(FullHttpResponse response) {
        try {
            ReferenceCountUtil.release(response);
        } catch (Throwable throwable) {
            log.info("释放buffer出错，错误信息：{}", ExceptionUtils.getStackTrace(throwable));
        }
    }
}
