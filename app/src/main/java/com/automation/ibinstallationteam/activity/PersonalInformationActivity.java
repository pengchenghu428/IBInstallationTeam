package com.automation.ibinstallationteam.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.entity.UserInfo;

/**
 * Created by pengchenghu on 2019/10/27.
 * Author Email: 15651851181@163.com
 * Describe: 个人信息页面
 */

public class PersonalInformationActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "PersonalInformation";

    // 控件
    // 账号信息
    private RelativeLayout mUserHeadRv;
    private ImageView mUserHeadIv; // 头像
    private RelativeLayout mUserIdRv; // 工号
    private TextView mUserIdTv;
    private RelativeLayout mUserNameRv; // 用户名
    private TextView mUserNameTv;
    private RelativeLayout mUserRoleRv; // 账户类型
    private TextView mUserRoleTv;
    private RelativeLayout mUserPhoneRv; // 手机号
    private TextView mUserPhoneTv;
    private RelativeLayout mUserPasswordRv; // 密码
    private RelativeLayout mUserQRcodeRv; // 二维码名片
    private RelativeLayout mUserMoreRv; // 更多

    // 个人信息
    private UserInfo mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        mUserInfo = (UserInfo) getIntent().getExtras().get("userInfo");

        initWidgetResource();
    }

    /*
     * 控件初始化
     */
    private void initWidgetResource(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("个人信息");
        titleText.setText("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 个人信息栏
        mUserHeadRv = (RelativeLayout) findViewById(R.id.user_head_layout);
        mUserHeadRv.setOnClickListener(this);
        mUserIdRv = (RelativeLayout) findViewById(R.id.user_id_layout);
        mUserIdTv = (TextView) findViewById(R.id.user_id_textview);
        mUserHeadIv = (ImageView) findViewById(R.id.user_head_imageview);
        mUserHeadIv.setOnClickListener(this);
        mUserNameRv = (RelativeLayout) findViewById(R.id.user_name_layout);
        mUserNameRv.setOnClickListener(this);
        mUserNameTv = (TextView) findViewById(R.id.user_name_textview);
        mUserRoleRv = (RelativeLayout) findViewById(R.id.user_role_layout);
        mUserRoleTv = (TextView) findViewById(R.id.user_role_textview);
        mUserPhoneRv = (RelativeLayout) findViewById(R.id.user_phone_layout);
        mUserPhoneTv = (TextView) findViewById(R.id.user_phone_textview);
        mUserPasswordRv = (RelativeLayout) findViewById(R.id.user_password_layout);
        mUserPasswordRv.setOnClickListener(this);
        mUserQRcodeRv = (RelativeLayout) findViewById(R.id.user_qrcode_layout);
        mUserQRcodeRv.setOnClickListener(this);
        mUserMoreRv = (RelativeLayout) findViewById(R.id.user_more_layout);
        mUserMoreRv.setOnClickListener(this);

        // 信息更新
        mUserIdTv.setText(mUserInfo.getUserId());
        mUserNameTv.setText(mUserInfo.getUserName());
        switch(mUserInfo.getUserRole()){
            case "InstallTeam":
                mUserRoleTv.setText("安装队伍");
                break;
            default:break;
        }
        mUserPhoneTv.setText(mUserInfo.getUserPhone());
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
            case R.id.user_head_layout:  // 更换头像
                Log.i(TAG, "You have clicked the user_head_layout");
                break;
            case R.id.user_head_imageview:  // 查看大图
                Log.i(TAG, "You have clicked the user_head_image");
                break;
            case R.id.user_name_layout:  // 更换用户名
                Log.i(TAG, "You have clicked the user_name_layout");
                break;
            case R.id.user_password_layout:  // 更换密码
                Log.i(TAG, "You have clicked the user_password_layout");
                break;
            case R.id.user_qrcode_layout:  // 点击二维码名片
                Log.i(TAG, "You have clicked the user_qrcode_layout");
                intent = new Intent(PersonalInformationActivity.this, QRcodeCardActivity.class);
                intent.putExtra("userId", mUserInfo.getUserId());
                intent.putExtra("userRole", mUserInfo.getUserRole());
                startActivity(intent);
                break;
            case R.id.user_more_layout:  // 更多按钮
                Log.i(TAG, "You have clicked the user_more_layout");
                intent = new Intent(PersonalInformationActivity.this, UserMoreActivity.class);
                intent.putExtra("user_id", mUserInfo.getUserId());
                startActivity(intent);
                break;
        }
    }
}
