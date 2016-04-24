package net.ak.myRetailBlackBoxTests;

import java.io.IOException;
import java.util.Properties;


public class TestConfig {

    private static TestConfig testConfig ;

    private Properties properties;
    private TestConfig(Properties properties) {
        this.properties = properties;
    }

    public static TestConfig getInstance() {
        if(testConfig==null) {
            Properties properties = null;
            try {
                properties = new Properties();
                properties.load(String.class.getResourceAsStream("/blackboxtest.properties"));
            }
            catch(IOException ioE) {
                ioE.printStackTrace();
                throw new RuntimeException("Could not load configuration to run tests ", ioE);
            }
            testConfig = new TestConfig(properties);
        }
        return testConfig;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public String getTestUrl() {
        return properties.getProperty("ServiceURL");
    }
}
