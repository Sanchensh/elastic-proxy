package com.mrxu.netty.util;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.pojo.SessionContext;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ByteBufManager {

    public static void deepSafeRelease(Object msg) {
        ReferenceCountUtil.release(msg);
    }

    /**
     * 异常时候关闭channel
     * @param sessionContext
     * @param customException
     */
    public static void close(SessionContext sessionContext, CustomException customException) {
        sessionContext.stopTimer();//关闭超时
        ProxyRunner.errorProcess(sessionContext, customException);
        sessionContext.getServerChannel().close(); // 如果在解析的时候失败，则直接关闭掉该channel。
    }
}
