package com.mrxu.netty.filter.rest;

import com.alibaba.fastjson.JSONObject;
import com.mrxu.exception.CustomException;
import com.mrxu.model.CommonDTO;
import com.mrxu.netty.boot.ProxyRunner;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.pojo.SessionContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static com.mrxu.netty.util.Uri.*;

public class ESRequestUriFilter extends AbstractFilter {
    public static String DEFAULT_NAME = PRE_FILTER_NAME + ESRequestUriFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
        FullHttpRequest httpRequest = sessionContext.getFullHttpRequest();
        CommonDTO commonDTO = sessionContext.getCommonDTO();
        String index = commonDTO.getIndex();
        String uri ;
        String method;
        switch (httpRequest.uri()) {
            case search:
                uri = index + es_search;
                method = "POST";
                break;
            case insert:
                uri = getEndPoint(sessionContext);
                method = "POST";
                break;
            case updateById:
                uri = getEndpoint(sessionContext, es_update);
                method = "POST";
                break;
            case updateByQuery:
                method = "POST";
                uri = index + es_update_by_query;
                break;
            case delete:
                method = "POST";
                uri = index + es_delete;
                break;
            case searchScroll:
                method = "POST";
                //scroll id 不是空则是滚动查询
                if (!StringUtils.isBlank(commonDTO.getScrollId())) {
                    Scroll scroll = new Scroll(commonDTO.getKeepAlive(), commonDTO.getScrollId());
                    String json = Scroll.getJson(scroll);
                    uri = es_search_scroll;
                    //设置新的查询语句
                    commonDTO.setJson(json);
                } else {
                    uri = index + es_scroll + commonDTO.getKeepAlive();
                }
                break;
            default:
                ProxyRunner.errorProcess(sessionContext, new CustomException("path not found", "Request uri is not found"));
                return;
        }
        sessionContext.setRestRequestUri(uri);
        sessionContext.setRestRequestMethod(method);

        filterContext.fireNext(sessionContext);
    }

    //更新
    private String getEndpoint(SessionContext context, String endpoint) {
        CommonDTO commonDTO = context.getCommonDTO();
        String index = commonDTO.getIndex();
        String id = commonDTO.getId();
        return index + endpoint + "/" + (Objects.isNull(id) ? "" : id);
    }

    //新增or查询or删除
    private String getEndPoint(SessionContext context) {
        CommonDTO commonDTO = context.getCommonDTO();
        String index = commonDTO.getIndex();
        String type = commonDTO.getType();
        String id = commonDTO.getId();
        return index + "/" + type + "/" + (Objects.isNull(id) ? "" : id);
    }

    @Data
    private static class Scroll {
        private String scroll = "1m";
        private String scroll_id;

        public Scroll(String scroll, String scroll_id) {
            if (StringUtils.isNoneBlank(scroll))
                this.scroll = scroll;
            this.scroll_id = scroll_id;
        }

        public static String getJson(Scroll scroll) throws CustomException {
			if (StringUtils.isBlank(scroll.getScroll_id())) {
				throw new CustomException("scroll search error","scroll id can not be null");
			}
            return JSONObject.toJSONString(scroll);
        }
    }
}
