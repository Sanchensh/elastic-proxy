package com.mrxu.netty.filter;


import com.mrxu.netty.trait.Filter;

public class FilterContext extends AbstractFilterContext {
	private Filter filter;

	public FilterContext(Filter filter) {
		this.filter = filter;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}
}
