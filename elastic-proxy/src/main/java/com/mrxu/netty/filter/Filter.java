package com.mrxu.netty.filter;


import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.SessionContext;

public interface Filter {
    String name();
    void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException;
}
