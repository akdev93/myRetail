package com.myRetail.product.resources;

import java.io.IOException;
import java.util.Properties;


public class TestConfig {
    private static TestConfig testConfig = null;
    private TestConfig(Properties p ){
        this.properties = p;
    }

    private Properties properties = null;

    public static TestConfig getInstance() {
        if(testConfig == null) {
            Properties p = new Properties();
            try {
                p.load(String.class.getResourceAsStream("/test.properties"));
            } catch (IOException ioE) {
                ioE.printStackTrace();
                throw new RuntimeException("Tests can't run. Cannot load config ", ioE);
            }
            testConfig = new TestConfig(p);
        }
        return testConfig;
    }

    public Properties getProperties() {
        return properties;
    }
}
