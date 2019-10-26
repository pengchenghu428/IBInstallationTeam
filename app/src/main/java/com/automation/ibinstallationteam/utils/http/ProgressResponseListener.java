package com.automation.ibinstallationteam.utils.http;

/**
 * Created by pengchenghu on 2019/4/11.
 * Author Email: 15651851181@163.com
 * Describe: 响应体进度回调接口，用于文件下载进度回调
 */
public interface ProgressResponseListener {
    void onResponseProgress(long bytesRead, long contentLength, boolean done);
}
