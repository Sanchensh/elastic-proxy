package com.mrxu.sql.parser;

import com.alibaba.fastjson.JSONObject;
import com.mrxu.sql.domain.Script;
import com.mrxu.sql.domain.Update;
import com.mrxu.sql.exception.SqlParseException;
import org.elasticsearch.common.collect.Tuple;

import java.sql.SQLFeatureNotSupportedException;

import static com.mrxu.sql.component.SearchComponent.singleton;

public class UpdateParser {

    public static Tuple<String[],String> explainUpdate(String sql) throws SQLFeatureNotSupportedException, SqlParseException {
        String temp = sql.toLowerCase();
        String select;
        String index = getIndex(sql);

        if (temp.contains("where"))
            select = "SELECT * FROM " + index + " " + sql.substring(temp.indexOf("where") - 1);
        else
            select = "SELECT * FROM " + index;

        Update update = interpret(sql);
        String updateJson = JSONObject.toJSONString(update);
        JSONObject updObj = JSONObject.parseObject(updateJson);




        String dsl = singleton.explain(select);
        JSONObject dslObj = JSONObject.parseObject(dsl);
        dslObj.remove("size");
        dslObj.remove("from");

        dslObj.putAll(updObj);


        String json = JSONObject.toJSONString(dslObj);
        Tuple<String[],String> tuple = new Tuple<>(index.split(","),json);
        return tuple;
    }

    public static String getIndex(String sql) {
        String temp = sql.toLowerCase();
        return sql.substring(temp.indexOf("update") + 6, temp.indexOf("set")).trim();
    }

    public static String[] getSet(String sql) {
        String temp = sql.toLowerCase();
        String condition;
        if (temp.contains("where"))
            condition = sql.substring(temp.indexOf("set") + 3, temp.indexOf("where") - 1).trim();
        else
            condition = sql.substring(temp.indexOf("set") + 3).trim();
        return condition.split(",");
    }

    public static Update interpret(String sql) throws SqlParseException {
        String[] condition = getSet(sql);

        Update update = new Update();
        Script script = new Script();
        StringBuffer sb = new StringBuffer();
        for (String con : condition) {
            sb.append(getResult(con));
        }
        script.setSource(sb.toString());
        update.setScript(script);
        return update;
    }

    public static String getResult(String con) throws SqlParseException {
        if (!con.contains("="))
            throw new SqlParseException("修改条件缺少'='号");
        String[] split = con.split("=");
        return getKey(split);
    }

    public static String getKey(String[] split) {
        String key = split[0].trim();
        String value = split[1].trim();
        return key.contains(".") ? "ctx._source['" + key.substring(0, key.indexOf(".")) + "']" + key.substring(key.indexOf(".")) + "=" + value + ";" :
                "ctx._source['" + key + "']" + "=" + value + ";";
    }
}
