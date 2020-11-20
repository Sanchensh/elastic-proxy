package com.mrxu.netty.util;

import com.mrxu.netty.pojo.SessionContext;
import io.netty.buffer.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Slf4j
public class ByteBufManager {

    public static final int DEFAULT_BUF_SIZE = 8192;

    // Netty的PooledByteBufAllocator与Fiber不配，hack PooledByteBufAllocator，把FastThreadLocal改成继承TrueThreadLocal
    // public static ByteBufAllocator byteBufAllocator = OspSystemEnvProperties.USE_FIBER
    //		? new UnpooledByteBufAllocator(false) : new PooledByteBufAllocator(true);


    public static final ByteBufAllocator byteBufAllocator = new PooledByteBufAllocator(true);


    public static void initByteBufManager() {
        directBuffer(ByteBufManager.DEFAULT_BUF_SIZE).release();
    }

    /**
     * 分配pooled direct ByteBuf
     */
    public static ByteBuf directBuffer(int initialCapacity) {
        //如果要兼容堆内内存堆情况，使用buffer() 而不是directBuffer,让Netty自己多做一次判断
        int tempInitialCapacity = initialCapacity;
        if (tempInitialCapacity <= 0) {
            tempInitialCapacity = 128;
        }
        return byteBufAllocator.directBuffer(tempInitialCapacity);
    }

    public static void deepSafeRelease(ByteBuf buf) {
        //EmptyByte的refCnt固定为1，无须release
        if (buf == null || buf.refCnt() == 0 || buf instanceof EmptyByteBuf) {
            return;
        }
        ReferenceCountUtil.safeRelease(buf, buf.refCnt());
    }

	public static void deepSafeRelease(Object msg) {
		//EmptyByte的refCnt固定为1，无须release
        if (msg instanceof FullHttpRequest) {
            deepSafeRelease(((FullHttpRequest) msg).content());
        } else {
            try {
                ReferenceCountUtil.release(msg);
            } catch (Throwable throwable) {
                log.error("释放buffer出错，错误信息：{}", ExceptionUtils.getStackTrace(throwable));
            }
        }
	}

    public static void close(SessionContext sessionContext) {
        if (sessionContext.getClientChannel() != null) {
            try {
                sessionContext.getClientChannel().close(); // 如果在解析的时候失败，则直接关闭掉该channel。
            } catch (Throwable throwable) {
                log.error("关闭channel失败，错误信息：{}", ExceptionUtils.getStackTrace(throwable));
            }
        }
        //安全释放请求buffer
        deepSafeRelease(sessionContext.getRequest().getHttpRequest().content());
    }
}
