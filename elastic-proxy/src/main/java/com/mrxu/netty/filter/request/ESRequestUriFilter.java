package com.mrxu.netty.filter.request;

import com.alibaba.fastjson.JSONObject;

import com.mrxu.exception.CustomException;
import com.mrxu.model.SearchDTO;
import com.mrxu.netty.filter.AbstractFilter;
import com.mrxu.netty.filter.AbstractFilterContext;
import com.mrxu.netty.SessionContext;
import com.mrxu.netty.util.ByteBufManager;
import com.mrxu.netty.util.Uri;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;

public class ESRequestUriFilter extends AbstractFilter {
    public static String DEFAULT_NAME = PRE_FILTER_NAME + ESRequestUriFilter.class.getSimpleName().toUpperCase();

    @Override
    public String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
        FullHttpRequest fullHttpRequest = sessionContext.getFullHttpRequest();
        SearchDTO commonDTO = sessionContext.getSearchDTO();
        String index = commonDTO.getIndex();
        String uri;
        HttpMethod method;
        switch (fullHttpRequest.uri()) {
            case Uri.search:
                uri = index + Uri.es_search;
                method = HttpMethod.POST;
                break;
            case Uri.insert:
                uri = getEndPoint(sessionContext);
                method = HttpMethod.POST;
                break;
            case Uri.updateById:
                uri = getEndpoint(sessionContext, Uri.es_update);
                method = HttpMethod.POST;
                break;
            case Uri.updateByQuery:
                method = HttpMethod.POST;
                uri = index + Uri.es_update_by_query;
                break;
            case Uri.delete:
                method = HttpMethod.POST;
                uri = index + Uri.es_delete;
                break;
            case Uri.searchScroll:
                method = HttpMethod.POST;
                //scroll id 不是空则是滚动查询
                if (!StringUtils.isBlank(commonDTO.getScrollId())) {
                    Scroll scroll = new Scroll(commonDTO.getKeepAlive(), commonDTO.getScrollId());
                    String json = Scroll.getJson(scroll);
                    uri = Uri.es_search_scroll;
                    //设置新的查询语句
                    commonDTO.setJson(json);
                } else {
                    uri = index + Uri.es_scroll + commonDTO.getKeepAlive();
                }
                break;
            default:
                ByteBufManager.close(sessionContext, new CustomException("path not found", "Request uri is not found"));// 关闭掉channel
                return;
        }
        sessionContext.setRestRequestUri(uri);
        sessionContext.setRestRequestMethod(method);
        filterContext.fireNext(sessionContext);
    }

    //更新
    private String getEndpoint(SessionContext sessionContext, String endpoint) {
        SearchDTO commonDTO = sessionContext.getSearchDTO();
        String index = commonDTO.getIndex();
        String type = commonDTO.getType();
        String id = commonDTO.getId();
        //6.x更新的endpoint
//        if (Integer.parseInt(proxyIndexCluster.getClusterVersion().split("\\.")[0]) < 7) {
//            return index + "/" + type + (Objects.isNull(id) ? "" : "/" + id) + endpoint;
//        } else {//7.x以上的endpoint
//            return index + endpoint + "/" + (Objects.isNull(id) ? "" : id);
//        }
        return index + endpoint + "/" + (Objects.isNull(id) ? "" : id);
    }

    //新增or查询or删除
    private String getEndPoint(SessionContext sessionContext) {
        SearchDTO commonDTO = sessionContext.getSearchDTO();
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
            if (StringUtils.isNoneBlank(scroll)) {
                this.scroll = scroll;
            }
            this.scroll_id = scroll_id;
        }

        public static String getJson(Scroll scroll) throws CustomException {
            if (StringUtils.isBlank(scroll.getScroll_id())) {
                throw new CustomException("scroll search error", "scroll id can not be null");
            }
            return JSONObject.toJSONString(scroll);
        }
    }
}
