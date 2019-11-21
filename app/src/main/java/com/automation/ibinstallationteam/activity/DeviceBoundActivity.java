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
import android.widget.Toast;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.adapter.DeviceAddedAdapter;
import com.automation.ibinstallationteam.entity.Device;
import com.automation.ibinstallationteam.entity.Portion;
import com.automation.ibinstallationteam.entity.PortionMap;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.widget.zxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.List;

/*
 * 设备绑定
 */
public class DeviceBoundActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "DeviceBoundActivity";

    // 页面跳转标志
    private final static int DEVICE_CHOOSEN_ACTIVITY = 100;
    private final static int CAPTURE_ACTIVITY_RESULT = 101;

    // 页面消息传递标志
    public final static String QRCODE_RESULT = "qr_code";
    public final static String DEVICE_NAME = "device_name";

    // 全局变量
    private int prePosition = -1;

    // 控件
    private RecyclerView mAddedDeviceRv;  // 已添加设备列表
    private List<Device> mDeviceList;
    private DeviceAddedAdapter mDeviceAdapter;
    private LinearLayout mAddDeviceLl;  // 添加设备按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_bound);

        if(!isHasPermission()) requestPermission();
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
                prePosition = position;
                startActivityForResult(new Intent(DeviceBoundActivity.this, CaptureActivity.class), CAPTURE_ACTIVITY_RESULT);
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
                if(!isHasPermission()) requestPermission();
                intent = new Intent(DeviceBoundActivity.this, DeviceChooseActivity.class);
                startActivityForResult(intent, DEVICE_CHOOSEN_ACTIVITY);
                break;
        }
    }

    /*
     * 活动返回
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case DEVICE_CHOOSEN_ACTIVITY:
                if(resultCode == RESULT_OK){
                    String deviceName = data.getStringExtra(DEVICE_NAME);  // 名称
                    String deviceNumber = data.getStringExtra(QRCODE_RESULT);  // 序列号
                    /*
                     * Do Something: 和后台通讯
                     */
                    int idx = PortionMap.chinesePortion.indexOf(deviceName);
                    String imageName = "ic_" + PortionMap.englishPortion.get(idx);
                    int imageResourceId =  getResources().getIdentifier(imageName, "mipmap", getPackageName());
                    mDeviceList.add(new Device(deviceName, imageResourceId, deviceNumber));
                    mDeviceAdapter.notifyDataSetChanged();
                }
                break;
            case CAPTURE_ACTIVITY_RESULT:
                if(resultCode == RESULT_OK) {
                    String qrCode = data.getStringExtra(CaptureActivity.QR_CODE_RESULT);
                    if(qrCode.equals(mDeviceList.get(prePosition).getNumber())){
                        // 扫码和原先的序列号一致，则删除该设备
                        mDeviceList.remove(prePosition);
                        mDeviceAdapter.notifyDataSetChanged();
                    }else{
                        ToastUtil.showToastTips(DeviceBoundActivity.this, "设备不一致，请确认后在此扫描！");
                    }
                }
                break;
        }
    }

    /* Other
    * */
    private void initDevices(){
        mDeviceList.add(new Device("摄像头", R.mipmap.ic_camera, "DS400119873245"));
        mDeviceList.add(new Device("提升机", R.mipmap.ic_elevator, "ELE3399052710"));
    }

    /*
     * 申请权限
     */
    private void requestPermission() {
        XXPermissions.with(DeviceBoundActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CAMERA)
                .permission(Permission.CALL_PHONE)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();

                        }else {
                            Toast.makeText(DeviceBoundActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(DeviceBoundActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(DeviceBoundActivity.this);
                        }else {
                            Toast.makeText(DeviceBoundActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }
    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(DeviceBoundActivity.this, Permission.Group.STORAGE)
                && XXPermissions.isHasPermission(DeviceBoundActivity.this, Permission.CAMERA)
                && XXPermissions.isHasPermission(DeviceBoundActivity.this, Permission.CALL_PHONE))
            return true;
        return false;
    }
}
