package com.automation.ibinstallationteam.activity.manage.worker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.activity.manage.InstallManageActivity;
import com.automation.ibinstallationteam.adapter.WorkerInfoAdapter;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.UserInfo;
import com.automation.ibinstallationteam.entity.WorkerInfo;
import com.automation.ibinstallationteam.utils.okhttp.BaseCallBack;
import com.automation.ibinstallationteam.utils.okhttp.BaseOkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

public class WorkerInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "WorkerInfoActivity";

    // 页面跳转标志
    private final static int NEW_WORKER_INFO_ACTIVITY = 100;  // 新建
    private final static int MODIFY_WORKER_INFO_ACTIVITY = 101;  // 修改

    // handler 消息
    private final static int UPDATE_WORKER_INFOS_MSG = 100;

    // 页面消息传递
    public final static String PROJECT_ID = "project_id";  // 0 新建 1 修改或删除
    public final static String BASKET_ID = "basket_id";  // 0 新建 1 修改或删除
    public final static String OPERATION_TYPE = "operation_type";  // 0 新建 1 修改或删除
    public final static String WORKER_NAME = "worker_name";
    public final static String WORKER_PHONE_NUMBER = "worker_phone_number";
    public final static String WORKER_ID_CARD_NUMBER = "worker_id_card_number";
    public final static String OPERATION_IN_FACT = "operation_in_fact"; // 实际操作 0：删除 1：修改

    // 控件
    private RecyclerView mAddedWorkerRv;
    private List<WorkerInfo> workerInfoList;
    private WorkerInfoAdapter workerInfoAdapter;
    private LinearLayout mAddWorlerLl;

    // 全局变量
    private int prePosition = -1;

    // 项目、吊篮
    private String mProjectId;  // 项目号
    private String mBasketId;  // 吊篮号
    private int mBasketFlag;

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
                case UPDATE_WORKER_INFOS_MSG:  // 更新WorkInfoList状态
                    workerInfoAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_info);

        getUserInfo();
        getIntentInfo();
        getWorkerInfo();
        initWidgets();
    }

    /* 初始化控件
    * */
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("作业人员管理");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 已添加作业人员列表
        mAddedWorkerRv = (RecyclerView) findViewById(R.id.added_worker_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mAddedWorkerRv.setLayoutManager(layoutManager);
        workerInfoList = new ArrayList<>();
//        initWorkInfoList();
        workerInfoAdapter = new WorkerInfoAdapter(this, workerInfoList);
        mAddedWorkerRv.setAdapter(workerInfoAdapter);
        workerInfoAdapter.setOnItemClickListener(new WorkerInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                prePosition = position;
                WorkerInfo workerInfo = workerInfoList.get(position);
                // 点击Item
                if(mBasketFlag==0) {  // 修改、删除
                    Intent intent = new Intent(WorkerInfoActivity.this, OperateWorkerInfoActivity.class);
                    intent.putExtra(OPERATION_TYPE, 1);  // 修改
                    intent.putExtra(PROJECT_ID, mProjectId);
                    intent.putExtra(BASKET_ID, mBasketId);
                    intent.putExtra(WorkerInfoActivity.WORKER_NAME, workerInfo.getName());
                    intent.putExtra(WorkerInfoActivity.WORKER_PHONE_NUMBER, workerInfo.getPhoneNumber());
                    intent.putExtra(WorkerInfoActivity.WORKER_ID_CARD_NUMBER, workerInfo.getIdCardNumber());
                    startActivityForResult(intent, MODIFY_WORKER_INFO_ACTIVITY);
                }else{ // 查看
                    Intent intent = new Intent(WorkerInfoActivity.this, OperateWorkerInfoActivity.class);
                    intent.putExtra(OPERATION_TYPE, 2);  // 修改
                    intent.putExtra(PROJECT_ID, mProjectId);
                    intent.putExtra(BASKET_ID, mBasketId);
                    intent.putExtra(WorkerInfoActivity.WORKER_NAME, workerInfo.getName());
                    intent.putExtra(WorkerInfoActivity.WORKER_PHONE_NUMBER, workerInfo.getPhoneNumber());
                    intent.putExtra(WorkerInfoActivity.WORKER_ID_CARD_NUMBER, workerInfo.getIdCardNumber());
                    startActivity(intent);
                }
            }

            @Override
            public void onCallPhoneClick(View view, int position) {
                // 点击拨号按钮
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+workerInfoList.get(position).getPhoneNumber()));
                startActivity(intent);
            }
        });

        // 添加作业人员按钮
        mAddWorlerLl = (LinearLayout) findViewById(R.id.add_worker_ll);
        mAddWorlerLl.setOnClickListener(this);
        if(mBasketFlag==1)  // 安装过程可见
            mAddWorlerLl.setVisibility(View.INVISIBLE);
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
    // 一般消息
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.add_worker_ll:
                intent = new Intent(WorkerInfoActivity.this, OperateWorkerInfoActivity.class);
                intent.putExtra(OPERATION_TYPE, 0); // 新建
                intent.putExtra(PROJECT_ID, mProjectId);
                intent.putExtra(BASKET_ID, mBasketId);
                startActivityForResult(intent, NEW_WORKER_INFO_ACTIVITY);
                break;
        }
    }

    /*
     * 后台通信
     */
    // 获取全部施工队队员信息
    private void getWorkerInfo(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .addParam("projectId", mProjectId)
                .addParam("deviceId", mBasketId)
                .get()
                .url(AppConfig.GET_INSTALLER)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String data = o.toString();
                        JSONObject jsonObject = JSON.parseObject(data);
                        boolean isLogin = jsonObject.getBooleanValue("isLogin");
                        if(isLogin)
                            parseWorkerInfo(jsonObject.getString("installerTeam"));

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

    private void parseWorkerInfo(String data){
        workerInfoList.clear();
        JSONArray jsonArray = JSON.parseArray(data);

        Iterator<Object> iterator = jsonArray.iterator();  // 迭代获取项目信息
        while(iterator.hasNext()) {
            JSONObject workerInfoJsonObject = (JSONObject) iterator.next();
            WorkerInfo workerInfo = new WorkerInfo();
            workerInfo.setName(workerInfoJsonObject.getString("name"));
            workerInfo.setPhoneNumber(workerInfoJsonObject.getString("phone"));
            workerInfo.setIdCardNumber(workerInfoJsonObject.getString("account_id"));
            workerInfoList.add(workerInfo);
        }

        mHandler.sendEmptyMessage(UPDATE_WORKER_INFOS_MSG);
    }


    /*
     * 本地信息交互
     */
    // 获取用户数据
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
        mProjectId = intent.getStringExtra(InstallManageActivity.PROJECT_ID);
        mBasketId = intent.getStringExtra(InstallManageActivity.BASKET_ID);
        mBasketFlag = intent.getIntExtra(InstallManageActivity.BASKET_FLAG, 0);
    }

    /* 页面返回消息处理
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case NEW_WORKER_INFO_ACTIVITY:
                if(resultCode == RESULT_OK){
                    String workerName = data.getStringExtra(WORKER_NAME);
                    String workerPhoneNumber = data.getStringExtra(WORKER_PHONE_NUMBER);
                    String workerIdCardNumber = data.getStringExtra(WORKER_ID_CARD_NUMBER);
                    workerInfoList.add(new WorkerInfo(workerName, workerPhoneNumber, workerIdCardNumber));
                    workerInfoAdapter.notifyDataSetChanged();
                }
                break;
            case MODIFY_WORKER_INFO_ACTIVITY:
                if(resultCode == RESULT_OK){
                    int operation_in_fact = data.getIntExtra(OPERATION_IN_FACT, -1);
                    switch(operation_in_fact){
                        case 0:
                            workerInfoList.remove(prePosition);
                            workerInfoAdapter.notifyDataSetChanged();
                            break;
                        case 1:
                            String workerName = data.getStringExtra(WORKER_NAME);
                            String workerPhoneNumber = data.getStringExtra(WORKER_PHONE_NUMBER);
                            String workerIdCardNumber = data.getStringExtra(WORKER_ID_CARD_NUMBER);
                            workerInfoList.remove(prePosition);
                            workerInfoList.add(prePosition, new WorkerInfo(workerName, workerPhoneNumber, workerIdCardNumber));
                            workerInfoAdapter.notifyDataSetChanged();
                            break;
                    }
                }
                break;
        }
    }

    /* Others
    * */
    private void initWorkInfoList(){
        workerInfoList.add(new WorkerInfo("张三", "15651870001", "321322199802230480"));
        workerInfoList.add(new WorkerInfo("李四", "13851856049", "321322197304010929"));
        workerInfoList.add(new WorkerInfo("赵金", "17759830901", "321322198904434982"));
    }
}
