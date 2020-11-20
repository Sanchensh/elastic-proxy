package com.mrxu.netty.util;

public class HttpCode {
	public static final int HTTP_OK_CODE = 200;
	
	public static final int HTTP_NON_AUTHORITATIVE_INFORMATION = 203;

	public static final int HTTP_REDIRECT_CODE = 300;
	
	public static final int HTTP_REQUEST_URI_TOO_LONG = 414;
	//调用osp服务不存在时返回460
	public static final int PROXY = 460;
	//调用osp方法不存在
	public static final int CALLEE = 550;
	public static final int HTTP_FORBIDDEN = 403;
	public static final int HTTP_NOT_FOUND = 404;
	public static final int HTTP_UNSUPPORTED_MEDIA_TYPE=415;
	public static final int HTTP_METHOD_NOT_ALLOWED =405;
	public static final int HTTP_PRECONDITION_FAILED = 412;
	public static final int HTTP_BAD_REQUEST = 400;
	public static final int HTTP_UNAUTHORIZED = 401;
	
	public static final int HTTP_UNPROCESSABLE_ENTITY = 422;
	public static final int HTTP_TOO_MANY_REQUESTS = 429;
	
	public static final int HTTP_PARSER_FAILED = 499;
	
	public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
	public static final int HTTP_BAD_GATEWAY = 502;
	public static final int HTTP_SERVICE_UNAVAILABLE = 503;
	public static final int HTTP_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_GATEWAY_SHUTDOWN = 512;
	public static final int HTTP_RETRY_TIMEOUT = 520;
	
	public static final int HTTP_REQUEST_ENTITY_TOO_LARGE=413;

	public static final int HTTP_ANTI_BRUSH=911; //防刷拒绝状态码
	public static final int HTTP_WAF_FORBIDDEN = 912; //waf拒绝状态码
	public static final int HTTP_LIMITER = 914; // 限流拒绝状态码
	//字符串描述
	public static final String HTTP_BAD_GATEWAY_STR = "502";
}
