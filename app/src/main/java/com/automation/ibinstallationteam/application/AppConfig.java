package com.automation.ibinstallationteam.application;

public class AppConfig {
    /*
     * 服务器地址
     */
    public final static String ANDROID_URL_PATH = "http://47.100.1.211";  // 阿里云
//    public final static String ANDROID_URL_PATH = "http://10.193.19.93:8080"; // 老刘
    public final static String FILE_SERVER_YBLIU_IP = "47.100.1.211";  // 刘跃博FTP文件服务器
    public final static int FILE_SERVER_YBLIU_PORT = 21;  // 刘跃博FTP文件服务器的端口
    public final static String FILE_SERVER_YBLIU_PATH = "http://47.100.1.211:8082/smartNacelle/"; // 刘跃博HTTP文件服务器地址
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
    // 获取基本信息
    public static final String GET_USER_ALL_INFO = ANDROID_URL_PATH.concat("/androidGetUserInfo");

    /*
     * 项目相关
     */
    // 获取项目详情
    public static final String GET_PROJECT_INFO = ANDROID_URL_PATH.concat("/projectDetailInfo");
    public static final String GET_BASKET_LIST_INFO = ANDROID_URL_PATH.concat("/getBasketList");

    // 安装队伍获取项目信息
    public static final String GET_PROJECT_BY_INSTALLER = ANDROID_URL_PATH.concat("/getProjectByInstaller");
    public static final String GET_INSTALLER = ANDROID_URL_PATH.concat("/getInstaller");
    public static final String CREATE_INSTALLER = ANDROID_URL_PATH.concat("/createInstaller");
    public static final String UPDATE_INSTALLER = ANDROID_URL_PATH.concat("/updateInstaller");
    public static final String DELETE_INSTALLER = ANDROID_URL_PATH.concat("/deleteInstaller");
    // 绑定设备
    public static final String GET_ELECTRIC_BOX_CONFIG = ANDROID_URL_PATH.concat("/getElectricBoxConfig");
    public static final String CREATE_ELECTRIC_BOX_CONFIG = ANDROID_URL_PATH.concat("/createElectricBoxConfig");
    public static final String DELETE_ELECTRIC_BOX_CONFIG = ANDROID_URL_PATH.concat("/deleteElectricBoxConfig");
    // 设备状态更新
    public static final String UPDATE_INSTALLER_STATE = ANDROID_URL_PATH.concat("/updateInstallState");

    /*
     * 吊篮请求
     */
    // 获取吊篮实施参数
    public static final String REAL_TIME_PARAMETER = ANDROID_URL_PATH.concat("/getRealTimeData");
}