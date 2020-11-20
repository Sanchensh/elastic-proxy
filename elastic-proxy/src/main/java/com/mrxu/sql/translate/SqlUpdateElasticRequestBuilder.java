package com.mrxu.sql.translate;

import com.mrxu.sql.connector.SqlElasticRequestBuilder;
import com.mrxu.sql.exception.SqlParseException;
import com.mrxu.sql.parser.UpdateParser;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.collect.Tuple;

import java.sql.SQLFeatureNotSupportedException;

@Slf4j
public class SqlUpdateElasticRequestBuilder implements SqlElasticRequestBuilder {
    private String sql;

    private Tuple<String[], String> tuple;

    public SqlUpdateElasticRequestBuilder(String sql) {
        this.sql = sql;
    }

    public Tuple<String[], String> getTuple(){
        return this.tuple;
    }

    @Override
    public ActionRequest request() {
        return null;
    }

    @Override
    public String explain() {
        try {
            Tuple<String[], String> tuple = UpdateParser.explainUpdate(this.sql);
            this.tuple = tuple;
            return "";
        } catch (SQLFeatureNotSupportedException | SqlParseException e) {
            log.info("update 语句解析失败");
            return null;
        }
    }

    @Override
    public ActionResponse get() {
        return null;
    }

    @Override
    public ActionRequestBuilder getBuilder() {
        return null;
    }
}
