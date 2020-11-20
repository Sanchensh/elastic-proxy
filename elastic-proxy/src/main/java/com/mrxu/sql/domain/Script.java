package com.mrxu.sql.domain;

import lombok.Data;

@Data
public class Script {
    private String lang = "painless";
    private String source;
//    private String params;
}
