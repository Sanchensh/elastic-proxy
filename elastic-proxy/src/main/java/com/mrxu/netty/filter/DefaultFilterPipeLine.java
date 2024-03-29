package com.mrxu.netty.filter;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//单例
public enum DefaultFilterPipeLine implements FilterPipeLine {
    INSTANCE;
    private final Map<String, AbstractFilterContext> name2ctx = new ConcurrentHashMap<>(16);
    private final List<String> index = new ArrayList<>();

    @Override
    public void addLastSegment(Filter... filters) {
        for (Filter filter : filters) {
            checkDuplicateName(filter.name());
            index.add(filter.name());
            name2ctx.put(filter.name(), new FilterContext(filter));
        }
        if (index.size() <= 1) {
            return;
        }
        for (int i = 0; i < index.size() - 1; i++) {
            name2ctx.get(index.get(i)).next = name2ctx.get(index.get(i + 1));
        }
    }

    @Override
    public AbstractFilterContext get(String name) {
        return name2ctx.get(name);
    }

    private void checkDuplicateName(String name) {
        if (name2ctx.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate filter name: " + name);
        }
    }
}
