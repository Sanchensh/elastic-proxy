package com.mrxu.netty.filter.prepare;

import com.mrxu.exception.CustomException;
import com.mrxu.model.SearchDTO;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.SessionContext;
import com.mrxu.netty.util.ByteBufManager;
import com.mrxu.netty.util.IndexPatternUtil;
import com.mrxu.sql.component.SqlParserInterface;
import com.mrxu.sql.exception.SqlParseException;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.elasticsearch.common.collect.Tuple;

import java.sql.SQLFeatureNotSupportedException;


/**
 * 该类只解析SQL，也就是原生请求不到这一步
 */
@Slf4j
public class SqlParseAndCheckFilter extends AbstractFilter {

    public static String DEFAULT_NAME = PRE_FILTER_NAME + SqlParseAndCheckFilter.class.getSimpleName().toUpperCase();

    @Override
    public void run(final AbstractFilterContext filterContext, final SessionContext sessionContext) throws CustomException {
        FullHttpRequest fullHttpRequest = sessionContext.getFullHttpRequest();
        //获取请求的请求体
        SearchDTO searchDTO = IndexPatternUtil.getSearchDTO(fullHttpRequest);
        if (StringUtils.isNotEmpty(searchDTO.getSql())) {
            try {
                //解析SQL，拿到索引与解析后的DSL语句
                Tuple<String[], String> tuple = SqlParserInterface.singleton.explain2Tuple(searchDTO.getSql());
                String[] indexes = tuple.v1();
                boolean bool = IndexPatternUtil.validIndex(indexes);
                if (!bool) {
                    ByteBufManager.close(sessionContext,new CustomException("index does not match", "indices do not match same index pattern"));
                    return;
                }
                searchDTO.setIndices(indexes);
                if (StringUtils.isBlank(searchDTO.getIndex())) {
                    searchDTO.setIndex(IndexPatternUtil.getIndices(indexes));
                }
                searchDTO.setJson(tuple.v2());
                //sql校验index pattern
                String indexPattern = IndexPatternUtil.getPattern(indexes);
                sessionContext.setIndexPattern(indexPattern);
            } catch (SQLFeatureNotSupportedException | SqlParseException e) {
                log.error("解析SQL出错，错误信息:{}", ExceptionUtils.getStackTrace(e));
                ByteBufManager.close(sessionContext,new CustomException("sql parser error", e.getMessage()));
                return;
            }
        } else {
            //没有SQL情况，index是直接传的
            String indexPattern = IndexPatternUtil.getPattern(searchDTO.getIndex());
            sessionContext.setIndexPattern(indexPattern);
        }
        sessionContext.setSearchDTO(searchDTO);
        filterContext.fireNext(sessionContext);
    }

    @Override
    public String name() {
        return DEFAULT_NAME;
    }
}
