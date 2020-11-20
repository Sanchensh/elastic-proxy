package com.mrxu.model;

import com.mrxu.netty.client.ProxyChannelPool;

public enum ElasticSearchInfo {
    Instance;
    private static final ProxyChannelPool proxyChannelPool = new ProxyChannelPool("192.168.37.143", 9200);
    public ProxyChannelPool getProxyChannelPool() {
        return proxyChannelPool;
    }
    public String getAuthorization(){
        return "";
    }
}
