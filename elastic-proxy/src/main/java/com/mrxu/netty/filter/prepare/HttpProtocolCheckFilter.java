package com.mrxu.netty.filter.prepare;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.model.SessionContext;
import com.mrxu.netty.util.ByteBufManager;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import lombok.extern.slf4j.Slf4j;

/**
 * http请求协议的校验
 */
@Slf4j
public class HttpProtocolCheckFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + HttpProtocolCheckFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(final AbstractFilterContext filterContext, final SessionContext sessionContext) throws CustomException {
        FullHttpRequest fullHttpRequest = sessionContext.getFullHttpRequest();
        DecoderResult decoderResult = fullHttpRequest.decoderResult();
        if (decoderResult != null) {
            if (decoderResult.isFailure()) {
                CustomException customException;
                if (decoderResult.cause() instanceof TooLongFrameException) {
                    customException = new CustomException("request error", "Http line is larger than max length");
                } else if (decoderResult.cause() instanceof IllegalArgumentException) {
                    customException = new CustomException("uri error", "Header name cannot contain non-ASCII characters");
                } else if (decoderResult.cause() instanceof ErrorDataDecoderException) {
                    customException = new CustomException("bad request body", "Request body exist illegal characters");
                } else if (decoderResult.cause() instanceof PrematureChannelClosureException) {
                    customException = new CustomException("reset", "Http request reset");
                } else {
                    customException = new CustomException("unknown error", "Unknown error,please check your request or see log");
                }
                ByteBufManager.close(sessionContext, customException);
                return;
            }
        }
        //目前只支持post请求
        if (!fullHttpRequest.method().name().equalsIgnoreCase("POST")) {
            ByteBufManager.close(sessionContext,new CustomException("method error", "request method must be POST"));
            return;
        }
        filterContext.fireNext(sessionContext);
    }
}
