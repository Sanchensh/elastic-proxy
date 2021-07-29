package com.mrxu.common;

/**
 * @author jlxu@telenav.cn
 * @date 2021/7/28/18:11
 */
public class Constants {
    public static final String SESSION_ATTRIBUTE = "session_attribute";
    public static final String CLIENT_EVENT_LOOP_NAME = "client_nio_event_loop_group";
    public static final String COLON = ":";
    public static final String TIMEOUT = "timeout";
    public static final String APPLICATION_NAME = "elasticsearch-proxy";
    public static final String SHORT_LINE = "-";
    public static final int CLIENT_MAX_CONTENT_LENGTH = 1024 * 1024 * 64;
    public static final int SERVER_MAX_CONTENT_LENGTH = CLIENT_MAX_CONTENT_LENGTH;
    public static final int ALL_MAX_SIZE = 8192;
    public static final int CONNECT_TIMEOUT_MILLIS = 5000;
    public static final int ZERO = 0;
    public static final int DEFAULT_QUEUE_SIZE = 100;
    public static final int DEFAULT_KEEPALIVE_TIME = 60;
    public static final int DEFAULT_TICK_DURATION = 1;
    public static final String SNIFFER_THREAD_NAME = "es_rest_client_sniffer"; //线程池名称
}
