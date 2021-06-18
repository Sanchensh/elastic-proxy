package com.mrxu.netty.filter;

import com.mrxu.netty.filter.DefaultFilterPipeLine;
import com.mrxu.netty.filter.error.HandleErrorFilter;
import com.mrxu.netty.filter.error.ResponseSenderFilter;
import com.mrxu.netty.filter.pre.HttpProtocolCheckFilter;
import com.mrxu.netty.pojo.SessionContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class ProxyRunner {
	public static void run(SessionContext context) {
		try {
			DefaultFilterPipeLine.INSTANCE.get(HttpProtocolCheckFilter.DEFAULT_NAME).fireSelf(context); // 开始执行
		} catch (Throwable e) {
			errorProcess(context, e);
		}
	}

	// 错误处理的统一入口。由于异步的原因，在调用filter出错，会catch住，然后调用该接口
	public static void errorProcess(SessionContext sessionContext, Throwable t) {
		try {
			Throwable throwable = sessionContext.getThrowable();
			// 说明在处理错误当中，又发生了错误(该方法被调用了两次)，则直接到发送数据的地方
			if (throwable != null) {
				if (t != null) {
					log.error("系统内部错误，错误信息：{}", ExceptionUtils.getStackTrace(throwable));
				}
				DefaultFilterPipeLine.INSTANCE.get(ResponseSenderFilter.DEFAULT_NAME).fireSelf(sessionContext);
			} else { // 有错误，则将指向errorFilter进行处理
				sessionContext.setThrowable(t);
				DefaultFilterPipeLine.INSTANCE.get(HandleErrorFilter.DEFAULT_NAME).fireSelf(sessionContext);
			}
		} catch (Exception e) {
			log.error("系统内部错误，错误信息：{}", ExceptionUtils.getStackTrace(e));
			sessionContext.setHttpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			// 直接打印错误
			DefaultFilterPipeLine.INSTANCE.get(ResponseSenderFilter.DEFAULT_NAME).fireSelf(sessionContext);
		}
	}
}
