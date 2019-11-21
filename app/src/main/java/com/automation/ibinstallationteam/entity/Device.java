package com.automation.ibinstallationteam.entity;

/**
 * Created by pengchenghu on 2019/11/21.
 * Author Email: 15651851181@163.com
 * Describe: 设备类-> 在设备绑定活动中使用
 * */
public class Device {
    private String name;  // 功能名称
    private int imageId;  // 功能示意图
    private String number;  // 序列号

    /* 构造函数
    * */
    public Device(){}
    public Device(String name, int imageId){
        this.name = name;
        this.imageId = imageId;
    }
    public Device(String name, int imageId, String number){
        this.name = name;
        this.imageId = imageId;
        this.number = number;
    }

    /* Bean
    * */

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
