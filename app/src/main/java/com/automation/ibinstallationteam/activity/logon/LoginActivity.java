package com.automation.ibinstallationteam.activity.logon;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.activity.common.ITParimaryActivity;
import com.automation.ibinstallationteam.entity.UserInfo;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.utils.http.HttpUtil;
import com.automation.ibinstallationteam.widget.dialog.CommonDialog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by pengchenghu on 2019/2/22.
 * Author Email: 15651851181@163.com
 * Describe: 安装队伍登录
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "LoginActivity";
    private final static int  LOGONIN_SUCCESS = 100;
    private final static int LOGONIN_REGISTER_NOT_ALLOW = 101;
    private final static int LOGONIN_USER_NOT_EXIST = 102;
    private final static int LOGONIN_PASSWORD_ERROR = 103;
    private final static int LOGON_WITH_ERROR_ROLE = 104;

    private SharedPreferences pref;  // 本地记录文件
    private SharedPreferences.Editor editor;

    private EditText accountEdit;  // 账号密码输入框->手机号码
    private EditText passwordEdit;

    private Button login;  // 登录注册按钮
    private Button to_register;

    private CheckBox rememberPass;  // 记住密码
    private CheckBox autoLogin;  // 自动登录
    private int shareState = 0;  // 本地存储状态

    private UserInfo mUserInfo;  // 账号信息
    private String mAccount;
    private String mPassword;

    private CommonDialog mCommonDialog; // 弹窗

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGONIN_SUCCESS:  // 登陆成功
                    startActivity(new Intent(LoginActivity.this, ITParimaryActivity.class));
                    finish();  // 销毁本活动
                    break;
                case LOGONIN_REGISTER_NOT_ALLOW: // 注册未审核
                    mCommonDialog = initDialog(getString(R.string.dialog_login_audit));
                    mCommonDialog.show();
                    break;
                case LOGONIN_USER_NOT_EXIST: // 用户名不存在
                    mCommonDialog = initDialog(getString(R.string.dialog_login_audit_fail));
                    mCommonDialog.show();
                    break;
                case LOGONIN_PASSWORD_ERROR: // 密码错误
                    mCommonDialog = initDialog(getString(R.string.dialog_login_fail));
                    mCommonDialog.show();
                    break;
                case LOGON_WITH_ERROR_ROLE:
                    mCommonDialog = initDialog(getString(R.string.dialog_error_role));
                    mCommonDialog.show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initWidgets();
    }

    // 初始化控件
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("登录");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置返回键可用

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();
        accountEdit =  findViewById(R.id.text_input_username);
        passwordEdit = findViewById(R.id.text_input_password);
        rememberPass = findViewById(R.id.remember_pass);
        autoLogin = findViewById(R.id.auto_login);
        login =  findViewById(R.id.Login_Button);
        to_register = findViewById(R.id.To_Register);

        if (pref.getBoolean("remember_password", false)) {  // 是否记住密码
            //将账号和密码都设置到文本框中
            mAccount = pref.getString("userPhone", "");
            mPassword = pref.getString("password", "");
            accountEdit.setText(mAccount);
            accountEdit.setSelection(mAccount.length());//光标定位到最后
            passwordEdit.setText(mPassword);
        }

        to_register.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    /*
     * 消息响应
     */
    // 顶部导航栏消息响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                LoginActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // 一般按钮点击响应
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Login_Button:  // 登录按钮
                mAccount = accountEdit.getText().toString();
                mPassword = passwordEdit.getText().toString();
                if(mAccount.equals("") || mPassword.equals("")) {
                    ToastUtil.showToastTips(LoginActivity.this, "账户或密码不能为空");
                    break;
                }
                loginHttp();
                break;
            case R.id.To_Register:  // 注册按钮
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
            default:break;
        }
    }

    /*
     * 网络通信
     */
    private void loginHttp(){
        HttpUtil.sendLoginOkHttpRequest(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                ToastUtil.showToastTips(LoginActivity.this, "网络连接失败！");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() != 200){
                    ToastUtil.showToastTips(LoginActivity.this, "网络连接超时,请稍后重试！");
                }
                // 返回服务器数据
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    boolean isLogin = jsonObject.getBooleanValue("isLogin");
                    String registerState = jsonObject.getString("registerState");
                    String userString = jsonObject.getString("userInfo");
                    mUserInfo =JSON.parseObject(userString,UserInfo.class);
                    if (isLogin){  // 登录成功
                        // 权限限制
                        if (!mUserInfo.getUserRole().equals("InstallTeam")){
                            mHandler.sendEmptyMessage(LOGON_WITH_ERROR_ROLE);
                            return;
                        }

                        String token = jsonObject.getString("token");
                        saveTokenPref(token); // 保存用户状态
                        mHandler.sendEmptyMessage(LOGONIN_SUCCESS);
                    }else{ // 登录失败
                        switch (registerState){
                            case "1":
                                mHandler.sendEmptyMessage(LOGONIN_REGISTER_NOT_ALLOW);
                                break;
                            case "2":
                                mHandler.sendEmptyMessage(LOGONIN_USER_NOT_EXIST);
                                break;
                            case "3":
                                mHandler.sendEmptyMessage(LOGONIN_PASSWORD_ERROR);
                                break;
                            default:
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, mAccount, mPassword);
    }

    /*
     * 数据交互
     */
    // 保存自动登录数据
    private void saveTokenPref(String token) {
        editor.putBoolean("auto_login", true);  // 自动登录
        if(rememberPass.isChecked())  // 记住密码
            editor.putBoolean("remember_password", true);
        else
            editor.putBoolean("remember_password", false);

        editor.putString("userId",mUserInfo.getUserId());  // 用户数据
        editor.putString("userPhone", mAccount);
        editor.putString("password", mPassword);
        editor.putString("loginToken",token);
        editor.putString("userName",mUserInfo.getUserName());
        editor.putString("userRole",mUserInfo.getUserRole());
        editor.commit();
    }

    /*
     * 提示弹框
     */
    private CommonDialog initDialog(String mMsg){
        return new CommonDialog(this, R.style.dialog, mMsg,
                new CommonDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if(confirm){
                            dialog.dismiss();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }
}
