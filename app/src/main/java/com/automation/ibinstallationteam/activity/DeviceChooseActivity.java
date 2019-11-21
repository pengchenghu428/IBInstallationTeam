package com.automation.ibinstallationteam.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.adapter.DeviceChooseAdapter;
import com.automation.ibinstallationteam.entity.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceChooseActivity extends AppCompatActivity {

    private final static String TAG = "DeviceChooseActivity";

    // 控件
    private ListView mDeviceLv;
    private List<Device> deviceList;
    private DeviceChooseAdapter deviceChooseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_choose);

        initWidgets();
    }

    /* 初始化控件
    * */
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("选择设备");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 列表
        mDeviceLv = (ListView) findViewById(R.id.device_choose_lv);
        initDeviceList();
        deviceChooseAdapter = new DeviceChooseAdapter(DeviceChooseActivity.this,
                R.layout.item_device_choose, deviceList);  // 初始化适配器
        mDeviceLv.setAdapter(deviceChooseAdapter);
        mDeviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
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

    /* Other
    * */
    private void initDeviceList(){
        deviceList = new ArrayList<>();
        deviceList.add(new Device("电柜", R.mipmap.ic_electrical_box));
        deviceList.add(new Device("摄像头", R.mipmap.ic_camera));
        deviceList.add(new Device("安全绳", R.mipmap.ic_safe_rope));
        deviceList.add(new Device("电缆", R.mipmap.ic_cable));
        deviceList.add(new Device("提升机", R.mipmap.ic_electrical_box));
    }
}
