package com.dgmall.realtime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dgmall.util.JedisUtil;
import com.dgmall.util.PropertiesUtil;
import javafx.scene.input.DataFormat;
import kafka.serializer.StringDecoder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.dgmall.bean.StartupLog;
import org.apache.spark.api.java.function.Function;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import redis.clients.jedis.Jedis;
import scala.Tuple2;
import org.apache.commons.collections.IteratorUtils;

public class DAURealtime {
    public static void main(String[] args) throws InterruptedException {

        SparkConf sparkConf = new SparkConf().setAppName("dgmall_dau").setMaster("local[2]");

        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(3));

        HashMap params = new HashMap();
        params.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,PropertiesUtil.getProperties("config.properties","kafka.broker.list"));
        params.put(ConsumerConfig.GROUP_ID_CONFIG,PropertiesUtil.getProperties("config.properties","kafka.group.id"));
        params.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.IntegerDeserializer");
        params.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        params.put("auto.offset.reset","earliest");
        HashSet<String> topicSet = new HashSet<>();
        topicSet.add("topic_startup");

        JavaInputDStream<ConsumerRecord<String, String>> directStream = KafkaUtils.createDirectStream(jssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topicSet, params)
        );
        
        //日活
        //1封装数据
        /*directStream.map(new org.apache.spark.api.java.function.Function() {
            @Override
            public Object call(Object v1) throws Exception {
                
                return null;
            }
        });*/
        JavaDStream<StartupLog> startupLogStream = directStream.map(rdd -> {
            //{"logType":"startup","area":"shanghai","uid":"7799",
            // "os":"android","appId":"gmall","channel":"wandoujia","mid":"mid_33","version":"1.2.0","ts":"1574825071158"}
            DateTimeFormatter dFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String value = rdd.value();
//            JSON.parseObject(value,StartupLog.class);
            JSONObject startupJson = JSON.parseObject(value);
            String logType = startupJson.getString("logType");
            String area = startupJson.getString("area");
            String uid = startupJson.getString("uid");
            String os = startupJson.getString("os");
            String appId = startupJson.getString("appId");
            String channel = startupJson.getString("channel");
            String mid = startupJson.getString("version");
            String ts = startupJson.getString("ts");
            String dt = dFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(ts)), ZoneId.systemDefault()));
//            dFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(ts)), ZoneId.systemDefault()));
            return new StartupLog(logType, area, uid, os, appId, channel, mid, dt);
        });
        
       /* startupLogStream.foreachRDD(rdd->{
//            System.out.println(rdd.toString());
            rdd.foreach(item->{
                System.out.println(item);
            });
        });*/
        
        //redis去重
        /*startupLogStream.foreachRDD(rdd->{
            
                rdd.foreachPartition(partition-> {
//            Jedis connect = JedisUtil.getConnect();
                    //redis中的uid的set
//            Set<String> uidSet = connect.smembers("topic_startup:" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(new Date().toInstant()));
                    while (partition.hasNext()) {
//                if (partition.)
                        System.out.println(partition);
                    }
                    
                } );
                
        });*/
        
        /*startupLogStream.mapPartitions(partition->{
            Jedis connect = JedisUtil.getConnect();
            Set<String> uidSet = connect.smembers("topic_startup:" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(new Date().toInstant()));
            
            while (partition.hasNext()) {
                String uid = partition.next().getUid();
                if ()
            }
        })
        
        startupLogStream.mapPartitions(new FlatMapFunction<Iterator<StartupLog>, StartupLog>() {
            @Override
            public Iterator<StartupLog> call(Iterator<StartupLog> startupLogIterator) throws Exception {
                
                return null;
            }
        })*/

        JavaDStream<StartupLog> filtedStream = startupLogStream.transform(rdd -> {
            Jedis connect = JedisUtil.getConnect();
            long timeMillis = System.currentTimeMillis();
            Set<String> uidSet =
                    connect.smembers("topic_startup:" +
//                            DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.ofInstant(Instant.ofEpochMilli(lastTime), ZoneId.systemDefault())));
                            DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneId.systemDefault())));
            connect.close();
            Broadcast<Set<String>> broadcastUidSet = jssc.sparkContext().broadcast(uidSet);
            rdd.filter(item -> !broadcastUidSet.value().contains(item.getUid()));
            return rdd;
        });
        
       

        
        
        /*filtedStream.foreachRDD(rdd->{
            rdd.foreachPartition(partition->{
                while (partition.hasNext()) {
                    System.out.println(partition.next());
                }
            });
        });*/
        


        //将id存入redis 批次内去重
        JavaDStream<StartupLog> filtedS = filtedStream.mapToPair(item -> new Tuple2<>(item.getUid(), item))
                .groupByKey()
                .map(item -> {
                    List<StartupLog> list = IteratorUtils.toList(item._2.iterator());
//                    list.sort(Comparator.comparingLong(StartupLog::getTs));
                    return list.get(0);
                });
        
        filtedS.foreachRDD(rdd->{
            rdd.foreachPartition(partition->{
                Jedis connect = JedisUtil.getConnect();
                
                while (partition.hasNext()) {
                    StartupLog log = partition.next();
                    connect.sadd("topic_startup:" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault())),log.getUid());
                }
                connect.close();
            });
        });
                
                /*.map(item -> {
                    Optional min = IteratorUtils.toList((Iterator) item._2).stream().min(new Comparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            StartupLog o11 = (StartupLog) o1;
                            StartupLog o12 = (StartupLog) o2;
                            if (Long.parseLong(o11.getTs()) > Long.parseLong(o12.getTs())) {
                                return 1;
                            }
                            return 0;
                        }
                    });
                    return min;
                });*/


        //存入hbase
        
        
        
        jssc.start();
        jssc.awaitTermination();


    }
}
