package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fileInputStream = new FileInputStream("./src/test/resources/config/config.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            System.err.println("Unable to load properties file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getDbURL() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("dbURL"));
    }

    public static String getDbUsername() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("dbUsername"));
    }

    public static String getDbPassword() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("dbPassword"));
    }

    public static String getCustomDestinationUsername() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("customDestinationUserName"));
    }

    public static String getCustomDestinationPassword() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("customDestinationPassword"));
    }

    public static String getInternalDemoUsername() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("demoUser"));
    }

    public static String getInternalDemoPassword() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("demoPassword"));
    }

    public static String getInternalPreReleaseUsername() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("preReleaseUser"));
    }

    public static String getInternalPreReleasePassword() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("preReleasePassword"));
    }

    public static String getExternalDemoUsername() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("demoExternalUser"));
    }

    public static String getExternalDemoPassword() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("demoExternalPassword"));
    }

    public static String getExternalPreReleaseUsername() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("preReleaseExternalUser"));
    }

    public static String getExternalPreReleasePassword() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("preReleaseExternalPassword"));
    }

    public static String getP2McpAgentApiKey() throws Exception {
        return EncryptionDecryption.decrypt(getProperty("p2McpAgentApiKey"));
    }
}
