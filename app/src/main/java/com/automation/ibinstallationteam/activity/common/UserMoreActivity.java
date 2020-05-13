package com.automation.ibinstallationteam.activity.common;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;

public class UserMoreActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "UserMoreActivity";

    // Intent 消息
    public final static String UPLOAD_IMAGE_TYPE = "uploadImageType";
    public final static String UPLOAD_WORKER_CAPACITY_IMAGE = "userCapacityImage"; // 施工队伍资质技能
    public final static String USER_ID = "user_id";

    // 控件
    private RelativeLayout mUserUploadCapacityImageRv;
    private RelativeLayout mCheckHomePageRv;

    // 个人信息
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_more);

        mUserId = getIntent().getStringExtra("worker_id");
        initWidgets();
    }

    /*
     * 活动初始化
     */
    // 控件初始化
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("更多");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 页面控件
        mUserUploadCapacityImageRv = (RelativeLayout) findViewById(R.id.upload_capacity_layout);
        mUserUploadCapacityImageRv.setOnClickListener(this);
        mCheckHomePageRv = (RelativeLayout) findViewById(R.id.check_homepage_layout);
        mCheckHomePageRv.setOnClickListener(this);
    }

    /*
     * 消息监听
     */
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

    // 控件点击响应
    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            case R.id.upload_capacity_layout:  // 上传资质证书
                Log.i(TAG, "You have clicked the upload_capacity button");
//                intent = new Intent(WorkerMoreActivity.this, UploadImageFTPActivity.class);
//                intent.putExtra(WORKER_ID, mWorkerId);
//                intent.putExtra(UPLOAD_IMAGE_TYPE, UPLOAD_WORKER_CAPACITY_IMAGE);
//                startActivity(intent);
                break;
            case R.id.check_homepage_layout:  //  查看主页
//                intent = new Intent(UserMoreActivity.this, UserHomePageActivity.class);
//                intent.putExtra("user_id", mUserId);
//                startActivity(intent);
                break;
        }
    }
}
