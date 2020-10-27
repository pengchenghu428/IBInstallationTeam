package com.automation.ibinstallationteam.utils.http;

import android.net.Uri;

import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.application.AppConfig;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    //注册请求
    public static void sendRegistOkHttpRequest(okhttp3.Callback callback, String userName, String userPassword,
                                               String userPhone,String userRole) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);
        jsonObject.put("userPassword", userPassword);
        jsonObject.put("userPhone", userPhone);
        jsonObject.put("userRole", userRole);
        String json = jsonObject.toJSONString();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(AppConfig.REGISTER_USER)
                .addHeader("Authorization", "null")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    // 登录请求
    public static void sendLoginOkHttpRequest(okhttp3.Callback callback, String account, String password) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userPhone", account);
        jsonObject.put("userPassword", password);
        String json = jsonObject.toJSONString();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(AppConfig.LOGIN_USER)
                .addHeader("Authorization", "null")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /*
     * 设备参数请求
     * /getRealTimeData
     * get token deviceId
     */
    public static void getDeviceParameterOkHttpRequest(okhttp3.Callback callback,
                                                       String token, String deviceId) {
        OkHttpClient client = new OkHttpClient();

        StringBuilder tempParams = new StringBuilder();
        //对参数进行URLEncoder
        tempParams.append(String.format("%s=%s", "deviceId", Uri.encode(deviceId), "utf-8"));
        String requestUrl = String.format("%s?%s", AppConfig.REAL_TIME_PARAMETER, tempParams.toString());
        final Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", token)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }

    /*
     * 设备视频请求
     * /getRealTimeData
     * get token deviceId
     */
    public static void getDeviceVideoOkHttpRequest(okhttp3.Callback callback, String token,
                                                   String deviceId, String videoUrl) {
        // 生成推流地址
        String command = "/server.command?command=start_rtmp_stream&pipe=0&url=".concat(videoUrl);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "getVideo");
        jsonObject.put("device_id", Long.valueOf(deviceId));
        jsonObject.put("http_str", command);
        String json = jsonObject.toJSONString();

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(AppConfig.HANGING_BASKET_VIDEO)
                .addHeader("Authorization", token)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }
    /*萤石云
     */
    public static void getEZAccessToken(okhttp3.Callback callback, String key, String secret){
        OkHttpClient client = new OkHttpClient();
        FormBody builder = new FormBody.Builder()
                .add("appKey", key)
                .add("appSecret", secret)
                .build();
        final Request request = new Request.Builder()
                .url(AppConfig.GET_UIKIT_ACCESS_TOKEN)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public static void getEZVideoUrlList(okhttp3.Callback callback, String accessToken){
        OkHttpClient client = new OkHttpClient();
        FormBody builder = new FormBody.Builder()
                .add("accessToken", accessToken)
                .build();
        final Request request = new Request.Builder()
                .url(AppConfig.GET_UIKIT_VIDEO_URL)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void getEZVideoUrl(okhttp3.Callback callback, String accessToken,
                                     String source){
        OkHttpClient client = new OkHttpClient();
        FormBody builder = new FormBody.Builder()
                .add("accessToken", accessToken)
                .add("source", source)
                .build();
        final Request request = new Request.Builder()
                .url(AppConfig.GET_UIKIT_DEVICE_INFO)
                .post(builder)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
