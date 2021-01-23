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

package com.mrxu.sql.utils;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;
import com.mrxu.sql.domain.IndexStatement;
import com.mrxu.sql.domain.KVValue;
import com.mrxu.sql.exception.SqlParseException;
import com.mrxu.sql.parser.ElasticSqlExprParser;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class Util {

    public static final String NESTED_JOIN_TYPE = "NestedJoinType";

    public static String joiner(List<KVValue> lists, String oper) {

        if (lists.size() == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(lists.get(0).toString());
        for (int i = 1; i < lists.size(); i++) {
            sb.append(oper);
            sb.append(lists.get(i).toString());
        }

        return sb.toString();
    }

    public static Object removeTableAilasFromField(Object expr, String tableAlias) {

        if (expr instanceof SQLIdentifierExpr || expr instanceof SQLPropertyExpr || expr instanceof SQLVariantRefExpr) {
            String name = expr.toString().replace("`", "");
            if (tableAlias != null) {
                String aliasPrefix = tableAlias + ".";
                if (name.startsWith(aliasPrefix)) {
                    String newFieldName = name.replaceFirst(aliasPrefix, "");
                    return new SQLIdentifierExpr(newFieldName);
                }
            }
        }
        return expr;
    }


    public static Object expr2Object(SQLExpr expr) {
        return expr2Object(expr, "");
    }

    public static Object expr2Object(SQLExpr expr, String charWithQuote) {
        Object value = null;
        if (expr instanceof SQLNumericLiteralExpr) {
            value = ((SQLNumericLiteralExpr) expr).getNumber();
        } else if (expr instanceof SQLCharExpr) {
            value = charWithQuote + ((SQLCharExpr) expr).getText() + charWithQuote;
        } else if (expr instanceof SQLIdentifierExpr) {
            value = expr.toString();
        } else if (expr instanceof SQLPropertyExpr) {
            value = expr.toString();
        } else if (expr instanceof SQLVariantRefExpr) {
            value = expr.toString();
        } else if (expr instanceof SQLAllColumnExpr) {
            value = "*";
        } else if (expr instanceof SQLValuableExpr) {
            value = ((SQLValuableExpr) expr).getValue();
        } else {
            //throw new SqlParseException("can not support this type " + expr.getClass());
        }
        return value;
    }

    public static Object getScriptValue(SQLExpr expr) throws SqlParseException {
        if (expr instanceof SQLIdentifierExpr || expr instanceof SQLPropertyExpr || expr instanceof SQLVariantRefExpr) {
            return "doc['" + expr.toString() + "'].value";
        } else if (expr instanceof SQLValuableExpr) {
            return ((SQLValuableExpr) expr).getValue();
        }
        throw new SqlParseException("could not parse sqlBinaryOpExpr need to be identifier/valuable got"
                + expr.getClass().toString() + " with value:" + expr.toString());
    }

    public static Object getScriptValueWithQuote(SQLExpr expr, String quote) throws SqlParseException {
        if (expr instanceof SQLIdentifierExpr || expr instanceof SQLPropertyExpr || expr instanceof SQLVariantRefExpr) {
            return "doc['" + expr.toString() + "'].value";
        } else if (expr instanceof SQLCharExpr) {
            return quote + ((SQLCharExpr) expr).getValue() + quote;
        } else if (expr instanceof SQLIntegerExpr) {
            return ((SQLIntegerExpr) expr).getValue();
        } else if (expr instanceof SQLNumericLiteralExpr) {
            return ((SQLNumericLiteralExpr) expr).getNumber();
        } else if (expr instanceof SQLNullExpr) {
            return expr.toString().toLowerCase();
        }
        throw new SqlParseException("could not parse sqlBinaryOpExpr need to be identifier/valuable got"
                + expr.getClass().toString() + " with value:" + expr.toString());
    }

    public static boolean isFromJoinOrUnionTable(SQLExpr expr) {
        SQLObject temp = expr;
        AtomicInteger counter = new AtomicInteger(10);
        while (temp != null && !(expr instanceof SQLSelectQueryBlock)
                && !(expr instanceof SQLJoinTableSource) && !(expr instanceof SQLUnionQuery) && counter.get() > 0) {
            counter.decrementAndGet();
            temp = temp.getParent();
            if (temp instanceof SQLSelectQueryBlock) {
                SQLTableSource from = ((SQLSelectQueryBlock) temp).getFrom();
                if (from instanceof SQLJoinTableSource || from instanceof SQLUnionQuery) {
                    return true;
                }
            }
            if (temp instanceof SQLJoinTableSource || temp instanceof SQLUnionQuery) {
                return true;
            }
        }
        return false;
    }

    public static double[] KV2DoubleArr(List<KVValue> params) {
        double[] ds = new double[params.size()];
        int i = 0;
        for (KVValue v : params) {
            ds[i] = Double.parseDouble(v.value.toString());
            i++;
        }
        return ds;
    }


    public static String extendedToString(SQLExpr sqlExpr) {
        if (sqlExpr instanceof SQLTextLiteralExpr) {
            return ((SQLTextLiteralExpr) sqlExpr).getText();
        }
        return sqlExpr.toString();
    }



    public static SQLExpr toSqlExpr(String sql) throws SqlParseException {
        SQLExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new SqlParseException("不合法的SQL语句: " + sql);
        }
        return expr;
    }

}
