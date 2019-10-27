package com.automation.ibinstallationteam.utils.okhttp;

/**
 * Created by pengchenghu on 2019/2/28.
 * Author Email: 15651851181@163.com
 * Describe: 参数类
 */
public class RequestParameter {
    private String key;
    private Object obj;

    public RequestParameter(String key, Object obj) {
        this.key = key;
        this.obj = obj;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
