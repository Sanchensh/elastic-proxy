package com.mrxu.netty.filter.error;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.SessionContext;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;

@Slf4j
public class HandleErrorFilter extends AbstractFilter {
    public static String DEFAULT_NAME = PRE_FILTER_NAME + HandleErrorFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
        if (Objects.nonNull(sessionContext.getCustomException()))
            sessionContext.sendErrorMessage(sessionContext.getCustomException());
        else
            sessionContext.sendErrorMessage(new CustomException("未知错误", "系统出现未知错误!"));
    }

}
