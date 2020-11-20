package com.mrxu.netty.balance;

import com.mrxu.model.ElasticSearchInfo;

import java.util.List;

public interface LoadBalance {
    ElasticSearchInfo invoker(List<ElasticSearchInfo> infoList, Integer clusterId);
}
