package com.automation.ibinstallationteam.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.UserInfo;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.utils.ftp.FTPUtil;
import com.automation.ibinstallationteam.utils.http.HttpUtil;
import com.automation.ibinstallationteam.widget.dialog.CommonDialog;
import com.automation.ibinstallationteam.widget.dialog.LoadingDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by pengchenghu on 2019/2/22.
 * Author Email: 15651851181@163.com
 * Describe: 安装队伍注册
 */

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "RegisterActivity";
    public static final int SEND_REGISTER_MSG = 101;
    public static final int UPLODA_IMAGE_FAILURED_MSG = 102;
    public static final int REGISTER_SUCCESS_MSG = 103;
    public static final int REGISTER_FAILURED_PHONE_MSG = 104;
    public static final int REGISTER_FAILURED_SERVICE_MSG = 105;

    public static final int TAKE_PHOTO = 1;  // 拍照
    public static final int CHOOSE_PHOTO = 2; // 相册选择
    private ImageView picture; // 上传图片
    private Uri imageUri;
    private Bitmap bitmap;
    private File photo_file;
    private Boolean photo_exist;

    private TextView uploadResult; // 图片上传结果

    private EditText edt_userName; // 注册信息
    private EditText edt_userPhone;
    private EditText edt_userPwd;
    private EditText edt_userPwd_again;

    private Button takePhoto;  // 图片选择方式
    private Button chooseFromAlbum;
    private Button register;

    private CommonDialog mCommonDialog;  // 弹窗
    private LoadingDialog mLoadingDialog;
    private UserInfo userInfo;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_REGISTER_MSG:  // 发送注册请求
                    sendRegister();
                    break;
                case UPLODA_IMAGE_FAILURED_MSG: // 上传图片失败
                    mCommonDialog = initDialog(getString(R.string.pic_failNotice));
                    mLoadingDialog.dismiss();
                    mCommonDialog.show();
                    handler.removeCallbacksAndMessages(null);
                    break;
                case REGISTER_SUCCESS_MSG: // 注册成功
                    mCommonDialog = initDialog(getString(R.string.register_success));
                    mLoadingDialog.dismiss();
                    mCommonDialog.show();
                    break;
                case REGISTER_FAILURED_PHONE_MSG: // 注册失败，手机号已被注册
                    mCommonDialog = initDialog(getString(R.string.register_exist));
                    mLoadingDialog.dismiss();
                    mCommonDialog.show();
                    break;
                case REGISTER_FAILURED_SERVICE_MSG: // 注册失败，服务器异常
                    mCommonDialog = initDialog(getString(R.string.register_back_fail));
                    mLoadingDialog.dismiss();
                    mCommonDialog.show();
                    break;
                default: break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initWidgets();
    }

    // 初始化控件
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(getString(R.string.registInstallTeam_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        takePhoto = (Button) findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(this);
        chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
        chooseFromAlbum.setOnClickListener(this);
        register = findViewById(R.id.btn_regist);
        register.setOnClickListener(this);
        picture = (ImageView) findViewById(R.id.picture);

        edt_userName = findViewById(R.id.edt_register_userName);
        edt_userPhone = findViewById(R.id.edt_register_userPhone);
        edt_userPwd = findViewById(R.id.edt_register_pwd);
        edt_userPwd_again = findViewById(R.id.edt_register_pwd_again);
        photo_exist = false;

        initLoadingDialog();
    }

    /*
     * 消息响应
     */
    // 顶部导航栏
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                RegisterActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // 一般按键消息响应
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take_photo:
                if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO);
                else
                    openCamera();
                break;
            case R.id.choose_from_album:
                if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHOOSE_PHOTO);
                } else {
                    openAlbum();
                }
                break;
            case R.id.btn_regist:
                String password = edt_userPwd.getText().toString();
                String password_2 = edt_userPwd_again.getText().toString();

                if (photo_exist.equals(false)) {
                    ToastUtil.showToastTips(RegisterActivity.this, "请上传身份证图片！");
                } else if (!password.equals(password_2) || password.equals(" ") || password == null) {
                    ToastUtil.showToastTips(RegisterActivity.this, "两次密码输入不一致！");
                    edt_userPwd.getText().clear();
                    edt_userPwd_again.getText().clear();
                } else if (!isMobileNO(edt_userPhone.getText().toString())) {
                    ToastUtil.showToastTips(RegisterActivity.this, "手机号码格式不正确！");
                    edt_userPhone.getText().clear();
                } else if (edt_userName.getText().toString() == null || edt_userName.getText().toString().equals(" ") ) {
                    ToastUtil.showToastTips(RegisterActivity.this, "请填写用户名！");
                    edt_userPhone.getText().clear();
                } else {
                    userInfo = new UserInfo(edt_userName.getText().toString(), edt_userPhone.getText().toString(),
                            edt_userPwd.getText().toString(), "InstallTeam");
                    mLoadingDialog.show();
                    uploadIdentityCardImage();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                        photo_exist = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    else // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                }
                break;
            default: break;
        }
    }
    // 权限申请返回
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                break;
            case TAKE_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openCamera();
                else
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    /*
     * 注册申请
     */
    //注册
    private void sendRegister() {
        HttpUtil.sendRegistOkHttpRequest(new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                //异常情况处理
                ToastUtil.showToastTips(RegisterActivity.this, "网络连接失败！");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                try{
                    JSONObject jsonObject = JSON.parseObject(responseData);
                    String mMessage = jsonObject.getString("message");
                    int msg_what;
                    if(mMessage!=null) {
                        if (mMessage.equals("success"))
                            msg_what = REGISTER_SUCCESS_MSG;
                        else if (mMessage.equals("exist"))
                            msg_what = REGISTER_FAILURED_PHONE_MSG;
                        else
                            msg_what = REGISTER_FAILURED_SERVICE_MSG;
                    }else
                        msg_what = REGISTER_FAILURED_SERVICE_MSG;
                    handler.sendEmptyMessage(msg_what);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, userInfo.getUserName(), userInfo.getUserPassword(), userInfo.getUserPhone(), userInfo.getUserRole());
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
                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                            finish();
                        }else{
                            dialog.dismiss();
                        }
                    }
                }).setTitle("提示");
    }
    // 加载弹窗
    private void initLoadingDialog(){
        mLoadingDialog = new LoadingDialog(RegisterActivity.this, "正在上传...");
        mLoadingDialog.setCancelable(false);
    }

    /*
     * 工具类
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }
    private void openCamera(){
        String filename = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA)
                .format(new Date()) + ".jpg";
        photo_file = new File(getExternalCacheDir(), filename);
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT < 24) // 低于Android 7.0 ，将file转换为uri对象
            imageUri = Uri.fromFile(photo_file);
        else { // FileProvider —— 内容提供器
            imageUri = FileProvider.getUriForFile(this, "com.automation.ibinstallationteam.fileprovider", photo_file);
            openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 添加这一句表示对目标应用临时授权该Uri所代表的文件
            openCameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);// 设置Action为拍照
        }
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
        startActivityForResult(openCameraIntent, TAKE_PHOTO);
    }
    public static boolean isMobileNO(String mobiles) {
        /**
         * 判断字符串是否符合手机号码格式
         * 移动号段: 134,135,136,137,138,139,147,150,151,152,157,158,159,170,178,182,183,184,187,188
         * 联通号段: 130,131,132,145,155,156,170,171,175,176,185,186
         * 电信号段: 133,149,153,170,173,177,180,181,189
         * @param str
         * @return 待检测的字符串
         */
        String telRegex = "^((1[3,5,7,8][0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        } else {
            return mobiles.matches(telRegex);
        }
    }
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }
    // 获取图片地址
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    // 根据图片路径显示图片
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            photo_exist = true;
            bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
            photo_file = new File(imagePath);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * 上传身份证图片至服务器
     */
    // FTP直接上传
    private void uploadIdentityCardImage(){
        final FTPUtil mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
        final String mRemotePath = "photo";
        new Thread(){
            public void run() {
                try {
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
                    String fileName = userInfo.getUserPhone() + ".jpg";
                    mFTPClient.uploadingSingleRenameFile(photo_file, fileName);
                    mFTPClient.closeConnect();  // 关闭连接
                    handler.sendEmptyMessage(SEND_REGISTER_MSG);
                }catch (IOException e){  // 上传文件失败
                    e.printStackTrace();
                    handler.sendEmptyMessage(REGISTER_FAILURED_SERVICE_MSG);
                }
            }
        }.start();
    }
}
