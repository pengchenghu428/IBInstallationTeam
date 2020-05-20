package com.automation.ibinstallationteam.enmu;

/**
 * Created by pengchenghu on 2020/5/20.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public enum BasketState {
    INSTALLING("待安装", 1),
    INSTALL("正在安装", 11),
    INSTALL_APPLY("安装审核中", 12),
    ACCEPTANCE("待上传安检证书", 2),
    CERT_APPLY("安检证书审核", 21),
    ONGOING("使用中", 3),
    STAND_BY("待报停", 4),
    STAND_BY_ACCEPTANCE("报停审核", 5);

    // 成员变量
    private String name;
    private int index;
    // 构造方法
    private BasketState(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (BasketState c : BasketState.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }
    // get set 方法
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
