package com.automation.ibinstallationteam.utils.okhttp;

/**
 * Created by pengchenghu on 2019/2/28.
 * Author Email: 15651851181@163.com
 * Describe: 拦截器接口
 */
public interface ProgressListener {
    /**
     * 显示进度
     *
     * @param mProgress
     */
    public void onProgress(int mProgress, long contentSize);

    /**
     * 完成状态
     *
     * @param totalSize
     */
    public void onDone(long totalSize);
}
