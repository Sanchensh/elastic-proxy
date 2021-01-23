package com.mrxu.sql.component;

import com.mrxu.sql.connector.SqlElasticRequestBuilder;
import com.mrxu.sql.exception.SqlParseException;
import com.mrxu.sql.translate.*;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.collect.Tuple;

import java.sql.SQLFeatureNotSupportedException;

//解释程序入口
public enum SqlParserInterface {
    singleton;

    public String explain(String sql) throws SQLFeatureNotSupportedException, SqlParseException {
        return ESActionFactory.create(sql).explain().explain();
    }

    public Tuple<String[], String> explain2Tuple(String sql) throws SQLFeatureNotSupportedException, SqlParseException {
        QueryAction queryAction = ESActionFactory.create(sql);
        SqlElasticRequestBuilder explain = queryAction.explain();
        ActionRequest action = explain.request();
        if (action instanceof SearchRequest) {
            SearchRequest searchRequest = (SearchRequest) action;
            return new Tuple<>(searchRequest.indices(), explain.explain());
        } else if (queryAction instanceof UpdateAction) {
            UpdateAction updateAction = (UpdateAction) queryAction;
            SqlUpdateElasticRequestBuilder builder = (SqlUpdateElasticRequestBuilder) updateAction.explain();
            builder.explain();
            return builder.getTuple();
        } else if (queryAction instanceof DeleteQueryAction) {
            return new Tuple<>(queryAction.getQuery().getIndexArr(), explain.explain());
        }
        throw new SQLFeatureNotSupportedException(String.format("Query must start with SELECT,UPDATE or DELETE: %s", sql));
    }
}
