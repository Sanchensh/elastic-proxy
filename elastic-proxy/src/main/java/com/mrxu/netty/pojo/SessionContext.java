package com.mrxu.netty.pojo;

import com.mrxu.exception.CustomException;
import com.mrxu.model.CommonDTO;
import com.mrxu.model.ElasticSearchInfo;
import com.mrxu.netty.boot.ProxyRunner;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.mrxu.netty.thread.TimerThreadPool.submit;
import static com.mrxu.netty.util.HttpCode.HTTP_OK_CODE;
import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Data
public class SessionContext {

    public SessionContext() {
        this(30, TimeUnit.SECONDS);
    }

    private ScheduledFuture future;

    public SessionContext(long time, TimeUnit timeUnit) {
        this.future = submit(() -> {
            CustomException customException = new CustomException(408, "timeout", "this request is timeout");
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT, Unpooled.wrappedBuffer(customException.getMessage().getBytes()));
            clientChannel.writeAndFlush(response).addListener(CLOSE);
            ProxyRunner.errorProcess(this, customException);
        }, time >= 0 ? time : 10, timeUnit);
    }

    private Throwable throwable;

    private Boolean printExceStackInfo;

    private Integer clusterId;

    private String indexPattern;

    private int httpCode = HTTP_OK_CODE;

    private Channel clientChannel;

    private ProxyHttpRequest request;

    private CommonDTO commonDTO;

    private String appId;

    private String token;

    //请求es的uri
    private String restRequestUri;

    private String restRequestMethod;

    //    消息是否发送过，避免重复发送
    private boolean bodySend;
    /**
     * 获取返回的response对象
     */
    private HttpHeaders responseHttpHeaders = new DefaultHttpHeaders(false);

    private ByteBuf responseBody;

    // 返回的httpVersion
    private HttpVersion responseHttpVersion = HTTP_1_1;

    public DefaultFullHttpRequest getFullRequest(ElasticSearchInfo info) throws URISyntaxException, UnsupportedEncodingException {
        URI url = new URI(restRequestUri);
        String authorization = info.getAuthorization();
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.valueOf(restRequestMethod), url.toASCIIString(), Unpooled.wrappedBuffer(commonDTO.getJson().getBytes("UTF-8")));
        // 构建http请求
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
        if (StringUtils.isNoneBlank(authorization)) {
            request.headers().set("Authorization", authorization);
        }
        return request;
    }

}


