package com.mrxu.netty.filter;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.model.SessionContext;

public abstract class AbstractFilter implements Filter {
    public final static String PRE_FILTER_NAME = "PROXY_FILTER_";

    @Override
    public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
        filterContext.fireNext(sessionContext);
    }
}
