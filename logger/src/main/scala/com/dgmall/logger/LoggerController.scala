package com.dgmall.logger

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation._
import com.alibaba.fastjson.JSON
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate



//@Controller
@RestController  //@Controller + @ResponseBody
class LoggerController {

  @PostMapping(Array("/log"))
//  @ResponseBody
  def dolog(@RequestParam("log") log:String) = {
//    println(log)
    //1.添加ts
    val logWithTs = addTs(log)
//    println(logWithTs)
    //2.落盘
    saveLog2File(logWithTs)
    
    
    //3.写入kafka
    send2kafka(logWithTs)
    
    
    "ok"
    
  }
  @Autowired
  var template:KafkaTemplate[String,String]=_
  
  def send2kafka(logWithTs:String)={
    //1.生产者
    if ("startup".equals(JSON.parseObject(logWithTs).get("logType"))){
      template.send("topic_startup",logWithTs)
    } else template.send("topic_event",logWithTs)
    //2.写出去
    
    
  }
  
  private val logger: Logger = LoggerFactory.getLogger(classOf[LoggerController])
  def saveLog2File(logWithTs:String)={
    logger.info(logWithTs)
    
  }

  /**
    * 加时间戳
    * @param log
    * @return
    */
  def addTs(log:String)={
    val jSONObject = JSON.parseObject(log)
    jSONObject.put("ts", System.currentTimeMillis()+"")
    jSONObject.toString
  }
}
