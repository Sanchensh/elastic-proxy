package com.mrxu.netty.prop;

public interface PropertyProcessor {

	String NAME_PROXY_APPLICATION_RESOURCES = "properties/proxy-application.properties";
	
	String getString(String key, String def);
	
	int getInteger(String key, int def);
	
	boolean getBoolean(String key, boolean def);
}