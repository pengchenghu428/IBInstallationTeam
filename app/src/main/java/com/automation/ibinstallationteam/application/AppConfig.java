package com.automation.ibinstallationteam.application;

public class AppConfig {
    /*
     * 服务器地址
     */
    public final static String ANDROID_URL_PATH = "http://47.100.1.211";  // 阿里云
//    public final static String ANDROID_URL_PATH = "http://10.193.233.106:8080"; // 老刘
    public final static String FILE_SERVER_YBLIU_IP = "47.100.1.211";  // 刘跃博FTP文件服务器
    public final static int FILE_SERVER_YBLIU_PORT = 21;  // 刘跃博FTP文件服务器的端口
    public final static String FILE_SERVER_YBLIU_PATH = "http://47.100.1.211:8082"; // 刘跃博HTTP文件服务器地址
    public final static String COMMUNICATION_SERVER_PATH = "http://47.100.1.211:8081"; // 通讯服务器地址
    public final static String VIDEO_STREAM_PATH = "rtmp://47.96.103.244:1935"; // 流媒体服务器地址

    /*
     * FTP账户和密码
     */
    public final static String FILE_SERVER_USERNAME = "root";
    public final static String FILE_SERVER_PASSWORD = "nishipig2/";

    /*
     * 用户信息
     */
    // 登录
    public final static String LOGIN_USER = ANDROID_URL_PATH.concat("/login");
    // 注册
    public final static String REGISTER_USER = ANDROID_URL_PATH.concat("/checkRegister");
    // 获取用于基本信息
    public final static String USER_INFO = ANDROID_URL_PATH.concat("/getUserInfo");
}