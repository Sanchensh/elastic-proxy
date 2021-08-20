package com.mrxu.netty;

import com.mrxu.netty.filter.DefaultFilterPipeLine;
import com.mrxu.netty.filter.exception.HandleErrorFilter;
import com.mrxu.netty.filter.exception.ResponseHeaderFilter;
import com.mrxu.netty.filter.exception.ResponseSenderFilter;
import com.mrxu.netty.filter.prepare.AuthorizationCheckFilter;
import com.mrxu.netty.filter.prepare.HttpProtocolCheckFilter;
import com.mrxu.netty.filter.prepare.SqlParseAndCheckFilter;
import com.mrxu.netty.filter.request.ESRequestCallFilter;
import com.mrxu.netty.filter.request.ESRequestUriFilter;
import com.mrxu.netty.property.PropertiesUtil;
import com.mrxu.netty.server.ProxyNettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ProxyServerStarter implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ProxyNettyServer server = new ProxyNettyServer();
        log.info("........初始化filter........");
        initJavaFilters();
        log.info("........netty 启动中........");
        server.startServer(PropertiesUtil.properties.getNettyPort());
    }

    private static void initJavaFilters() {
        // 前置流程
        DefaultFilterPipeLine.INSTANCE.addLastSegment(
                new HttpProtocolCheckFilter(),
                new AuthorizationCheckFilter(),
                new SqlParseAndCheckFilter());

        // 请求es流程
        DefaultFilterPipeLine.INSTANCE.addLastSegment(new ESRequestUriFilter(), new ESRequestCallFilter());

        // 后置流程，只有异常时才会走这个流程，正常数据直接由请求es这一阶段返回给前端
        // 错误处理
        DefaultFilterPipeLine.INSTANCE.addLastSegment(new ResponseHeaderFilter(), new ResponseSenderFilter(), new HandleErrorFilter());
    }
}
