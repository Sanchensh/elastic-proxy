package com.mrxu.netty.filter.request;

import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.model.SessionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ESRequestFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + ESRequestFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(final AbstractFilterContext filterContext, final SessionContext sessionContext) {
        ESRequest.performRequestByNetty(sessionContext);
    }
}
