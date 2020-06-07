package com.automation.ibinstallationteam.entity;

/**
 * Created by pengchenghu on 2019/2/24.
 * Author Email: 15651851181@163.com
 * Describe: 开关变量类
 */
public class VarSwitch {
    private String name;  // 功能名称
    private int imageId;  // 功能示意图
    private int state; // 开关状态(0: 安全， 1：提醒)

    public VarSwitch(){
        this.state=-1;
    }
    public VarSwitch(String name, int imageId, int state){
        this.name = name;
        this.imageId = imageId;
        this.state = state;
    }

    public String getName(){
        return name;
    }

    public int getImageId(){
        return imageId;
    }

    public  int getState(){
        return state;
    }

    public void setState(int state){
        this.state = state;
    }
}
