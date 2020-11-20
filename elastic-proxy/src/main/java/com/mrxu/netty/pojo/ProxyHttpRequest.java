package com.mrxu.netty.pojo;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ProxyHttpRequest  {
    private FullHttpRequest httpRequest;
    public ProxyHttpRequest(FullHttpRequest request){
        this.httpRequest = request;
    }
    private String clientIp;

    public String getHeader(String name) {
        return httpRequest.headers().get(name);
    }
    public DecoderResult getDecoderResult() {
        return httpRequest.getDecoderResult();
    }
    public HttpVersion getHttpVersion() {
        return httpRequest.getProtocolVersion();
    }
}
