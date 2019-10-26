package com.automation.ibinstallationteam.utils.http;

/**
 * Created by pengchenghu on 2019/4/11.
 * Author Email: 15651851181@163.com
 * Describe: 请求体进度回调接口，用于文件上传进度回调
 */
public interface ProgressRequestListener {
    void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}

