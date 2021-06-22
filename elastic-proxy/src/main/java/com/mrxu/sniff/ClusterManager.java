package com.mrxu.sniff;

import com.alibaba.fastjson.JSON;
import com.jayway.jsonpath.JsonPath;
import com.mrxu.model.ClusterNodeInfo;
import com.mrxu.netty.property.PropertiesUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class ClusterManager implements AutoCloseable {
    private List<ClusterNodeInfo> activeNodes = new ArrayList<>();//白名单
    private List<ClusterNodeInfo> deadNodes = new ArrayList<>();//白名单
    private int index = 0;
    private Scheduler scheduler;
    private final RestTemplate restTemplate = new RestTemplate();

    public ClusterManager() {
        if (PropertiesUtil.properties.isSniffer()){
            long millis = TimeUnit.SECONDS.toMillis(PropertiesUtil.properties.getSnifferTime());
            this.scheduler = new DefaultScheduler();
            Task task = new Task(millis) {
                @Override
                public void run() {
                    super.run();
                }
            };
            this.scheduler.schedule(task, millis);
        }
    }

    //获取可用节点
    public ClusterNodeInfo getActiveNode() {
        synchronized (this) {
            return CollectionUtils.isEmpty(activeNodes) ? loadBalance.apply(deadNodes) : loadBalance.apply(activeNodes);
        }
    }

    public void addDeadNode(ClusterNodeInfo clusterNodeInfo) {
        deleteFromActiveNodes(clusterNodeInfo.getHost());
        deadNodes.add(clusterNodeInfo);
    }

    @Override
    public void close() throws IOException {
        scheduler.shutdown();
    }

    //从存活节点中删除一个
    private void deleteFromActiveNodes(String host) {
        activeNodes.removeIf(clusterNodeInfo -> host.equals(clusterNodeInfo.getHost()));
    }

    //节点轮询策略
    private Function<List<ClusterNodeInfo>, ClusterNodeInfo> loadBalance = (infoList) -> {
        int size = infoList.size();
        if (index >= size) {
            index = 0;
            return infoList.get(0);
        } else {
            return infoList.get(index++);
        }
    };

    private void sniffNodes() {
        ClusterNodeInfo clusterNodeInfo = getActiveNode();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", clusterNodeInfo.getAuthorization());
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            String url = "http://" + clusterNodeInfo.getHost() + "/_nodes/http";
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            String data = responseEntity.getBody();
            List<String> hosts = JsonPath.read(data, "$.nodes..http.bound_address[-1]");
            if (CollectionUtils.isNotEmpty(hosts)) {
                List<NodeInfo> nodeInfos = hosts.stream().map(host -> {
                    String[] ipAndPort;
                    if (host.startsWith("[")) {
                        String ip = PropertiesUtil.properties.getIp();
                        String port = PropertiesUtil.properties.getPort().toString();
                        ipAndPort = new String[]{ip, port};
                    } else {
                        ipAndPort = host.split(":");
                    }
                    NodeInfo nodeInfo = NodeInfo.builder()
                            .ip(ipAndPort[0])
                            .port(Integer.parseInt(ipAndPort[1]))
                            .host(ipAndPort[0] + ":" + ipAndPort[1])
                            .build();
                    return nodeInfo;
                }).collect(Collectors.toList());
                synchronized (this) {
                    //直接将最新节点赋值给activeNodes
                    activeNodes = nodeInfos.stream()
                            .map(nodeInfo -> {
                                ClusterNodeInfo elasticSearch = ClusterNodeInfo.builder()
                                        .ip(nodeInfo.getIp())
                                        .port(nodeInfo.getPort())
                                        .host(nodeInfo.getHost())
                                        .password(clusterNodeInfo.getPassword())
                                        .authorization(clusterNodeInfo.getAuthorization())
                                        .clusterName(clusterNodeInfo.getClusterName())
                                        .clusterId(clusterNodeInfo.getClusterId())
                                        .clusterVersion(clusterNodeInfo.getClusterVersion())
                                        .username(clusterNodeInfo.getUsername())
                                        .build();
                                return elasticSearch;
                            }).collect(Collectors.toList());
                    deadNodes.clear();
                }
                log.info("集群名：{}，嗅探结果：{}", clusterNodeInfo.getClusterName(), JSON.toJSONString(activeNodes.stream().map(ClusterNodeInfo::getHost).collect(Collectors.toList())));
            }
        } catch (Exception e) {
            log.error("集群嗅探异常:{}集群名称是：{}，当前存活节点:{}", ExceptionUtils.getStackTrace(e),
                    clusterNodeInfo.getClusterName(),
                    JSON.toJSONString(activeNodes.stream().map(ClusterNodeInfo::getHost).collect(Collectors.toList())));
        }
    }

    @Data
    @Builder
    private static class NodeInfo {
        private String ip;
        private Integer port;
        private String host;
    }

    private class Task implements Runnable {
        final long nextTaskDelay;

        Task(long nextTaskDelay) {
            this.nextTaskDelay = nextTaskDelay;
        }

        @Override
        public void run() {
            sniffNodes();
            Task task = new Task(nextTaskDelay);
            scheduler.schedule(task, nextTaskDelay);
        }
    }
}
