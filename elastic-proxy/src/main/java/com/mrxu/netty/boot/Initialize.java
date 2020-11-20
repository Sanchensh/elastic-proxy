package com.mrxu.netty.boot;

import com.mrxu.netty.filter.DefaultFilterPipeLine;
import com.mrxu.netty.filter.error.HandleErrorFilter;
import com.mrxu.netty.filter.last.ResponseHeaderFilter;
import com.mrxu.netty.filter.last.ResponseSenderFilter;
import com.mrxu.netty.filter.pre.HttpProtocolCheckFilter;
import com.mrxu.netty.filter.pre.SqlParseAndCheckFilter;
import com.mrxu.netty.filter.rest.ESRequestFilter;
import com.mrxu.netty.filter.rest.ESRequestUriFilter;

public class Initialize {

    public static void initZuul() throws Exception {
        initJavaFilters();
    }

    private static void initJavaFilters() {
        // 前置流程
        DefaultFilterPipeLine.getInstance()
                .addLastSegment(
                        new HttpProtocolCheckFilter(),
                        new SqlParseAndCheckFilter());

        // 请求es流程
        DefaultFilterPipeLine.getInstance()
                .addLastSegment(
                        new ESRequestUriFilter(),
                        new ESRequestFilter());

        // 后置流程，只有异常时才会走这个流程，正常数据直接由请求es这一阶段返回给前端
        DefaultFilterPipeLine.getInstance()
                .addLastSegment(
                        new ResponseHeaderFilter(),
                        new ResponseSenderFilter());

        // 错误处理
        DefaultFilterPipeLine.getInstance().addLastSegment(new HandleErrorFilter());
    }

}
