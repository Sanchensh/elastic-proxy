package com.mrxu.netty.filter;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.model.SessionContext;
import com.mrxu.netty.util.ByteBufManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Objects;

@Slf4j
public abstract class AbstractFilterContext {

    public AbstractFilterContext next;

    public void fireNext(SessionContext sessionContext) throws CustomException {
        if (Objects.isNull(next)) {
            ByteBufManager.close(sessionContext, new CustomException("filter error", "filter链路为空"));
            return;
        }
        fire0(next, sessionContext);
    }

    public void fireSelf(SessionContext sessionContext) {
        fire0(this, sessionContext);
    }

    public void fireFilter(SessionContext sessionContext, String filterName) throws CustomException {
        AbstractFilterContext filterContext = DefaultFilterPipeLine.INSTANCE.get(filterName);
        if (Objects.isNull(filterContext)) {
            ByteBufManager.close(sessionContext, new CustomException("filter error", "当前filter链路不存在"));
            return;
        }
        fire0(filterContext, sessionContext);
    }

    private void fire0(AbstractFilterContext filterContext, SessionContext sessionContext) {
        try {
        	filterContext.getFilter().run(filterContext, sessionContext);
        } catch (CustomException e) {
            log.error("系统内部错误，错误信息：{}", ExceptionUtils.getStackTrace(e));
            ByteBufManager.close(sessionContext, e);
        }
    }

    public abstract Filter getFilter();
}
