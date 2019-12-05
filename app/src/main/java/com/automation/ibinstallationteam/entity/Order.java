package com.automation.ibinstallationteam.entity;

/**
 * Created by pengchenghu on 2019/11/19.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class Order {

    // 成员变量
    private String id; // 工单id
    private String name;  // 工单名称
    private int finishNum;  // 完成的数目
    private int totalNum;  // 吊篮总数
    private String completedTime;  // 开始时间

    /*
     * 构造函数
     */
    public Order(){
    }

    public Order(String name, int finishNum, int totalNum, String completedTime){
        this.name = name;
        this.finishNum = finishNum;
        this.totalNum = totalNum;
        this.completedTime = completedTime;
    }

    /*
     * Bean 函数
     */
    public String getId(){
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFinishNum() {
        return finishNum;
    }

    public void setFinishNum(int finishNum) {
        this.finishNum = finishNum;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public String getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(String completedTime) {
        this.completedTime = completedTime;
    }
}
