package com.automation.ibinstallationteam.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;

/*
 * 安装管理页面
 */

public class InstallManageActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "InstallManageActivity";

    // 控件
    private LinearLayout mWorkerInfoLayout;  // 信息采集
    private LinearLayout mFinishImgLayout;  // 完工图片
    private LinearLayout mDeviceBoundLayout;  // 设备绑定

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_manage);

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
    }

    /* 消息响应
     */
    // 按键消息
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.worker_info_layout:  // 添加工人信息
                break;
            case R.id.finish_img_layout:
                break;
            case R.id.device_bound_layout:
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
}
