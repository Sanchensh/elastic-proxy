package com.mrxu.netty;

import com.mrxu.exception.CustomException;
import com.mrxu.model.ClusterNodeInfo;
import com.mrxu.model.SearchDTO;
import com.mrxu.netty.filter.ProxyRunner;
import com.mrxu.netty.client.ChannelUtil;
import com.mrxu.netty.timer.TimerController;
import com.mrxu.netty.timer.TimerHolder;
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
    }
}


