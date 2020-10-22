package com.automation.ibinstallationteam.activity.manage.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.activity.manage.InstallManageActivity;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.utils.http.HttpUtil;
import com.automation.ibinstallationteam.utils.okhttp.BaseCallBack;
import com.automation.ibinstallationteam.utils.okhttp.BaseOkHttpClient;
import com.automation.ibinstallationteam.widget.pldroid.CustomMediaController;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.automation.ibinstallationteam.application.AppConfig.ASPECT_RATIO;
import static com.automation.ibinstallationteam.application.AppConfig.ASPECT_RATIO_VIDEO;

public class BasketVideoActivity extends AppCompatActivity {

    private static final String TAG = "VideoMonitor2Activity";

    // 消息标志位
    private static final int OPEN_VIDEO_SUCESS = 1;
    private static final int OPEN_VIDEO_FAILED = 2;
    public static final int SET_ACCESS_TOKEN_MSG = 100;
    public static final int SET_VIDEO_URL_MSG = 101;
    public static final int INIT_VIDEO_URL_MSG = 103;
    public static final int FINISH_ACTIVITY_MSG = 104;
    public static final int NO_VALID_CAMERA_MSG = 105;

    // 控件声明
    private RelativeLayout mVideoViewRelativelayout;
    private PLVideoTextureView mVideoView;
    private View mLoadingView;
    private TextView mStateInfoTv;

    private Toast mToast = null;
    private int mDisplayAspectRatio = PLVideoTextureView.ASPECT_RATIO_FIT_PARENT; //default
    private List<String> mVideoUrlList = new ArrayList<>();;
    private boolean mIsBuffering = true;
    private CustomMediaController mMediaController;

    // 屏幕适配
    private int mScreenWidth;
    private int mScreenHeight;

    // 吊篮相关
    private String mBasketId;

    // 用户登录token
    private String mToken;

    // 文件缓存
    public SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    // 登录视频查看消息
    private long mExpireTime;  // Token 有效时间戳
    private String mAccessToken;  // 设备Token
    private String mDeviceSerial = "D44041017";  // 设备序列号

    // 处理线程信息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OPEN_VIDEO_SUCESS:
                    mVideoView.setVideoPath(mVideoUrlList.get(0)); // 设置播放地址
                    break;
                case OPEN_VIDEO_FAILED:
                    ToastUtil.showToastTips(BasketVideoActivity.this, "流媒体服务器无响应");
                    finish();
                    break;
                case SET_ACCESS_TOKEN_MSG:  // 获取Token
                    long expireTime = (long)(msg.arg1 * 100000) + (long)(msg.arg2);
                    String accessToken = msg.obj.toString();
                    mExpireTime = expireTime;
                    mAccessToken = accessToken;
                    mEditor.putLong("expireTime", expireTime);
                    mEditor.putString("accessToken", accessToken);
                    mEditor.commit();
                    getEzVideoUrl();
                    break;
                case SET_VIDEO_URL_MSG:  // 设置播放地址
                    openEzVideo();
                    break;
                case INIT_VIDEO_URL_MSG:  // 初始化播放地址并打开默认视频流
                    initEzVideoUrl();
                    break;
                case NO_VALID_CAMERA_MSG:
                    if(msg.arg1 == 1){
                        ToastUtil.showToastTips(BasketVideoActivity.this,
                                "该摄像头未激活或未添加至萤石云账号，请联系管理员");
                    }else if(msg.arg1 == 2){
                        ToastUtil.showToastTips(BasketVideoActivity.this,
                                "设备摄像头未绑定");
                    }
                    mHandler.sendEmptyMessage(FINISH_ACTIVITY_MSG);
                    break;
                case FINISH_ACTIVITY_MSG:
                    try {
                        Thread.sleep(2000);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket_video);

        Intent intent = getIntent();
        mBasketId = intent.getStringExtra(InstallManageActivity.BASKET_ID);  // 获取吊篮id
        if(mBasketId==null || mBasketId.equals("")) mBasketId = "1";

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);  // 本地资源获取
        mEditor = mSharedPref.edit();

        getScreenSize();
        getSharedPrefInfo();  // 获取基本信息
        getCameraNumber();  // 获取摄像头序列号
        initWidget();
    }

    private void initWidget(){
        // 设置播放器大小
        mVideoViewRelativelayout = (RelativeLayout) findViewById(R.id.video_view_rl);
        ViewGroup.LayoutParams lp = mVideoViewRelativelayout.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = (int)(mScreenWidth / ASPECT_RATIO);
        mVideoViewRelativelayout.setLayoutParams(lp);

        // 播放器参数初始化
        mVideoView = (PLVideoTextureView) findViewById(R.id.pl_video_view);
        mLoadingView = (View) findViewById(R.id.loading_ly);  // 设置缓冲提示器
        mVideoView.setBufferingIndicator(mLoadingView);

        View coverView = findViewById(R.id.cover_image_view);  // 设置黑屏覆盖
        mVideoView.setCoverView(coverView);

        mStateInfoTv = (TextView) findViewById(R.id.state_info_tv);  // 缓冲信息

        // 视频流基本参数
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000); // timeout=10s
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1); // 直播优化
        options.setInteger(AVOptions.KEY_FAST_OPEN, 1); // 快开模式
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_HW_DECODE); // 硬解
        options.setInteger(AVOptions.KEY_LOG_LEVEL, 2); // 设置 SDK 的 log 等级
        options.setInteger(AVOptions.KEY_CACHE_BUFFER_DURATION, 500); // 缓存大小500ms
        mVideoView.setAVOptions(options);

        // 控制栏初始化
        mMediaController = new CustomMediaController(this, mBasketId, mVideoUrlList, BasketVideoActivity.this);
        mMediaController.setAnchorView(mVideoViewRelativelayout);  // 设定控制栏位置
        mVideoView.setMediaController(mMediaController);  // 绑定控制栏和播放器
        //mMediaController.setFileName(mBasketId);  // 设置播放页面信息
        mMediaController.setPlVideoTextureView(mVideoView);  // 设置控制栏控制的视频控件
        mMediaController.setVideoSize(mVideoViewRelativelayout, mScreenWidth,
                mVideoView, ASPECT_RATIO_VIDEO); // 设置播放器尺寸

        // 视频播放器监听
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setLooping(false);  // 不循环
    }

    /*
     * 网络相关
     */
    // 获取吊篮对应的设备序列号
    private void getCameraNumber(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("deviceId", mBasketId)
                .addParam("type", 1)  // type = 2 返回摄像头序列号
                .get()
                .url(AppConfig.GET_ELECTRICBOX_CONFIG)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String data = o.toString();
                        JSONObject jsonObject = JSON.parseObject(data);
                        boolean isLogin = jsonObject.getBooleanValue("isLogin");
                        if(isLogin){
                            JSONObject electricBoxConfig = JSON.parseObject(jsonObject.getString("electricBoxConfig"));
                            String cameraId = electricBoxConfig.getString("cameraId");

                            if(cameraId==null || cameraId.equals("") || cameraId.isEmpty()){
                                Message msg = new Message();
                                msg.what = NO_VALID_CAMERA_MSG;
                                msg.arg1 = 2;
                                mHandler.sendMessage(msg);
                            }else{  // 获取正确的摄像头序列号
                                mDeviceSerial = cameraId;
                                mHandler.sendEmptyMessage(INIT_VIDEO_URL_MSG);
                            }
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.i(TAG, "Error:" + code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "Failure:" + e.toString());
                    }
                });
    }

    // 打开海康视屏
    public void openEzVideo(){
        mVideoView.setVideoPath(mVideoUrlList.get(0));
    }
    // 打开板载视频。 flag->true:打开， false:关闭
    public void openDeviceDefaultVideo(){
        HttpUtil.getDeviceVideoOkHttpRequest(new Callback() {  // 通知硬件打开流媒体服务器
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "失败：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                switch (response.code()){
                    case 200:
                        Log.i(TAG, "成功");
                        mHandler.sendEmptyMessage(OPEN_VIDEO_SUCESS);
                        break;
                    default:
                        Log.i(TAG, "错误编码：" + response.code());
                        mHandler.sendEmptyMessage(OPEN_VIDEO_FAILED);
                        break;
                }
            }
        }, mToken, mBasketId, mVideoUrlList.get(1));
    }

    /** 播放相关函数
     */
    // 获取AccessToken
    private void getEzAccessToken(){
        HttpUtil.getEZAccessToken(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "失败：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);  // string 转 jsonobject
                String code = jsonObject.getString("code");
                if (code.equals("200")){
                    String data = jsonObject.getString("data");
                    JSONObject dataJsonObject = JSON.parseObject(data);  // string 转 jsonobject
                    String accessToken = dataJsonObject.getString("accessToken");
                    long expireTime = dataJsonObject.getLong("expireTime");
                    Message msg = new Message();
                    msg.obj = accessToken;
                    expireTime = (long) expireTime / 1000;
                    msg.arg1 = (int) expireTime / 100000;
                    msg.arg2 = (int) expireTime % 100000;
                    msg.what = SET_ACCESS_TOKEN_MSG;
                    mHandler.sendMessage(msg);
                }
            }
        }, AppConfig.EZUIKit_APPKEY, AppConfig.EZUIKit_SECRET);
    }
    // 获取用户所有的直播地址
    private void getEzVideoUrl(){
        HttpUtil.getEZVideoUrlList(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "失败：" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);  // string 转 jsonobject
                String code = jsonObject.getString("code");
                if (code.equals("200")){
                    String data = jsonObject.getString("data");
                    JSONArray urls = JSON.parseArray(data);
                    boolean if_exist = false;  // 检查摄像头是否激活/公司账户
                    for(int i=0; i<urls.size(); i++){
                        JSONObject row = urls.getJSONObject(i);
                        String deviceSeries = row.getString("deviceSerial");
                        if (deviceSeries.equals(mDeviceSerial)){
                            String videoUrl = row.getString("rtmp");  // 一般清晰度视频
                            mVideoUrlList.add(videoUrl);
//                            String videoUrlHd = row.getString("rtmpHd");  // 暂时无法解析高清视频
//                            mVideoUrlList.add(videoUrlHd);
//                            mVideoUrlList.add(VIDEO_STREAM_PATH + "/rtmplive/" + mBasketId);  // 板子上视频
                            mHandler.sendEmptyMessage(SET_VIDEO_URL_MSG);
                            if_exist = true;
                            break;
                        }
                    }
                    if(!if_exist){

                        Message msg = new Message();
                        msg.what = NO_VALID_CAMERA_MSG;
                        msg.arg1 = 1;  // 状态码
                        Looper.prepare();
                        mHandler.handleMessage(msg);
                        Looper.loop();
                    }
                }
            }
        }, mAccessToken);
    }
    // 更新状态
    private void updateStatInfo() {
        long bitrate = mVideoView.getVideoBitrate() / 1024;
        final String stat = bitrate + "kbps";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStateInfoTv.setText(stat);
            }
        });
    }

    // 获取本地expireTime 和 accessToken
    public void getSharedPrefInfo(){
        mToken = mSharedPref.getString("loginToken","");
        mExpireTime = mSharedPref.getLong("expireTime", 0);
        mAccessToken = mSharedPref.getString("accessToken", "");
    }
    // 初始化播放地址
    private void initEzVideoUrl(){
        if (mExpireTime==0 || mAccessToken.equals(""))
            getEzAccessToken();
        else{
            if (System.currentTimeMillis() >= mExpireTime*1000) {  // 时间超过7天，重新获取
                getEzAccessToken();
            }else{
                getEzVideoUrl();
            }
        }
    }

    /*
     * 其他
     */
    // 获取屏幕的宽高度
    private void getScreenSize(){
        DisplayMetrics dm2 = getResources().getDisplayMetrics();
        mScreenHeight = dm2.heightPixels;
        mScreenWidth = dm2.widthPixels;
    }

    /*
     * 生命周期函数
     */
    // 正在运行
    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();  // 开始播放
    }

    // 离开页面
    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();  // 暂停播放
        mToast = null;
    }

    // 销毁页面
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();  // 释放资源
    }

    // 重写默认按键功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    finish();  // 竖屏-》销毁活动
                else if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    mMediaController.doOriginScreen(this); // 横屏-》竖屏
                break;
        }
        return true;
    }

    /*
     * 监听函数
     */
    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                    Log.w(TAG, "media info buffering start ");
                    mLoadingView.setVisibility(View.VISIBLE);
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    Log.w(TAG, "media info buffering end ");
                    mLoadingView.setVisibility(View.GONE);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    ToastUtil.showToastTips(BasketVideoActivity.this,
                            "First video render time: " + extra + "ms");
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    Log.i(TAG, "First audio render time: " + extra + "ms");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    Log.i(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    Log.i(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                    Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLOnInfoListener.MEDIA_INFO_METADATA:
                    Log.i(TAG, mVideoView.getMetadata().toString());
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                    updateStatInfo();
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    Log.i(TAG, "Connected !");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    Log.i(TAG, "Rotation changed: " + extra);
                    break;
                default:
                    break;
            }
        }
    };

    private PLOnErrorListener mOnErrorListener = new PLOnErrorListener() {
        @Override
        public boolean onError(int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    ToastUtil.showToastTips(BasketVideoActivity.this, "IO Error !");
                    return false;
                case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                    ToastUtil.showToastTips(BasketVideoActivity.this, "failed to open player !");
                    break;
                case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                    ToastUtil.showToastTips(BasketVideoActivity.this, "failed to seek !");
                    return true;
                default:
                    ToastUtil.showToastTips(BasketVideoActivity.this, "unknown error !");
                    break;
            }
            finish();
            return true;
        }
    };

    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            Log.i(TAG, "Play Completed !");
            ToastUtil.showToastTips(BasketVideoActivity.this, "Play Completed !");
            finish();
        }
    };

    private PLOnBufferingUpdateListener mOnBufferingUpdateListener = new PLOnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(int precent) {
            Log.w(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            Log.i(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height);

        }
    };
}