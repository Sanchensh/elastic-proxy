package com.mrxu.netty.filter.rest;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.SessionContext;
import lombok.extern.slf4j.Slf4j;

import static com.mrxu.netty.filter.rest.ESRequest.performRequestByNetty;

@Slf4j
public final class ESRequestFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + ESRequestFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(final AbstractFilterContext filterContext, final SessionContext context) throws CustomException {
        //构造请求
        performRequestByNetty(context);
    }
}
