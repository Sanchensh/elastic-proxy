package com.mrxu.netty.property;


import com.mrxu.netty.util.BeanUtils;

public class PropertiesUtil {
    public static ProxyProperties properties = BeanUtils.getBean("ProxyProperties", ProxyProperties.class);
}
