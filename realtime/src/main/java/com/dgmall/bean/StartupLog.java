package com.dgmall.bean;


//import scala.Serializable;

import java.io.Serializable;

public class StartupLog implements Serializable {
    
    private String logType;
    private String area;
    private String uid;
    private String os;
    private String appId;
    private String channel;
    private String mid;
    private String ts;

    @Override
    public String toString() {
        return "StartupLog{" +
                "logType='" + logType + '\'' +
                ", area='" + area + '\'' +
                ", uid='" + uid + '\'' +
                ", os='" + os + '\'' +
                ", appId='" + appId + '\'' +
                ", channel='" + channel + '\'' +
                ", mid='" + mid + '\'' +
                ", ts='" + ts + '\'' +
                '}';
    }

    public StartupLog(String logType, String area, String uid, String os, String appId, String channel, String mid, String ts) {
        this.logType = logType;
        this.area = area;
        this.uid = uid;
        this.os = os;
        this.appId = appId;
        this.channel = channel;
        this.mid = mid;
        this.ts = ts;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getLogType() {
        return logType;
    }

    public String getArea() {
        return area;
    }

    public String getUid() {
        return uid;
    }

    public String getOs() {
        return os;
    }

    public String getAppId() {
        return appId;
    }

    public String getChannel() {
        return channel;
    }

    public String getMid() {
        return mid;
    }

    public String getTs() {
        return ts;
    }
}
