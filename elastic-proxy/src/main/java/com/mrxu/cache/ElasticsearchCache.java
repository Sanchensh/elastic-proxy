package com.mrxu.cache;

import com.mrxu.model.ClusterNodeInfo;
import com.mrxu.netty.prop.PropertiesUtil;
import com.mrxu.sniff.ClusterManager;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("ElasticsearchCache")
public class ElasticsearchCache {

    @Bean("ClusterManager")
    public ClusterManager getClusterManager() {
        ClusterManager clusterManager = new ClusterManager();
        ClusterNodeInfo clusterNodeInfo = new ClusterNodeInfo();
        clusterNodeInfo.setClusterName("elasticsearch");
        clusterNodeInfo.setIp(PropertiesUtil.properties.getIp());
        clusterNodeInfo.setPort(PropertiesUtil.properties.getPort());
        clusterNodeInfo.setHost(PropertiesUtil.properties.getIp() + ":" + PropertiesUtil.properties.getPort());
        clusterNodeInfo.setUsername(PropertiesUtil.properties.getUsername());
        clusterNodeInfo.setPassword(PropertiesUtil.properties.getPassword());
        clusterManager.addDeadNode(clusterNodeInfo);
        return clusterManager;
    }
}
