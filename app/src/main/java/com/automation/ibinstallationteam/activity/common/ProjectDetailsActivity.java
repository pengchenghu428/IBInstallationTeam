package com.automation.ibinstallationteam.activity.common;

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
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.utils.okhttp.BaseCallBack;
import com.automation.ibinstallationteam.utils.okhttp.BaseOkHttpClient;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

public class ProjectDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "OutStorageActivity";

    // 消息处理标识
    private final static int EXIT_ACTIVITY_FLAG = 101;
    private final static int UPDATE_PRO_INFO_FLAG = 102;
    private final static int UPDATE_BASKET_LIST_FLAG = 103;

    // 控件
    private TextView mProjectIdTextView; // 项目id
    private TextView mProjectNameTextView; //项目名称
    private TextView mProjectStartTextView; // 项目开始日期
    private TextView mProjectReadyInstallTextView; // 项目待安装吊篮数目
    private Button mApplyInstallationBtn; // 申请安装

    // 项目相关信息
    private String mProjectId;  // 项目ID

    // 后台请求必要信息
    private String mToken;
    private SharedPreferences mPref;

    // mHandler 处理消息
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case EXIT_ACTIVITY_FLAG:  // 退出页面
                    ToastUtil.showToastTips(ProjectDetailsActivity.this, "错误的项目号，请检查是否存在该项目");
                    finish();
                    break;
                case UPDATE_PRO_INFO_FLAG:  // 更新项目信息
                    parseProjectDetail((String)msg.obj);
                    break;
                case UPDATE_BASKET_LIST_FLAG:
                    parseBasketListInfo((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        Intent intent = getIntent();
        mProjectId = intent.getStringExtra(SearchProjectActivity.PROJECT_ID);  // 获取吊篮id
        if(mProjectId==null || mProjectId.equals("")){
            ToastUtil.showToastTips(ProjectDetailsActivity.this, "无效的项目号！");
            finish();
        }

        if(!isHasPermission()) requestPermission();  // 申请权限

        getUserToken();     // 获取Token
        getProInfo();
        initWidgetResource();  // 初始化控件
    }

    /*
     * 页面初始化
     */
    // 控件初始化
    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("项目详情");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 项目信息相关
        mProjectIdTextView = (TextView) findViewById(R.id.project_id_textview);
        mProjectNameTextView = (TextView) findViewById(R.id.project_name_textview);
        mProjectStartTextView = (TextView) findViewById(R.id.project_start_time_textview);
        mProjectReadyInstallTextView = (TextView) findViewById(R.id.project_ready_install_textview);

        // 申请安装按钮
        mApplyInstallationBtn = (Button) findViewById(R.id.apply_for_installation);
        mApplyInstallationBtn.setOnClickListener(this);
    }


    /*
     * 消息响应
     */
    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.apply_for_installation:  // 点击查看项目清单
                break;
        }
    }

    /*
     * 从后台获取数据
     */
    // 获取项目信息
    private void getProInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .get()
                .url(AppConfig.GET_PROJECT_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取项目信息");
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        String projectDetail = jsonObject.getString("projectDetail");
                        if(projectDetail == null || projectDetail.equals("")){
                            // 退出页面并提示
                            mHandler.sendEmptyMessage(EXIT_ACTIVITY_FLAG);
                        }else{
                            Message msg = new Message();
                            msg.what = UPDATE_PRO_INFO_FLAG;
                            msg.obj = projectDetail;
                            mHandler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取项目信息错误，错误编码："+code);

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "获取项目信息失败");
                    }
                });
    }
    // 解析项目基本信息
    private void parseProjectDetail(String data){
        JSONObject jsonObject = JSON.parseObject(data);
        String projectName = jsonObject.getString("projectName");
        if(projectName!=null || !projectName.equals("")) mProjectNameTextView.setText(projectName);
        mProjectId = jsonObject.getString("projectId");
        if(mProjectId!=null || !mProjectId.equals("")) mProjectIdTextView.setText(mProjectId);
        String projectStart= jsonObject.getString("projectStart");
        if(projectStart!=null || !projectStart.equals("")) mProjectStartTextView.setText(projectStart);
    }

    // 获取项目吊篮列表
    private void getBasketList(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("projectId", mProjectId)
                .get()
                .url(AppConfig.GET_BASKET_LIST_INFO)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "成功获取吊篮列表");
                        String responseData = o.toString();
                        Message message = new Message();
                        message.what = UPDATE_BASKET_LIST_FLAG;
                        message.obj = responseData;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(int code) {
                        Log.d(TAG, "获取吊篮列表信息错误，错误编码："+code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "获取吊篮列表信息失败");
                    }
                });
    }
    // 解析项目中的吊篮列表信息
    private void parseBasketListInfo(String responseDate) {
        int readyInstallNum = 0;

        JSONObject jsonObject = JSON.parseObject(responseDate);
        Iterator<String> iterator = jsonObject.keySet().iterator();  // 迭代获取吊篮信息
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!key.contains("storage")) continue;
            String value = jsonObject.getString(key);
            if (value == null || value.equals("")) continue;
            JSONObject basketObj = JSON.parseObject(value);
            String deviceId = basketObj.getString("deviceId");
            if (deviceId == null || deviceId.equals("")) continue;
            if(basketObj.getString("storageState").equals("1"))  // 待安装的吊篮数目为+1
                readyInstallNum += 1;
        }

        mProjectReadyInstallTextView.setText(String.valueOf(readyInstallNum));
    }

    /*
     * 解析用户信息
     */
    // 获取用户数据
    private void getUserToken(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mPref.getString("loginToken","");
    }

    /*
     * 权限管理
     */
     /*
        用xxpermissions申请权限
     */
    // 申请权限
    private void requestPermission() {
        XXPermissions.with(ProjectDetailsActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.CAMERA) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            //initCamera(scanPreview.getHolder());
                            onResume();
                        }else {
                            Toast.makeText(ProjectDetailsActivity.this,
                                    "必须同意所有的权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(ProjectDetailsActivity.this,
                                    "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(ProjectDetailsActivity.this);
                        }else {
                            Toast.makeText(ProjectDetailsActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    // 是否有权限：摄像头
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(ProjectDetailsActivity.this, Permission.CAMERA))
            return true;
        return false;
    }
}
