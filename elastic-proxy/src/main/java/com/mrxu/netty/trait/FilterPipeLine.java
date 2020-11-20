package com.mrxu.netty.trait;

import com.mrxu.netty.filter.AbstractFilterContext;

import java.util.List;

public interface FilterPipeLine {

	void addLastSegment(Filter... filters);

	AbstractFilterContext get(String name);

	List<Filter> getAllFilter();

}
