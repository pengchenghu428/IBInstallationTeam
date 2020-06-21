package com.automation.ibinstallationteam.entity;

import com.automation.ibinstallationteam.enmu.BasketState;

/**
 * Created by pengchenghu on 2019/11/19.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class Basket {

    /* 成员变量
     */
    private String projectId;  // 项目编号
    private String id;  // 电柜编号
    private String siteId; // 现场编号
    private boolean workerInfo;  // 工人信息
    private boolean finishImg;  // 完工图片
    private boolean deviceBound;  // 设备绑定
    private int basketState; // 吊篮状态

    /* 构造函数
     */
    public Basket(){}

    public Basket(String id, boolean workerInfo, boolean finishImg, boolean deviceBound){
        this.id = id;
        this.workerInfo = workerInfo;
        this.finishImg = finishImg;
        this.deviceBound = deviceBound;
    }

    public Basket(String id, boolean workerInfo, boolean finishImg, boolean deviceBound, int basketState){
        this.id = id;
        this.workerInfo = workerInfo;
        this.finishImg = finishImg;
        this.deviceBound = deviceBound;
        this.basketState = basketState;
    }

    /* Bean函数
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId){
        this.siteId = siteId;
    }

    public boolean isWorkerInfo() {
        return workerInfo;
    }

    public void setWorkerInfo(boolean workerInfo) {
        this.workerInfo = workerInfo;
    }
    public void setWorkerInfo(int workerInfo) {
        if(workerInfo==0)
            this.workerInfo=false;
        else
            this.workerInfo=true;
    }

    public boolean isFinishImg() {
        return finishImg;
    }

    public void setFinishImg(boolean finishImg) {
        this.finishImg = finishImg;
    }
    public void setFinishImg(int finishImg) {
        if(finishImg == 0)
            this.finishImg = false;
        else
            this.finishImg = true;
    }

    public boolean isDeviceBound() {
        return deviceBound;
    }

    public void setDeviceBound(boolean deviceBound) {
        this.deviceBound = deviceBound;
    }
    public void setDeviceBound(int deviceBound) {
        if(deviceBound==0) this.deviceBound=false;
        else this.deviceBound=true;
    }

    public String getProjectId(){
        return projectId;
    }
    public void setProjectId(String projectId){
        this.projectId = projectId;
    }

    public int getBasketState() {
        return basketState;
    }

    public void setBasketState(int basketState) {
        this.basketState = basketState;
    }
}
