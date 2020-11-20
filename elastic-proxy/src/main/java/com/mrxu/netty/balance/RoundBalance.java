package com.mrxu.netty.balance;

import com.mrxu.model.ElasticSearchInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RoundBalance implements LoadBalance {
    //存储当前集群到哪个节点了
    private static ConcurrentHashMap<Integer, Integer> currentCluster = new ConcurrentHashMap<>();

    @Override
    public ElasticSearchInfo invoker(List<ElasticSearchInfo> infoList, Integer clusterId) {
        if (CollectionUtils.isEmpty(infoList)) {
            return null;
        }
        //如果map中没有，直接给出第一个节点
        if (Objects.isNull(currentCluster.get(clusterId))) {
            currentCluster.put(clusterId, 0);
            return infoList.get(0);
        } else {//如果map中有信息
            Integer current = currentCluster.get(clusterId);
            int size = infoList.size();
            //如果当前节点索引大于或等于节点总数则直接返回第一个
            if (current >= size - 1) {//因为这里可能节点被剔除，如果单纯判断==，则会出错
                currentCluster.put(clusterId, 0);
                return infoList.get(0);
            } else {//否则直接返回当前索引的下一个节点
                int i = current + 1;
                currentCluster.put(clusterId, i);
                return infoList.get(i);
            }
        }
    }
}
