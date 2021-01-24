package com.mrxu.cache;

import com.mrxu.model.ElasticsearchNodeInfo;
import com.mrxu.netty.prop.PropertiesUtil;
import com.mrxu.sniff.ClusterManager;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("ElasticsearchCache")
public class ElasticsearchCache {

    @Bean("ClusterManager")
    public ClusterManager getClusterManager(){
        ClusterManager clusterManager = new ClusterManager();
        ElasticsearchNodeInfo elasticsearchNodeInfo = new ElasticsearchNodeInfo();
        elasticsearchNodeInfo.setIp(PropertiesUtil.properties.getIp());
        elasticsearchNodeInfo.setPort(PropertiesUtil.properties.getPort());
        elasticsearchNodeInfo.setHost(PropertiesUtil.properties.getIp() + ":"+PropertiesUtil.properties.getPort());
        elasticsearchNodeInfo.setUsername(PropertiesUtil.properties.getUsername());
        elasticsearchNodeInfo.setPassword(PropertiesUtil.properties.getPassword());
        clusterManager.addDeadNode(elasticsearchNodeInfo);
        return clusterManager;
    }
}
