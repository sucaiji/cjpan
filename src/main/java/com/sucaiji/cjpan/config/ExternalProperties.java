package com.sucaiji.cjpan.config;

import org.apache.shiro.crypto.AesCipherService;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.*;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 外部配置文件操作类
 */
public class ExternalProperties {

    private static final String CIPHER_KEY = "config.cipherKey";

    /**
     * 获取加密rememberMe cookies的密钥
     */
    public static String getRememberMeCipherKey() {
        return getPropertyByKey(CIPHER_KEY);
    }

    /**
     * 初始化外部配置文件
     */
    public static void initExternalProperties() {

        File externalProperties = new File(Property.EXTERNAL_PROPERTIES);
        if (externalProperties.exists()) {
            //TODO  目前初始化如果文件在的话就return  之后改成如果文件在的话，遍历一遍把缺失的属性补齐填默认值
            return;
        }

        Map<String, String> map = new HashMap<>();

        //生成rememberme 密钥
        AesCipherService aesCipherService = new AesCipherService();
        aesCipherService.setKeySize(128);
        String key = new String(Base64.encodeBase64(aesCipherService.generateNewKey().getEncoded()));
        map.put(CIPHER_KEY, key);

        setProperties(map);
    }







    private static void setProperty(String key, String value) {
        String externalPath = Property.EXTERNAL_PROPERTIES;
        try (FileInputStream fileInputStream = new FileInputStream(externalPath);
             FileOutputStream fileOutputStream = new FileOutputStream(externalPath); ) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            properties.setProperty(key, value);
            //将Properties写入输出流
            properties.store(fileOutputStream, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static void setProperties(Map<String, String> map) {
        File externalProperties = new File(Property.EXTERNAL_PROPERTIES);
        if (!externalProperties.exists()) {
            externalProperties.getParentFile().mkdirs();
            try {
                externalProperties.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileInputStream fileInputStream = new FileInputStream(externalProperties);
             FileOutputStream fileOutputStream = new FileOutputStream(externalProperties); ) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            for (Map.Entry<String, String> entry: map.entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }
            //将Properties写入输出流
            properties.store(fileOutputStream, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPropertyByKey(String key) {
        File externalProperties = new File(Property.EXTERNAL_PROPERTIES);
        if (!externalProperties.exists()) {
            initExternalProperties();
        }
        try (FileInputStream fileInputStream = new FileInputStream(externalProperties.getAbsoluteFile());) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties.getProperty(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
