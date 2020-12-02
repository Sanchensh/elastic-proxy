package com.mrxu.netty.filter.pre;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.SessionContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import lombok.extern.slf4j.Slf4j;

import static com.mrxu.netty.util.ByteBufManager.close;

@Slf4j
public class HttpProtocolCheckFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + HttpProtocolCheckFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(final AbstractFilterContext filterContext, final SessionContext sessionContext) throws CustomException {
        FullHttpRequest request = sessionContext.getFullHttpRequest();
        DecoderResult decoderResult = request.getDecoderResult();
        if (decoderResult != null) {
            if (decoderResult.isFailure()) {
                if (decoderResult.cause() instanceof TooLongFrameException) {
                    ProxyRunner.errorProcess(sessionContext, new CustomException("request error", "Http line is larger than max length"));
                } else if (decoderResult.cause() instanceof IllegalArgumentException) {
                    ProxyRunner.errorProcess(sessionContext, new CustomException("uri error", "Header name cannot contain non-ASCII characters"));
                } else if (decoderResult.cause() instanceof ErrorDataDecoderException) {
                    ProxyRunner.errorProcess(sessionContext, new CustomException("bad request body", "Request body exist illegal characters"));
                } else if (decoderResult.cause() instanceof PrematureChannelClosureException) {
                    ProxyRunner.errorProcess(sessionContext, new CustomException("reset", "Http request reset"));
                } else {
                    ProxyRunner.errorProcess(sessionContext, new CustomException("unknown error", "Unknown error,please check your request or see log"));
                }
                close(sessionContext);
                return;
            }
        }

        filterContext.fireNext(sessionContext);
    }
}
