package com.mrxu.netty.balance;

import com.mrxu.model.ElasticSearchInfo;

import java.util.List;
import java.util.Random;

public class RandomBalance implements LoadBalance {
    private final static Random rand = new Random();
    @Override
    public ElasticSearchInfo invoker(List<ElasticSearchInfo> infoList, Integer clusterId) {
        int size = infoList.size();
        return infoList.get(rand.nextInt(size));
    }
}
