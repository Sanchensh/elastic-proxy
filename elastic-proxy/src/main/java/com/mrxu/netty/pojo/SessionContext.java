package com.mrxu.netty.pojo;

import com.mrxu.exception.CustomException;
import com.mrxu.model.CommonDTO;
import com.mrxu.model.ElasticSearchInfo;
import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.thread.TimerHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static com.mrxu.netty.util.HttpCode.HTTP_OK_CODE;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;

@Data
public class SessionContext {

    private long time;

    private TimeUnit timeUnit;

    private String key = DefaultChannelId.newInstance().asLongText();
    //判断这次请求是否完成，再获取esChannel时候会做判断
    private Boolean completed = false;

    private CustomException customException;

    private HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;

    private Channel clientChannel;

    private Channel esChannel;

    private FullHttpRequest fullHttpRequest;

    private CommonDTO commonDTO;

    private String appId;

    private String token;

    //请求es的uri
    private String restRequestUri;

    private String restRequestMethod;

    private String indexPattern;

    private ElasticSearchInfo elasticSearchInfo;

    //    消息是否发送过，避免重复发送
    private boolean bodySend;
    /**
     * 获取返回的response对象
     */
    private HttpHeaders responseHttpHeaders = new DefaultHttpHeaders(false);

    // 返回的httpVersion
    private HttpVersion responseHttpVersion = HTTP_1_1;

    public SessionContext() {
        this(10, TimeUnit.SECONDS);
    }

    public SessionContext(long time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public void setClientChannel(Channel clientChannel) {
        this.clientChannel = clientChannel;
        start();
    }

    public void cancel() {
        TimerHolder.cancel(key);
    }

    private void start() {
        TimerHolder.schedule(key, () -> {
            completed = true;
            ProxyRunner.errorProcess(this, new CustomException(408, "timeout", "this request is timeout"));
        }, time, timeUnit);
    }

    public DefaultFullHttpRequest getEsRequest(ElasticSearchInfo info) throws URISyntaxException, UnsupportedEncodingException {
        URI url = new URI(restRequestUri);
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HTTP_1_1, HttpMethod.valueOf(restRequestMethod),
                url.toASCIIString(),
                Unpooled.wrappedBuffer(commonDTO.getJson().getBytes(UTF_8)));
        // 构建http请求
        setHeaders(request.headers(), request.content(), info.getAuthorization());
        return request;
    }

    public void sendErrorMessage(CustomException exception){
        clientChannel.writeAndFlush(getFullHttpResponse(exception));
    }

    private FullHttpResponse getFullHttpResponse(CustomException exception) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(exception.getMessage().getBytes());
        FullHttpResponse response = new DefaultFullHttpResponse(responseHttpVersion, httpResponseStatus, byteBuf);
        setHeaders(response.headers(), byteBuf);
        return response;
    }

    private void setHeaders(HttpHeaders headers, ByteBuf byteBuf, String authorization) {
        setHeaders(headers,byteBuf);
        if (StringUtils.isNoneBlank(authorization)) {
            headers.set("Authorization", authorization);
        }
    }
    private void setHeaders(HttpHeaders headers, ByteBuf byteBuf) {
        headers.set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=utf-8");
        headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        headers.set(HttpHeaders.Names.CONTENT_LENGTH, byteBuf == null ? 0 : byteBuf.readableBytes());
    }

}


