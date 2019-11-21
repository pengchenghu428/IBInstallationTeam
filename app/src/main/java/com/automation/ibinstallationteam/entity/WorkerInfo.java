package com.automation.ibinstallationteam.entity;

/**
 * Created by pengchenghu on 2019/11/21.
 * Author Email: 15651851181@163.com
 * Describe: 施工队伍信息
 */
public class WorkerInfo {

    private String id;
    private String name;
    private String phoneNumber;
    private String idCardNumber;

    public WorkerInfo(){}
    public WorkerInfo(String name, String phoneNumber, String idCardNumber){
        this.id =id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.idCardNumber = idCardNumber;
    }
    public WorkerInfo(String id, String name, String phoneNumber, String idCardNumber){
        this.id =id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.idCardNumber = idCardNumber;
    }

    /* Bean
    * */

    public String getId() {
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }
}
