package com.mrxu.netty.util;

import java.util.ArrayList;
import java.util.List;

public class Uri {
    public static final String insert = "/proxy/insert";
    public static final String search = "/proxy/search";
    public static final String updateById = "/proxy/updateById";
    public static final String delete = "/proxy/delete";
//    public static final String define = "/proxy/define";
    public static final String updateByQuery = "/proxy/updateByQuery";
    public static final String searchScroll = "/proxy/searchScroll";
//    public static final String selectById = "/proxy/selectById";
//    public static final String deleteById = "/proxy/deleteById";
//    public static final String refresh = "/proxy/refresh";

    public static final String es_search = "/_search?typed_keys=true";
    public static final String es_scroll = "/_search?scroll=";
    public static final String es_search_scroll = "/_search/scroll";
    public static final String es_delete = "/_delete_by_query";
    public static final String es_update_by_query = "/_update_by_query";
    public static final String es_update = "/_update";

    private static final List<String> url = new ArrayList<>();
    static {
        url.add(insert);
        url.add(search);
        url.add(updateById);
        url.add(delete);
//        url.add(define);
        url.add(updateByQuery);
        url.add(searchScroll);
//        url.add(deleteById);
//        url.add(selectById);
//        url.add(refresh);
    }
    public static boolean exist(String uri){
        return url.contains(uri);
    }
}
