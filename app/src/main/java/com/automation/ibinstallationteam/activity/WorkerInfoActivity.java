package com.automation.ibinstallationteam.activity;

import android.content.Intent;
import android.net.Uri;
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
import com.automation.ibinstallationteam.adapter.WorkerInfoAdapter;
import com.automation.ibinstallationteam.entity.WorkerInfo;

import java.util.ArrayList;
import java.util.List;

public class WorkerInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "WorkerInfoActivity";

    // 页面跳转标志
    private final static int NEW_WORKER_INFO_ACTIVITY = 100;  // 新建
    private final static int MODIFY_WORKER_INFO_ACTIVITY = 101;  // 修改

    // 页面消息传递
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_info);

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
        initWorkInfoList();
        workerInfoAdapter = new WorkerInfoAdapter(this, workerInfoList);
        mAddedWorkerRv.setAdapter(workerInfoAdapter);
        workerInfoAdapter.setOnItemClickListener(new WorkerInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                prePosition = position;
                WorkerInfo workerInfo = workerInfoList.get(position);
                // 点击Item
                Intent intent = new Intent(WorkerInfoActivity.this, OperateWorkerInfoActivity.class);
                intent.putExtra(OPERATION_TYPE, 1);  // 修改
                intent.putExtra(WorkerInfoActivity.WORKER_NAME, workerInfo.getName());
                intent.putExtra(WorkerInfoActivity.WORKER_PHONE_NUMBER, workerInfo.getPhoneNumber());
                intent.putExtra(WorkerInfoActivity.WORKER_ID_CARD_NUMBER, workerInfo.getIdCardNumber());
                startActivityForResult(intent, MODIFY_WORKER_INFO_ACTIVITY);
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
                startActivityForResult(intent, NEW_WORKER_INFO_ACTIVITY);
                break;
        }
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
