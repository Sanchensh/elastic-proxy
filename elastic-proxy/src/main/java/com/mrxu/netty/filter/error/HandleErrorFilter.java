package com.mrxu.netty.filter.error;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.filter.last.ResponseHeaderFilter;
import com.mrxu.netty.pojo.SessionContext;
import com.mrxu.netty.util.HttpCode;
import io.netty.buffer.Unpooled;
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
			sessionContext.setPrintExceStackInfo(printExceptionStackInfo(throwable));
			// 打印错误信息
			logThrowable(throwable);
			// 错误信息渲染，设置到body里面。返回状态码和mercury状态码处理
			processThrowable(sessionContext, throwable);
		}
		filterContext.fireFilter(sessionContext, ResponseHeaderFilter.DEFAULT_NAME);
	}

	public static void processThrowable(SessionContext sessionContext, Throwable t) {

		if (sessionContext.getHttpCode() == HttpCode.HTTP_RETRY_TIMEOUT) {
			return;
		}

		if (t instanceof CustomException) {
			sessionContext.setHttpCode(500);
		} else {
			sessionContext.setHttpCode(HttpCode.HTTP_INTERNAL_SERVER_ERROR);
		}

		if (sessionContext.getResponseBody() == null){
			sessionContext.setResponseBody(Unpooled.directBuffer().writeBytes(t.getMessage() != null ? t.getMessage().getBytes() : "null".getBytes()));
		}

		if (sessionContext.getPrintExceStackInfo()){
			sessionContext.getThrowable().printStackTrace();
		}
	}

	private static void logThrowable(Throwable t) {
		log.error("系统内部错误，错误信息：{}", ExceptionUtils.getStackTrace(t));
	}

	/**
	 * 判断是否需要打印堆栈信息
	 * 
	 * @param t
	 * @return
	 */
	private static Boolean printExceptionStackInfo(Throwable t) {
		return !(t instanceof CustomException);
	}

}
