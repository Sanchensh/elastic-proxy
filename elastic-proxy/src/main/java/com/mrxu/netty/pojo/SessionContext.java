package com.mrxu.netty.pojo;

import com.mrxu.exception.CustomException;
import com.mrxu.model.ElasticsearchNodeInfo;
import com.mrxu.model.SearchDTO;
import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.client.ChannelUtil;
import com.mrxu.netty.thread.TimerHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelId;
import io.netty.handler.codec.http.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Data
@Slf4j
public class SessionContext {

    //唯一的key
    private String key = DefaultChannelId.newInstance().asLongText();

    //该请求的Channel，该Channel会写回调用es获取的数据
    private Channel serverChannel;

    //请求es的客户端Channel，可以随时随地操作
    private Channel clientChannel;

    //超时时间
    private long timeout;

    //错误信息
    private Throwable throwable;

    //是否打印堆栈信息
    private Boolean printStackInfo;

    //集群id
    private Integer clusterId;

    //请求的indexPattern
    private String indexPattern;

    //返回给客户端的状态码
    private HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;

    //返回给客户端的头信息
    private HttpHeaders responseHttpHeaders = new DefaultHttpHeaders(false);

    //客户端请求数据
    private FullHttpRequest fullHttpRequest;

    //客户端请求体
    private SearchDTO searchDTO;

    //客户端请求对应的APPID
    private String appId;

    //客户端请求token
    private String token;

    //请求es的host，这里主要是防止字符串多次拼接与拆解
    private String restRequestHost;

    //请求es的uri
    private String restRequestUri;

    //请求es的Method
    private String restRequestMethod;

    //消息是否发送过错误消息，避免重复发送
    private boolean bodySend;

    //错误信息
    private ByteBuf errorResponseBody;

    // 返回的httpVersion
    private HttpVersion responseHttpVersion = HTTP_1_1;

    public SessionContext(Channel serverChannel) {
        this(2000, serverChannel);
    }

    public SessionContext(long timeout, Channel serverChannel) {
        this.timeout = timeout <= 0 ? 2000 : timeout;
        this.serverChannel = serverChannel;
        this.startTimer();//该请求超时设置
    }

    private void startTimer() {
        TimerHolder.schedule(key, () -> {
            if (clientChannel != null) {//如果clientChannel不为空,则丢掉这个Channel，因为超时可能是该Channel导致的，如果放回池中可能还会出现类似问题
                ChannelUtil.clearSessionContext(clientChannel);
                clientChannel.close();
            }
            ProxyRunner.errorProcess(this, new CustomException(HttpResponseStatus.REQUEST_TIMEOUT.code(), "Request Timeout", "This request is timeout,please retry"));
        }, timeout, TimeUnit.MILLISECONDS);
    }

    public void stopTimer() {
        TimerHolder.stop(key);
    }

    //获取请求es的请求信息
    public FullHttpRequest getRequest(ElasticsearchNodeInfo elasticsearchNodeInfo) {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1,
                HttpMethod.valueOf(restRequestMethod),
                restRequestUri,
                StringUtils.isNotBlank(searchDTO.getJson()) ? wrappedBuffer(searchDTO.getJson().getBytes(StandardCharsets.UTF_8)) : EMPTY_BUFFER);
        // 构建http请求
        request.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        if (StringUtils.isNoneBlank(elasticsearchNodeInfo.getAuthorization())) {
            request.headers().set("Authorization", elasticsearchNodeInfo.getAuthorization());
        }
        return request;
    }
}


