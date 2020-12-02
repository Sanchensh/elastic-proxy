package com.mrxu.netty.boot;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.DefaultFilterPipeLine;
import com.mrxu.netty.filter.error.HandleErrorFilter;
import com.mrxu.netty.filter.pre.HttpProtocolCheckFilter;
import com.mrxu.netty.pojo.SessionContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class ProxyRunner {
	public static void run(SessionContext context) {
		DefaultFilterPipeLine.getInstance().get(HttpProtocolCheckFilter.DEFAULT_NAME).fireSelf(context); // 开始执行
	}

	// 错误处理的统一入口。由于异步的原因，在调用filter出错，会catch住，然后调用该接口
	public static void errorProcess(SessionContext sessionContext, CustomException customException) {
		try {
			CustomException exception = sessionContext.getCustomException();
			// 说明在处理错误当中，又发生了错误(该方法被调用了两次)，则直接到发送数据的地方
			if (exception != null) {
				if (customException != null) {
					log.error("系统内部错误，错误信息：{}", ExceptionUtils.getStackTrace(customException));
				}
			} else { // 有错误，则将指向errorFilter进行处理
				sessionContext.setCustomException(customException);
			}
			DefaultFilterPipeLine.getInstance().get(HandleErrorFilter.DEFAULT_NAME).fireSelf(sessionContext);
		} catch (Exception e) {
			log.error("系统内部错误，错误信息：{}", ExceptionUtils.getStackTrace(e));
			sessionContext.setHttpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			// 直接打印错误
			DefaultFilterPipeLine.getInstance().get(HandleErrorFilter.DEFAULT_NAME).fireSelf(sessionContext);
		}
	}
}
