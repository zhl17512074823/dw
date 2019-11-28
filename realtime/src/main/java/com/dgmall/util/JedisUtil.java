package com.dgmall.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtil {
    
    private static String host = PropertiesUtil.getProperties("config.properties","redis.host");
    private static String port = PropertiesUtil.getProperties("config.properties","redis.port");
    private static JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    private static JedisPool jedisPool;
    static {
        //最大连接数
        jedisPoolConfig.setMaxTotal(100);
        //最大空闲
        jedisPoolConfig.setMaxIdle(100);
        //最小空闲
        jedisPoolConfig.setMinIdle(20);
        //忙碌时是否等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        //忙碌时等待时长
        jedisPoolConfig.setMaxWaitMillis(500);
        //每次获得连接的进行测试
        jedisPoolConfig.setTestOnBorrow(false);
        
        jedisPool= new JedisPool(jedisPoolConfig,host,Integer.parseInt(port));
    }
    
    public static Jedis getConnect(){
        return jedisPool.getResource();
    }
}
