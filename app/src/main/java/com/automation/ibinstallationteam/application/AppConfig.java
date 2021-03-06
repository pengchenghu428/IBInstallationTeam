package com.automation.ibinstallationteam.application;

public class AppConfig {
    /*
     * 服务器地址
     */
    public final static String ANDROID_URL_PATH = "http://39.98.115.183";  // 阿里云
//    public final static String ANDROID_URL_PATH = "http://47.100.1.211";  // 阿里云
//    public final static String ANDROID_URL_PATH = "http://10.193.19.93:8080"; // SEU

    public final static String FILE_SERVER_YBLIU_IP = "39.99.158.73";  // 阿里云FTP文件服务器
//    public final static String FILE_SERVER_YBLIU_IP = "47.100.1.211";  // 阿里云FTP文件服务器
    public final static int FILE_SERVER_YBLIU_PORT = 21;  // 刘跃博FTP文件服务器的端口

    public final static String FILE_SERVER_YBLIU_PATH = "http://39.99.158.73:8082/var/ftp/smartNacelle/"; // 阿里云服务器地址
//    public final static String FILE_SERVER_YBLIU_PATH = "http://47.100.1.211:8082/smartNacelle/"; // 阿里云文件服务器地址

    public final static String COMMUNICATION_SERVER_PATH = "http://39.98.115.183:8081"; // 通讯服务器地址
//    public final static String COMMUNICATION_SERVER_PATH = "http://47.100.1.211:8081"; // 通讯服务器地址
//    public final static String VIDEO_STREAM_PATH = "rtmp://47.96.103.244:1935"; // 流媒体服务器地址

    /*
     * FTP账户和密码
     */
    public final static String FILE_SERVER_USERNAME = "root";
    public final static String FILE_SERVER_PASSWORD = "BASServer2020";
//    public final static String FILE_SERVER_USERNAME = "root";
//    public final static String FILE_SERVER_PASSWORD = "nishipig2/";

    /*
     * 萤石云：appkey
     */
    public final static String EZUIKit_APPKEY = "6747c45b0baf43868d88e34748c742e7";
    public final static String EZUIKit_SECRET = "3b3d8db1a048dd9e197711b37ecb6c42";
    public final static String EZUIKit_AccessToken = "at.dq2en1fi9mpkqqzrdaqjhk392c7es89x-2h6a9c2x9j-1ry029z-abzmupcyx";

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
    // 更新现场编号
    public static final String UPDATE_SITE_ID = ANDROID_URL_PATH.concat("/updateSiteNo");

    /*
     * 吊篮请求
     */
    // 获取吊篮实施参数
    public static final String REAL_TIME_PARAMETER = ANDROID_URL_PATH.concat("/getRealTimeData");

    /* 获取流媒体视频
     */
    // 获取吊篮设备序列号
    public static final String GET_ELECTRICBOX_CONFIG = ANDROID_URL_PATH.concat("/getElectricBoxConfig");
    // 视频播放器窗口纵横比
    public static final float ASPECT_RATIO = (float)1.7777777777777777;  // 16:9
    // 视频纵横比
    public static final float ASPECT_RATIO_VIDEO = (float)1.333333333333;  // 4:3
    //视频推流地址+电柜参数设置
    public static final String HANGING_BASKET_VIDEO = COMMUNICATION_SERVER_PATH.concat("/sendToDevice");
    /* 萤石云
     */
    public static final String GET_UIKIT_ACCESS_TOKEN = "https://open.ys7.com/api/lapp/token/get";
    public static final String GET_UIKIT_VIDEO_URL = "https://open.ys7.com/api/lapp/live/video/list";
    public static final String GET_UIKIT_DEVICE_INFO = "https://open.ys7.com/api/lapp/live/address/get";
}