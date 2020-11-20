package com.mrxu.netty;

import com.mrxu.netty.server.ProxyNettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import static com.mrxu.netty.boot.Initialize.initZuul;

@Component
@Slf4j
public class ProxyServer implements ApplicationRunner {
    @Value("${netty.port}")
    private int port;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ProxyNettyServer server = new ProxyNettyServer();
        log.info("........初始化filter........");
        initZuul();
        log.info("........netty 启动中........");
        server.startServer(port);
    }
}
