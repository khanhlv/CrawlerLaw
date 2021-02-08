package com.crawler.law.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ResourceUtil {

    static Properties properties = new Properties();

    static {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");

        if (resourceAsStream == null) {
            throw new IllegalArgumentException("Config file config.properties not found in classpath");
        } else {
            try {
                properties.load(resourceAsStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String getValue(String key) {
        return properties.getProperty(key);
    }
}
