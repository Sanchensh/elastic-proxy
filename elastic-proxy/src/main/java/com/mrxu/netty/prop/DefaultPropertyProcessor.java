package com.mrxu.netty.prop;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPropertyProcessor extends AbstractPropertyProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertyProcessor.class);

    protected String keyPrefix = "";

    protected DefaultPropertyProcessor() {
        keyPrefix = getKeyPrifix();
        logger.info("Use configure property processor key prefix {}", keyPrefix);
    }

    protected String getValue(String key) {
        Objects.requireNonNull(key);
        String val = getSystem(key);
        if (null != val) {
            return val;
        }
        val = getEnv(key);
        if (null != val) {
            return val;
        }

        return getProperty(key);
    }

    private static String getKeyPrifix() {
        return props.getProperty(ProxyBasicProperties.NAME_PROCESSOR_PROPERTY_KEY_PREFIX, "");
    }

    protected String transform(String key) {
        return keyPrefix + key.replace('.', '_').toUpperCase();
    }

    protected String getEnv(String key) {
        String val = System.getenv(key);
        if (val != null) {
            return val;
        }
        return System.getenv(transform(key));
    }

    protected String getSystem(String key) {
        String val = System.getProperty(key);
        if (val != null) {
            return val;
        }
        return System.getProperty(transform(key));
    }

    protected String getProperty(String key) {
        return props.getProperty(key);
    }
}