package com.automation.ibinstallationteam.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.adapter.DeviceAddedAdapter;
import com.automation.ibinstallationteam.entity.Device;

import java.util.ArrayList;
import java.util.List;

/*
 * 设备绑定
 */
public class DeviceBoundActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "DeviceBoundActivity";

    // 控件
    private RecyclerView mAddedDeviceRv;  // 已添加设备列表
    private List<Device> mDeviceList;
    private DeviceAddedAdapter mDeviceAdapter;
    private LinearLayout mAddDeviceLl;  // 添加设备按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_bound);

        initWidgets();
    }

    /* 控件初始化
    * */
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("设备绑定");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 已添加设备列表
        mAddedDeviceRv = (RecyclerView) findViewById(R.id.added_device_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAddedDeviceRv.setLayoutManager(layoutManager);
        mDeviceList = new ArrayList<>();
        initDevices();
        mDeviceAdapter = new DeviceAddedAdapter(this, mDeviceList);
        mAddedDeviceRv.setAdapter(mDeviceAdapter);
        mDeviceAdapter.setOnItemClickListener(new DeviceAddedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击Item
            }

            @Override
            public void onDeleteClick(View view, int position) {
                // 点击Item 中的删除按键
            }
        });

        // 添加设备按钮
        mAddDeviceLl = (LinearLayout) findViewById(R.id.add_device_ll);
        mAddDeviceLl.setOnClickListener(this);
    }

    /* 消息响应
    * */
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
    // 普通按键响应
    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            case R.id.add_device_ll:  // 添加设备
                intent = new Intent(DeviceBoundActivity.this, DeviceChooseActivity.class);
                startActivity(intent);
                break;
        }
    }

    /* Other
    * */
    private void initDevices(){
        mDeviceList.add(new Device("摄像头", R.mipmap.ic_camera, "DS400119873245"));
        mDeviceList.add(new Device("提升机", R.mipmap.ic_elevator, "ELE3399052710"));
    }
}
