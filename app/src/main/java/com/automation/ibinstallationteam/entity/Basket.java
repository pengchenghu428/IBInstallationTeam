package com.automation.ibinstallationteam.entity;

/**
 * Created by pengchenghu on 2019/11/19.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class Basket {

    /* 成员变量
     */
    private String id;  // 电柜编号
    private boolean workerInfo;  // 工人信息
    private boolean finishImg;  // 完工图片
    private boolean deviceBound;  // 设备绑定

    /* 构造函数
     */
    public Basket(){}

    public Basket(String id, boolean workerInfo, boolean finishImg, boolean deviceBound){
        this.id = id;
        this.workerInfo = workerInfo;
        this.finishImg = finishImg;
        this.deviceBound = deviceBound;
    }

    /* Bean函数
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isWorkerInfo() {
        return workerInfo;
    }

    public void setWorkerInfo(boolean workerInfo) {
        this.workerInfo = workerInfo;
    }

    public boolean isFinishImg() {
        return finishImg;
    }

    public void setFinishImg(boolean finishImg) {
        this.finishImg = finishImg;
    }

    public boolean isDeviceBound() {
        return deviceBound;
    }

    public void setDeviceBound(boolean deviceBound) {
        this.deviceBound = deviceBound;
    }
}
