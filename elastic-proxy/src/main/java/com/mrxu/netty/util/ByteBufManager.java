package com.mrxu.netty.util;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.ProxyRunner;
import com.mrxu.netty.SessionContext;
import com.mrxu.netty.timer.TimerController;
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
        TimerController.stopTimer(sessionContext);//关闭超时
        ProxyRunner.errorProcess(sessionContext, customException);
    }
}
