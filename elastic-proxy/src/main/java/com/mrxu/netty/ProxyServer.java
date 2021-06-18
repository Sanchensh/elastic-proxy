package com.mrxu.netty;

import com.mrxu.netty.filter.DefaultFilterPipeLine;
import com.mrxu.netty.filter.error.HandleErrorFilter;
import com.mrxu.netty.filter.error.ResponseHeaderFilter;
import com.mrxu.netty.filter.error.ResponseSenderFilter;
import com.mrxu.netty.filter.pre.AuthorizationCheckFilter;
import com.mrxu.netty.filter.pre.HttpProtocolCheckFilter;
import com.mrxu.netty.filter.pre.SqlParseAndCheckFilter;
import com.mrxu.netty.filter.rest.ESRequestFilter;
import com.mrxu.netty.filter.rest.ESRequestUriFilter;
import com.mrxu.netty.prop.PropertiesUtil;
import com.mrxu.netty.server.ProxyNettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ProxyServer implements ApplicationRunner {
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
        DefaultFilterPipeLine.INSTANCE.addLastSegment(new ESRequestUriFilter(), new ESRequestFilter());

        // 后置流程，只有异常时才会走这个流程，正常数据直接由请求es这一阶段返回给前端
        // 错误处理
        DefaultFilterPipeLine.INSTANCE.addLastSegment(new ResponseHeaderFilter(), new ResponseSenderFilter(), new HandleErrorFilter());
    }
}
