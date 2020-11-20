package com.mrxu.netty.prop;

public class ProxySearchProperties extends ProxyBasicProperties {

	// server端 boss的线程个数默认为min(core/2,2)
	private static final String NAME_BOSS_SIZE = "proxy.boss.size";
	public static final int BOSS_GROUP_SIZE = processor.getInteger(NAME_BOSS_SIZE, Math.min(Runtime.getRuntime().availableProcessors() / 2, 2));

	// worker的个数默认为core的个数
	private static final String NAME_WORKER_SIZE = "proxy.worker.size";
	public static int WORKER_GROUP_SIZE = processor.getInteger(NAME_WORKER_SIZE, Runtime.getRuntime().availableProcessors());

	// TCP包组成HTTP包的最大长度，默认为64M
	private static final String NAME_HTTP_AGGREGATOR_MAXLENGTH = "proxy.http.aggregator.maxlength";
	public static int HTTP_AGGREGATOR_MAXLENGTH = processor.getInteger(NAME_HTTP_AGGREGATOR_MAXLENGTH, 1024 * 1024 * 1024);

	// http server的参数:maxInitialLineLength 长度，超过的话抛出 TooLongFrameException
	private static final String NAME_HTTP_SERVER_MAXINITIALLINELENGTH = "proxy.http.server_maxInitialLineLength";
	public static final Integer HTTP_SERVER_MAXINITIALLINELENGTH = processor.getInteger(NAME_HTTP_SERVER_MAXINITIALLINELENGTH, 4096);

	// http server的参数:header 长度，超过的话抛出 TooLongFrameException (处理方式和maxInitialLineLength一致)
	private static final String NAME_HTTP_SERVER_MAXHEADERSIZE = "proxy.http.server_maxHeaderSize";
	public static final Integer HTTP_SERVER_MAXHEADERSIZE = processor.getInteger(NAME_HTTP_SERVER_MAXHEADERSIZE, 8192);

	// http server参数：对header的name和value进行校验。 一般校验特殊字符或者非空 。 是针对set？？还需要调研
	private static final String NAME_HTTP_SERVER_VALIDATEHEADERS = "proxy.http.server_validateHeaders";
	public static final Boolean HTTP_SERVER_VALIDATEHEADERS = processor.getBoolean(NAME_HTTP_SERVER_VALIDATEHEADERS, true);

	// http server：
	private static final String NAME_HTTP_SERVER_KEEPALIVE_TIMEOUT = "proxy.http.server_keepalive_timeout";
	public static final Integer HTTP_SERVER_KEEPALIVE_TIMEOUT = processor.getInteger(NAME_HTTP_SERVER_KEEPALIVE_TIMEOUT, 75 * 1000);


	private static final String NAME_VALID_COOKIE = "proxy.valid_cookie";
	public final static Boolean VALID_COOKIE = processor.getBoolean(NAME_VALID_COOKIE, false);

	private static final String NAME_HEALTH_INVALID_TIME = "proxy.health_invalid_time";
	public static final Integer HEALTH_INVALID_TIME = processor.getInteger(NAME_HEALTH_INVALID_TIME, 10 * 1000);


		private static final String NAME_HTTP_HEADER_REMOTE_ADDRESS = "proxy.http.header.remote.address";
	public static final String HTTP_HEADER_REMOTE_ADDRESS = processor.getString(NAME_HTTP_HEADER_REMOTE_ADDRESS, "");

}
