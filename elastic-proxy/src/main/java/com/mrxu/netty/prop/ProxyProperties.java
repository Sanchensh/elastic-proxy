package com.mrxu.netty.prop;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component("ProxyProperties")
public class ProxyProperties {
    @Value("${proxy.client.retry:3}")
    private Integer retry;

    @Value("${proxy.client.idle_time:30}")
    private Integer idleTime;

    @Value("${proxy.client.client_thread:0}")
    private Integer clientThread;

    @Value("${proxy.client.pool_size:100}")
    private Integer poolSize;

    @Value("${proxy.server.server_keepalive_timeout:30}")
    private Integer serverKeepaliveTimeout;

    @Value("${proxy.server.sniffer_time:10}")
    private Integer snifferTime;

    @Value("${proxy.server.boss:0}")
    private Integer boss;

    @Value("${proxy.server.worker:0}")
    private Integer worker;

    @Value("${proxy.server.core_thread:0}")
    private Integer coreThread;

    @Value("${proxy.server.max_thread:0}")
    private Integer maxThread;

    @Value("${proxy.server.netty_port:8081}")
    private Integer nettyPort;

    @Value("${proxy.server.refresh_cache:3600000}")
    private Integer refreshCache;

    @Value("${proxy.elasticsearch.ip:'localhost'}")
    private String ip;

    @Value("${proxy.elasticsearch.port:9200}")
    private Integer port;

    @Value("${proxy.elasticsearch.username}")
    private String username;

    @Value("${proxy.elasticsearch.password}")
    private String password;

    public Integer getClientThread() {
        if (clientThread <= 0 ){
            return Runtime.getRuntime().availableProcessors() * 4 + 1;
        }
        return clientThread;
    }

    public Integer getBoss() {
        if (boss <= 0 ){
            return Runtime.getRuntime().availableProcessors() * 2 + 1;
        }
        return boss;
    }

    public Integer getWorker() {
        if (worker <= 0 ){
            return Runtime.getRuntime().availableProcessors() * 4 + 1;
        }
        return worker;
    }

    public Integer getCoreThread() {
        if (coreThread <= 0 ){
            return Runtime.getRuntime().availableProcessors() * 2 + 1;
        }
        return coreThread;
    }

    public Integer getMaxThread() {
        if (maxThread <= 0 ){
            return Runtime.getRuntime().availableProcessors() * 4 + 1;
        }
        return maxThread;
    }
}
