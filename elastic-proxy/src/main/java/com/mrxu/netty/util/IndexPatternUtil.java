package com.mrxu.netty.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mrxu.exception.CustomException;
import com.mrxu.model.SearchDTO;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;

import static io.netty.util.CharsetUtil.UTF_8;

public class IndexPatternUtil {

    public static final String suffix = "espaas";

    public static SearchDTO getSearchDTO(FullHttpRequest request) {
        ByteBuf byteBuf = request.content();
        String json = byteBuf.toString(UTF_8);
        return JSONObject.toJavaObject(JSON.parseObject(json), SearchDTO.class);
    }

    //校验是否匹配同一规则
    public static boolean validIndex(String[] indices) throws CustomException {
        if (indices.length == 1)
            return true;
        int j = indices.length - 1;
        int length = suffix.length();
        boolean bool = true;
        for (int i = 0; i <= j; i++, j--) {
            //索引都没有后缀的情况
            if (!indices[i].contains(suffix) && !indices[j].contains(suffix)) {
                if (indices[i].equals(indices[j])) {
                    continue;
                } else {
                    bool = false;
                }
            } else if (indices[i].contains(suffix) && indices[j].contains(suffix)) {//都有后缀的情况
                String first = indices[i].substring(0, indices[i].indexOf(suffix) + length);
                String last;
                if (i == j) {
                    last = indices[i - 1].substring(0, indices[i - 1].indexOf(suffix) + length);
                } else {
                    last = indices[j].substring(0, indices[j].indexOf(suffix) + length);
                }
                if (!first.equals(last))
                    bool = false;
            } else {
                bool = false;
            }
        }
        return bool;
    }

    /**
     * 获取index_pattern
     * 如果只有一个索引，那么直接根据suffix截取（因为有可能有迭代）
     */

    public static String getPattern(String... indices) {
        String pattern = indices[0];
        return pattern.contains(suffix) ? pattern.substring(0, pattern.indexOf(suffix) + suffix.length()) : pattern;
    }

    public static String getIndices(String[] indices) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < indices.length; i++) {
            if (i == indices.length - 1) {
                s.append(indices[i]);
            } else {
                s.append(indices[i]).append(",");
            }
        }
        return s.toString();
    }
}
