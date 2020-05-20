package com.automation.ibinstallationteam.activity.manage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.activity.manage.device.DeviceBoundActivity;
import com.automation.ibinstallationteam.activity.manage.image.FinishImgActivity;
import com.automation.ibinstallationteam.activity.manage.worker.WorkerInfoActivity;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.UserInfo;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.utils.okhttp.BaseCallBack;
import com.automation.ibinstallationteam.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;

import okhttp3.Call;

/*
 * 安装管理页面
 */

public class InstallManageActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "InstallManageActivity";

    // Handler 消息
    private final static int UPDATE_STATE_SUCCESS_MSG = 100;

    // 控件
    private LinearLayout mWorkerInfoLayout;  // 信息采集
    private LinearLayout mFinishImgLayout;  // 完工图片
    private LinearLayout mDeviceBoundLayout;  // 设备绑定

    // 页面消息传递
    public final static String PROJECT_ID = "project_id";
    public final static String BASKET_ID = "basket_id";
    public final static String BASKET_FLAG = "basket_flag";

    private Button mConfirmApplyBtn;  // 确认提交按钮

    // 项目、吊篮
    private String mProjectId;  // 项目号
    private String mBasketId;  // 吊篮号
    private int mBasketFlag; // 吊篮安装状态
    private int mBasketState; // 吊篮流程状态

    // 个人信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;

    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_STATE_SUCCESS_MSG:  // 更新WorkInfoList状态
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_manage);

        getUserInfo();
        getIntentInfo();
        initWidgets();
    }

    /* 控件初始化
     */
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("安装管理");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mWorkerInfoLayout = (LinearLayout) findViewById(R.id.worker_info_layout);
        mWorkerInfoLayout.setOnClickListener(this);
        mFinishImgLayout = (LinearLayout) findViewById(R.id.finish_img_layout);
        mFinishImgLayout.setOnClickListener(this);
        mDeviceBoundLayout = (LinearLayout) findViewById(R.id.device_bound_layout);
        mDeviceBoundLayout.setOnClickListener(this);
        mConfirmApplyBtn = (Button) findViewById(R.id.confirm_apply_btn);
        mConfirmApplyBtn.setOnClickListener(this);
        if(mBasketFlag==0)  // 提交按钮显示与否
            mConfirmApplyBtn.setVisibility(View.VISIBLE);
        else
            mConfirmApplyBtn.setVisibility(View.INVISIBLE);
    }

    /* 消息响应
     */
    // 按键消息
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.worker_info_layout:  // 添加工人信息
                intent = new Intent(InstallManageActivity.this, WorkerInfoActivity.class);
                intent.putExtra(PROJECT_ID, mProjectId);
                intent.putExtra(BASKET_ID, mBasketId);
                intent.putExtra(BASKET_FLAG, mBasketFlag);
                startActivity(intent);
                break;
            case R.id.finish_img_layout:  // 完工图片
                intent = new Intent(InstallManageActivity.this, FinishImgActivity.class);
                intent.putExtra(PROJECT_ID, mProjectId);
                intent.putExtra(BASKET_ID, mBasketId);
                intent.putExtra(BASKET_FLAG, mBasketFlag);
                startActivity(intent);
                break;
            case R.id.device_bound_layout:  // 设备绑定
                intent = new Intent(InstallManageActivity.this, DeviceBoundActivity.class);
                intent.putExtra(PROJECT_ID, mProjectId);
                intent.putExtra(BASKET_ID, mBasketId);
                intent.putExtra(BASKET_FLAG, mBasketFlag);
                startActivity(intent);
                break;
            case R.id.confirm_apply_btn:  // 确认提交按钮
                updateFinishImgState();
                break;
        }
    }
    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* 后台通信
    * */
    /* 后台数据库 */
    private void updateFinishImgState(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .addParam("projectId", mProjectId)
                .addParam("deviceId", mBasketId)
                .addParam("state", 1)  // 0 未完工 1 完工
                .addParam("type", 1)  // type 1 吊篮状态
                .post()
                .url(AppConfig.UPDATE_INSTALLER_STATE)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String data = o.toString();
                        JSONObject jsonObject = JSON.parseObject(data);
                        boolean isLogin = jsonObject.getBooleanValue("isLogin");
                        if(isLogin){
                            if(jsonObject.getString("update").equals("success")){
                                ToastUtil.showToastTips(InstallManageActivity.this, "吊篮完工申请中...");
                                mHandler.sendEmptyMessage(UPDATE_STATE_SUCCESS_MSG);
                            }else{
                                ToastUtil.showToastTips(InstallManageActivity.this, "无法完工，信息上传不完整");
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

    /*
     * 本地信息交互
     */
    // 获取个人信息
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUserInfo = new UserInfo();
        mUserInfo.setUserId(mPref.getString("userId", ""));
        mUserInfo.setUserPhone(mPref.getString("userPhone", ""));
        mUserInfo.setUserRole(mPref.getString("userRole", ""));
        mToken = mPref.getString("loginToken","");
    }
    // 获取页面传递消息
    private void getIntentInfo(){
        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(BasketActivity.PROJECT_ID);
        mBasketId = intent.getStringExtra(BasketActivity.BASKET_ID);
        mBasketFlag = intent.getIntExtra(BasketActivity.BASKET_FLAG, 0);
        mBasketState = intent.getIntExtra(BasketActivity.BASKET_STATE, 1);
    }
}
