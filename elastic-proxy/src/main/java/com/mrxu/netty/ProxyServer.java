package com.mrxu.netty;

import com.mrxu.netty.prop.PropertiesUtil;
import com.mrxu.netty.server.ProxyNettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import static com.mrxu.netty.boot.Initialize.initFilter;


@Component
@Slf4j
public class ProxyServer implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ProxyNettyServer server = new ProxyNettyServer();
        log.info("........初始化filter........");
        initFilter();
        log.info("........netty 启动中........");
        server.startServer(PropertiesUtil.properties.getNettyPort());
    }
}
