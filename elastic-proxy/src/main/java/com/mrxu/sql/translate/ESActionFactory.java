/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.mrxu.sql.translate;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.mrxu.sql.domain.*;
import com.mrxu.sql.domain.nested.NestedFieldRewriter;
import com.mrxu.sql.exception.SqlParseException;
import com.mrxu.sql.parser.SqlParser;
import com.mrxu.sql.parser.UpdateParser;
import com.mrxu.sql.utils.Util;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLFeatureNotSupportedException;


//处理SQL，将SQL分解成语法树
@Slf4j
public class ESActionFactory {

    public static QueryAction create(String sql) throws SqlParseException, SQLFeatureNotSupportedException {
        //默认格式
        return create(new QueryActionRequest(sql, new ColumnTypeProvider(), Format.JSON));
    }

    /**
     * Create the compatible Query object
     * based on the SQL query.
     *
     * @param request The SQL query.
     * @return Query object.
     */
    public static QueryAction create(QueryActionRequest request)
            throws SqlParseException, SQLFeatureNotSupportedException {
        String sql = request.getSql();
        // Remove line breaker anywhere and semicolon at the end
        sql = sql.replaceAll("\\R", " ").trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        switch (getFirstWord(sql)) {
            case "SELECT":
                SQLQueryExpr sqlExpr = (SQLQueryExpr) Util.toSqlExpr(sql);
                RewriteRuleExecutor<SQLQueryExpr> ruleExecutor = RewriteRuleExecutor.builder()
                        .withRule(new SQLExprParentSetterRule())
                        .withRule(new OrdinalRewriterRule(sql))
                        .withRule(new UnquoteIdentifierRule())
                        .withRule(new TableAliasPrefixRemoveRule())
                        .withRule(new SubQueryRewriteRule())
                        .build();
                ruleExecutor.executeOn(sqlExpr);
                sqlExpr.accept(new NestedFieldRewriter());
                sqlExpr.accept(new TermFieldRewriter());
                Select select = new SqlParser().parseSelect(sqlExpr);
                return handleSelect(select, true);
            case "DELETE":
                SQLStatementParser parser = createSqlStatementParser(sql);
                SQLDeleteStatement deleteStatement = parser.parseDeleteStatement();
                Delete delete = new SqlParser().parseDelete(deleteStatement);
                return new DeleteQueryAction(delete);
            case "UPDATE":
                return new UpdateAction(sql);
            default:
                throw new SQLFeatureNotSupportedException(String.format("Query must start with SELECT or DELETE: %s", sql));
        }
    }

    //获取第一个单词，判断是什么查询
    public static String getFirstWord(String sql) {
        int endOfFirstWord = sql.indexOf(' ');
        return sql.substring(0, endOfFirstWord > 0 ? endOfFirstWord : sql.length()).toUpperCase();
    }

    //判断SQL语句类型，是聚合查询还是普通查询
    private static QueryAction handleSelect(Select select, boolean page) {
        return select.isAggregate ? new AggregationQueryAction(select) : new DefaultQueryAction(select, page);
    }

    private static SQLStatementParser createSqlStatementParser(String sql) {
        ElasticLexer lexer = new ElasticLexer(sql);
        lexer.nextToken();
        return new MySqlStatementParser(lexer);
    }
}
