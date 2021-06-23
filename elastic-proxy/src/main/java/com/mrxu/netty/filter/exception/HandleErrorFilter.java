package com.mrxu.netty.filter.exception;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.SessionContext;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 错误统一处理
 *
 */
@Slf4j
public class HandleErrorFilter extends AbstractFilter {
	public static String DEFAULT_NAME = PRE_FILTER_NAME + HandleErrorFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
		Throwable throwable = sessionContext.getThrowable();
		if (throwable != null) {
			// 是否打印堆栈信息
			sessionContext.setPrintStackInfo(printExceptionStackInfo(throwable));
			// 打印错误信息
			logThrowable(throwable);
			// 错误信息渲染，设置到body里面。返回状态码和mercury状态码处理
			processThrowable(sessionContext, throwable);
		}
		filterContext.fireFilter(sessionContext, ResponseHeaderFilter.DEFAULT_NAME);
	}

	public static void processThrowable(SessionContext sessionContext, Throwable t) {
		if (sessionContext.getHttpResponseStatus() == HttpResponseStatus.REQUEST_TIMEOUT) {
			return;
		}

		if (t instanceof CustomException) {
			sessionContext.setHttpResponseStatus(HttpResponseStatus.valueOf(((CustomException) t).getStatus()));
		} else {
			sessionContext.setHttpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}

		if (sessionContext.getErrorResponseBody() == null){
			sessionContext.setErrorResponseBody(Unpooled.directBuffer().writeBytes(t.getMessage() != null ? t.getMessage().getBytes() : "null".getBytes()));
		}

		if (sessionContext.getPrintStackInfo()){
			sessionContext.getThrowable().printStackTrace();
		}
	}

	//自定义的信息不用打印，因为已经返回给前端了
	private static void logThrowable(Throwable t) {
		if (!(t instanceof CustomException)) {
			log.error("系统内部错误，错误信息：{}", ExceptionUtils.getStackTrace(t));
		}
	}

	//判断是否需要打印堆栈信息
	private static Boolean printExceptionStackInfo(Throwable t) {
		return !(t instanceof CustomException);
	}

}
