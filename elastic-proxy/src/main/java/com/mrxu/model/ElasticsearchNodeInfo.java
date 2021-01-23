package com.mrxu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ElasticsearchNodeInfo {
//    集群id
    private Integer clusterId;
//    集群名
    private String clusterName;
//    节点IP
    private String ip;
//    节点端口 http
    private Integer port;
//    ip + ":" +port
    private String host;
//    集群用户名
    private String username;
//    集群密码
    private String password;
//    根据用户名和密码生成的密码
    private String authorization;
//    集群版本
    private String clusterVersion;
}
