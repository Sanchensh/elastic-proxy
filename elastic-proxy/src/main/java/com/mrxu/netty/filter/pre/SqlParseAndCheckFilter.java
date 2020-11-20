package com.mrxu.netty.filter.pre;

import com.mrxu.exception.CustomException;
import com.mrxu.model.CommonDTO;
import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.ProxyHttpRequest;
import com.mrxu.netty.pojo.SessionContext;
import com.mrxu.sql.exception.SqlParseException;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elasticsearch.common.collect.Tuple;
import java.sql.SQLFeatureNotSupportedException;
import static com.mrxu.netty.util.ByteBufManager.close;
import static com.mrxu.netty.util.IndexPatternUtil.*;
import static com.mrxu.sql.component.SearchComponent.singleton;

/**
 * 该类只解析SQL，也就是原生请求不到这一步
 */
@Slf4j
public class SqlParseAndCheckFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + SqlParseAndCheckFilter.class.getSimpleName().toUpperCase();

    @Override
    public void run(final AbstractFilterContext filterContext, final SessionContext sessionContext) throws CustomException {
        if (!sessionContext.getRequest().getHttpRequest().method().name().equalsIgnoreCase("POST")) {
            ProxyRunner.errorProcess(sessionContext, new CustomException("method error", "request method must be POST"));
            close(sessionContext);
            return;
        }

        try {
            ProxyHttpRequest request = sessionContext.getRequest();
            FullHttpRequest httpRequest = request.getHttpRequest();
            CommonDTO commonDTO = getCommonDTO(httpRequest);
            if (StringUtils.isNotEmpty(commonDTO.getSql())) {
                try {
                    Tuple<String[], String> tuple = singleton.explain2Tuple(commonDTO.getSql());
                    String[] indexes = tuple.v1();
                    boolean b = validIndex(indexes);
                    if (!b) {
                        ProxyRunner.errorProcess(sessionContext, new CustomException("index does not match", "indices do not match same index pattern"));
                        close(sessionContext);
                        return;
                    }
                    commonDTO.setIndices(indexes);
                    if (StringUtils.isBlank(commonDTO.getIndex())) {
                        commonDTO.setIndex(getIndices(indexes));
                    }
                    commonDTO.setJson(tuple.v2());
                    //sql校验index pattern
                    String indexPattern = getPattern(indexes);

                    sessionContext.setIndexPattern(indexPattern);
                } catch (SQLFeatureNotSupportedException | SqlParseException e) {
                    log.error("解析SQL出错，错误信息:{}", ExceptionUtils.getStackTrace(e));
                    ProxyRunner.errorProcess(sessionContext, new CustomException("sql parser error", e.getMessage()));
                    close(sessionContext);
                    return;
                }
            } else {
                //没有SQL情况，index是直接传的
                String indexPattern = getPattern(commonDTO.getIndex());
                sessionContext.setIndexPattern(indexPattern);
            }

            sessionContext.setCommonDTO(commonDTO);

        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        filterContext.fireNext(sessionContext);
    }

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

 }
