package com.mrxu.netty.filter.pre;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.SessionContext;
import com.mrxu.netty.util.ByteBufManager;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import java.util.Objects;

@Slf4j
/**
 * 这里只对AppID和token是否匹配做校验，请求es时需校验AppID是否匹配indexPattern，
 * 在获取index是判断
 */
public class AuthorizationCheckFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + AuthorizationCheckFilter.class.getSimpleName().toUpperCase();

    @Override
    public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
        FullHttpRequest fullHttpRequest = sessionContext.getFullHttpRequest();
        String authorization = fullHttpRequest.headers().get("authorization");
        if (StringUtils.isBlank(authorization)) {
            closed(sessionContext);
            return;
        }
        //todo  权限校验
        filterContext.fireNext(sessionContext);
    }

    void closed(SessionContext sessionContext) {
        ByteBufManager.close(sessionContext, new CustomException("authorization error", "valid authorization failure,please check your authorization"));// 关闭掉channel
    }

    @Override
    public String name() {
        return DEFAULT_NAME;
    }
}
