package com.mrxu.netty.filter;

public interface FilterPipeLine {

	void addLastSegment(Filter... filters);

	AbstractFilterContext get(String name);
}
