package com.dgmall.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {

    //    new HashMap<>();
    static Map<String, Properties> map = new HashMap<>();
    
    public static String getProperties(String file, String key) {

        Properties props = new Properties();
        try {
            if (map.isEmpty()){
    
                InputStream is = ClassLoader.getSystemResourceAsStream(file);
                 props = new Properties();
                 props.load(is);
                 map.put(file,props);
            }else {
                props=map.get(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("配置文件"+file+"加载错误");
        }

        return props.getProperty(key);
    }
}
