package com.mrxu.model;

import lombok.Data;

@Data
public class SearchDTO {
    //SQL语句
    private String sql;
    //DSL语句
    private String json;
    //索引
    private String[] indices;
    //最终获得的所有索引组成的字符串
    private String index;
    //滚动查询时的scroll_id
    private String scrollId;
    //滚动查询时，scroll_id存活时间
    private String keepAlive = "1m";
    //文档类型
    private String type = "_doc";
    //新增数据时候的id
    private String id;
}
