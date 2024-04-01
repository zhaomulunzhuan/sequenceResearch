package com.chen;

import java.io.*;
import java.util.Properties;
import java.net.URISyntaxException;

public class ConfigReader {
    private static final String CONFIG_FILE_PATH = "/config.properties";
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream inputStream = ConfigReader.class.getResourceAsStream(CONFIG_FILE_PATH)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                System.err.println("无法找到配置文件");
            }
        } catch (IOException e) {
            System.err.println("无法读取配置文件: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static void addProperty(String key, String value) {
        properties.setProperty(key, value);
        savePropertiesToFile();
    }

    private static void savePropertiesToFile() {
        String configFileAbsolutePath = ConfigReader.class.getResource(CONFIG_FILE_PATH).getPath();
//        System.out.println(configFileAbsolutePath);
///D:/Code/Idea_Codes/sequenceResearch/target/classes/config.properties
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFileAbsolutePath))) {
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                writer.write(key + "=" + value);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("无法保存配置文件: " + e.getMessage());
        }
    }

    public static void saveConfigFile() throws IOException {
        // 构造目标文件路径
        String configFile=ConfigReader.getProperty("project-root-directory")+CONFIG_FILE_PATH;

        // 读取配置文件内容
        try (InputStream inputStream = ConfigReader.class.getResourceAsStream(CONFIG_FILE_PATH)) {
            // 创建输出流
            try (OutputStream outputStream = new FileOutputStream(configFile)) {
                // 将输入流中的内容复制到目标文件中
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    public static void loadProperties(String configFile) {
        properties = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                System.err.println("无法找到配置文件");
            }
        } catch (IOException e) {
            System.err.println("无法读取配置文件: " + e.getMessage());
        }
    }
}
