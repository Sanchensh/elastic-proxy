package com.mrxu.netty.filter;

import com.mrxu.exception.CustomException;
import com.mrxu.netty.pojo.SessionContext;
import com.mrxu.netty.trait.Filter;

public abstract class AbstractFilter implements Filter {

	public final static String PRE_FILTER_NAME = "PROXY_FILTER_";
	protected boolean valid = true;

	@Override
	public void init() {
		valid = true;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws CustomException {
		filterContext.fireNext(sessionContext);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean isValid) {
		this.valid = isValid;
	}

	@Override
	public void shutdown() {
	}
	
}
