package com.automation.ibinstallationteam.activity.manage.site;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.activity.manage.InstallManageActivity;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.UserInfo;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.utils.okhttp.BaseCallBack;
import com.automation.ibinstallationteam.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;

import okhttp3.Call;

public class ChangeSiteIdActivity extends AppCompatActivity {
    private final static String TAG = "ChangeSiteIdActivity";

    private final static int UPDATE_SITE_ID_SUCCESS = 100;
    private final static int UPDATE_SITE_ID_FAILED = 101;

    // 控件
    private EditText mSiteIdEv;
    private Button mSaveBtn;

    // 全局参数
    private String mSiteId;
    private String mBasketId;
    private String mToken;
    private SharedPreferences mPref;

    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_SITE_ID_SUCCESS:  // 更新WorkInfoList状态
                    ToastUtil.showToastTips(ChangeSiteIdActivity.this, "更新成功");
                    finish();
                    break;
                case UPDATE_SITE_ID_FAILED:
                    ToastUtil.showToastTips(ChangeSiteIdActivity.this, "更新失败");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_siteid);

        getUserInfo();
        getInfoFromIntent();
        initWidgets();

    }

    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("更改现场编号");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mSiteIdEv = (EditText) findViewById(R.id.site_id_ev);
        mSiteIdEv.setText(mSiteId);
        mSiteIdEv.setSelection(mSiteIdEv.getText().toString().length());
        mSaveBtn = (Button) findViewById(R.id.save_btn);
        mSaveBtn.setClickable(false);
        mSaveBtn.setEnabled(false);

        mSiteIdEv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 内容改变前
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 字符改变中
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 字符改变后
                mSaveBtn.setClickable(true);
                mSaveBtn.setEnabled(true);
                mSaveBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSiteId();
            }
        });
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

    /*
     * 保存现场编号
     */
    private void saveSiteId(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("siteNo", mSiteIdEv.getText().toString())
                .addParam("deviceId", mBasketId)
                .post()
                .url(AppConfig.UPDATE_SITE_ID)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String data = o.toString();
                        JSONObject jsonObject = JSON.parseObject(data);
                        boolean isLogin = jsonObject.getBooleanValue("isLogin");
                        if(isLogin){
                            if(jsonObject.getString("update").equals("success")){
                                mHandler.sendEmptyMessage(UPDATE_SITE_ID_SUCCESS);
                            }else{
                                mHandler.sendEmptyMessage(UPDATE_SITE_ID_FAILED);
                            }
                        }else{
                            mHandler.sendEmptyMessage(UPDATE_SITE_ID_FAILED);
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
     * 页面通信
     */
    // 获取个人信息
    private void getUserInfo(){
        // 从本地获取数据
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mPref.getString("loginToken","");
    }
    private void getInfoFromIntent(){
        Intent intent = getIntent();
        mSiteId = intent.getStringExtra(InstallManageActivity.SITE_ID);
        mBasketId = intent.getStringExtra(InstallManageActivity.BASKET_ID);
    }
}