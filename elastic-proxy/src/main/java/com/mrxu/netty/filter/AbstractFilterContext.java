package com.mrxu.netty.filter;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.pojo.SessionContext;
import com.mrxu.netty.trait.Filter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public abstract class AbstractFilterContext {


	public AbstractFilterContext next;

	public void fireNext(SessionContext sessionContext) throws CustomException {
		if (next == null) {
			sessionContext.getClientChannel().close();
			throw new CustomException("filter error","filter链路为空");
		}
		fire0(next, sessionContext);
	}

	public void fireSelf(SessionContext sessionContext) {
		fire0(this, sessionContext);
	}

	public void fireFilter(SessionContext sessionContext, String filterName) throws CustomException {
		AbstractFilterContext filterContext = DefaultFilterPipeLine.getInstance().get(filterName);
		if (filterContext == null) {
			throw new CustomException("filter error","当前filter链路不存在");
		}
		fire0(filterContext, sessionContext);

	}

	private void fire0(AbstractFilterContext filterContext, SessionContext sessionContext) {
		try {
			if (filterContext.getFilter().isValid()) {
				filterContext.getFilter().run(filterContext, sessionContext);
			} else {
				fire0(filterContext.next, sessionContext);
			}
		} catch (CustomException e) {
			e.printStackTrace();
			log.error("系统内部错误，错误信息：{}", ExceptionUtils.getStackTrace(e));
			ProxyRunner.errorProcess(sessionContext, e);
		}
	}
	
	public abstract Filter getFilter();
}
