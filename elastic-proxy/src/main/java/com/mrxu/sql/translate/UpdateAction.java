package com.mrxu.sql.translate;

import com.mrxu.sql.connector.SqlElasticRequestBuilder;
import com.mrxu.sql.domain.Query;
import com.mrxu.sql.exception.SqlParseException;

public class UpdateAction extends QueryAction {
    public UpdateAction(Query query) {
        super(query);
    }
    private String sql;
    private String indices;
    public UpdateAction(String  sql) {
        super(null);
        this.sql = sql;
    }

    @Override
    public SqlElasticRequestBuilder explain() throws SqlParseException {
        return new SqlUpdateElasticRequestBuilder(this.sql);
    }
}
