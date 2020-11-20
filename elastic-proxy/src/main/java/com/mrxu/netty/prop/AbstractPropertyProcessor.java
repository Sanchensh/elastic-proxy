package com.mrxu.netty.prop;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ServiceConfigurationError;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Resources;

@Slf4j
public abstract class AbstractPropertyProcessor implements PropertyProcessor {

    protected static Properties props;

    protected AbstractPropertyProcessor() {
    }

    ;

    static {
        props = getProperties(NAME_PROXY_APPLICATION_RESOURCES);
    }

    private static Properties getProperties(String resource) {
        Properties properties = new Properties();
        try {
            ClassLoader cl = AbstractPropertyProcessor.class.getClassLoader();
            Enumeration<URL> urls = cl.getResources(resource);
            List<URL> urlList = Collections.list(urls);
            Collections.reverse(urlList);
            for (URL url : urlList) {
                log.info("Get Properties: url=" + url);
                byte[] bytes = Resources.toByteArray(url);
                properties.load(new ByteArrayInputStream(bytes));
            }
        } catch (Exception e) {
            log.error("Error load: " + resource, e);
        }
        return properties;
    }

    private static class PropertyProcessorHolder {
        private static PropertyProcessor processor = initProcessor();

        private static PropertyProcessor initProcessor() {
            String propertyProcessorClassName = props.getProperty(ProxyBasicProperties.NAME_PROCESSOR_PROPERTY);
            if (StringUtils.isEmpty(propertyProcessorClassName)) {
                propertyProcessorClassName = DefaultPropertyProcessor.class.getName();
                log.info("Couldn't found configure property by key 'pallas.processor.property' Use Default {}", propertyProcessorClassName);
            }
            try {
                Class<?> PropertyProcessorClass = Class.forName(propertyProcessorClassName);
                Constructor<?> constructor = PropertyProcessorClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                processor = (PropertyProcessor) constructor.newInstance();
            } catch (Exception e) {
                log.error(e.toString());
                throw new ServiceConfigurationError("Provider " + propertyProcessorClassName + " could not be instantiated :" + e);
            }
            log.info("Use configure property processor {}", propertyProcessorClassName);
            return processor;
        }
    }

    public static PropertyProcessor getProcessor() {
        return PropertyProcessorHolder.processor;
    }

    protected abstract String getValue(String key);

    @Override
    public String getString(String key, String def) {
        String val = getValue(key);
        return null != val ? val : def;
    }

    @Override
    public int getInteger(String key, int def) {
        String val = getValue(key);
        return null != val ? Integer.parseInt(val) : def;
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        String val = getValue(key);
        return null != val ? Boolean.parseBoolean(val) : def;
    }

}