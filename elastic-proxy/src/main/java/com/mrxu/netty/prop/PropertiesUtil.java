package com.mrxu.netty.prop;


import com.mrxu.netty.util.BeanUtils;

public class PropertiesUtil {
    public static ProxyProperties properties = BeanUtils.getBean("ProxyProperties", ProxyProperties.class);
}
