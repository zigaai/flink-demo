package com.zigaai.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public final class PropertiesConstant {

    public static Properties getFlinkProperties() {
        return INNER.INSTANCE.FLINK_PROPERTIES;
    }

    public static Properties getConfigProperties() {
        return INNER.INSTANCE.CONFIG_PROPERTIES;
    }

    public static Properties getDebeziumProperties() {
        return INNER.INSTANCE.DEBEZIUM_PROPERTIES;
    }

    private PropertiesConstant() {
    }

    private enum INNER {
        INSTANCE;

        private final Properties FLINK_PROPERTIES = new Properties();

        private final Properties CONFIG_PROPERTIES = new Properties();

        private final Properties DEBEZIUM_PROPERTIES = new Properties();

        INNER() {
            String flinkPropertiesPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "flink.properties";
            String configPropertiesPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "config.properties";
            String debeziumPropertiesPath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "debezium.properties";
            try (BufferedReader flinkReader = new BufferedReader(new FileReader(flinkPropertiesPath));
                 BufferedReader configReader = new BufferedReader(new FileReader(configPropertiesPath));
                 BufferedReader debeziumReader = new BufferedReader(new FileReader(debeziumPropertiesPath))) {
                FLINK_PROPERTIES.load(flinkReader);
                CONFIG_PROPERTIES.load(configReader);
                DEBEZIUM_PROPERTIES.load(debeziumReader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
