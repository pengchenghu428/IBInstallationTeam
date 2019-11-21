package com.automation.ibinstallationteam.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.utils.ftp.FTPUtil;
import com.automation.ibinstallationteam.widget.dialog.LoadingDialog;
import com.automation.ibinstallationteam.widget.image.SmartImageView;
import com.heynchy.compress.CompressImage;
import com.heynchy.compress.compressinterface.CompressLubanListener;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OperateWorkerInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "NewWorkerInfoActivity";

    // Handler 消息
    public static final int UPLODA_IMAGE_SUCCESS_MSG = 101;
    public static final int UPLODA_IMAGE_FAILURED_MSG = 102;

    // 页面跳转标志
    public static final int TAKE_PHOTO_FROM_CAMERA= 2;  // 照相机

    // 相册位置
    public static final String CAMERA_PATH = Environment.getExternalStorageDirectory() +
            File.separator + Environment.DIRECTORY_DCIM + File.separator+"Camera"+ File.separator;

    // 控件
    private EditText mNameEv;
    private EditText mPhoneNumberEv;
    private EditText mIdCardNumberEv;
    private SmartImageView mOperationIv;
    private Button mConfirmBtn;
    private Button mModityBtn;
    private Button mDeleteBtn;

    // 全局变量
    private String operationType; // 操作类型
    private String workerName;
    private String workerPhoneNumber;
    private String workerIdCardNumber;

    private String fileName;  // 图片名
    private String remoteFileName; // 远程文件名
    private String remoteFileUrl;
    private File photoFile ; // 图片文件
    private Uri photoUrl ; // 图片URL

    // FTP 文件服务器
    private FTPUtil mFTPClient;
    private String mRemotePath;
    private LoadingDialog mLoadingDialog;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPLODA_IMAGE_SUCCESS_MSG:
                    mLoadingDialog.dismiss();
                    break;
                case UPLODA_IMAGE_FAILURED_MSG:
                    mLoadingDialog.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_worker_info);

        if(!isHasPermission()) requestPermission();

        initIntent();
        initWidgets();
        initFTPClient(); // 初始化文件服务器
    }

    /*
    * */
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(operationType);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mNameEv = (EditText) findViewById(R.id.worker_name_ev);
        mNameEv.setText(workerName);
        mPhoneNumberEv = (EditText) findViewById(R.id.worker_phone_number_ev);
        mPhoneNumberEv.setText(workerPhoneNumber);
        mIdCardNumberEv = (EditText) findViewById(R.id.worker_id_card_ev);
        mIdCardNumberEv.setText(workerIdCardNumber);
        mOperationIv = (SmartImageView) findViewById(R.id.operation_iv);
        mOperationIv.setImageUrl(remoteFileUrl, R.mipmap.ic_add_upload_image);
        mConfirmBtn = (Button) findViewById(R.id.confirm_worker_info_btn);
        mModityBtn = (Button) findViewById(R.id.modify_worker_info_btn);
        mDeleteBtn = (Button) findViewById(R.id.delete_worker_info_btn);
        if(operationType.contains("添加")){
            mConfirmBtn.setVisibility(View.VISIBLE);
            mModityBtn.setVisibility(View.GONE);
            mDeleteBtn.setVisibility(View.GONE);
        }else{
            mModityBtn.setVisibility(View.VISIBLE);
            mDeleteBtn.setVisibility(View.VISIBLE);
            mConfirmBtn.setVisibility(View.GONE);
        }

        mOperationIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isHasPermission()) requestPermission();
                startCameraActivity();
                return false;
            }
        });
        mConfirmBtn.setOnClickListener(this);
        mModityBtn.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);

        initLoadingDialog();
    }

    /* 按键消息响应
    * */
    // 顶部导航栏
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // 一般按键
    @Override
    public void onClick(View v) {
        String workerName = mNameEv.getText().toString();
        String workerPhoneNumber = mPhoneNumberEv.getText().toString();
        String workerIdCardNumber = mIdCardNumberEv.getText().toString();
        if(workerName.equals("") || workerPhoneNumber.equals("") || workerIdCardNumber.equals("")){
            ToastUtil.showToastTips(OperateWorkerInfoActivity.this, "请确认所有信息均填写完毕");
            return;
        }
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.confirm_worker_info_btn:  // 确认按钮
                mRemotePath = "tempUser/" + workerPhoneNumber + "/";  // 图片上传地址
                remoteFileName = "operation.jpg";
                if(photoFile == null){
                    ToastUtil.showToastTips(OperateWorkerInfoActivity.this, "请选择上传图片");
                    break;
                }
                mLoadingDialog.show();
                startSendImage();
                intent.putExtra(WorkerInfoActivity.WORKER_NAME, workerName);
                intent.putExtra(WorkerInfoActivity.WORKER_PHONE_NUMBER, workerPhoneNumber);
                intent.putExtra(WorkerInfoActivity.WORKER_ID_CARD_NUMBER, workerIdCardNumber);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.modify_worker_info_btn:
                mRemotePath = "tempUser/" + workerPhoneNumber + "/";  // 图片上传地址
                remoteFileName = "operation.jpg";
                if(photoFile != null){
                    ToastUtil.showToastTips(OperateWorkerInfoActivity.this, "该图片已上传");
                    mLoadingDialog.show();
                    startSendImage();
                }
                intent.putExtra(WorkerInfoActivity.OPERATION_IN_FACT, 1);
                intent.putExtra(WorkerInfoActivity.WORKER_NAME, workerName);
                intent.putExtra(WorkerInfoActivity.WORKER_PHONE_NUMBER, workerPhoneNumber);
                intent.putExtra(WorkerInfoActivity.WORKER_ID_CARD_NUMBER, workerIdCardNumber);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.delete_worker_info_btn:
                intent.putExtra(WorkerInfoActivity.OPERATION_IN_FACT, 0 );
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO_FROM_CAMERA:        // 拍摄
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(OperateWorkerInfoActivity.this, "取消了拍照", Toast.LENGTH_SHORT).show();
                    return;
                }
                String photoFilePath = CAMERA_PATH + "IMAGE_"+ fileName +".jpg";
                String compressFilePath = CAMERA_PATH + "IMAGE_"+ fileName +"_compress.jpg";
                compressImage(photoFilePath, compressFilePath);  // 压缩图片
                Bitmap photo = BitmapFactory.decodeFile(compressFilePath);
                mOperationIv.setImageBitmap(photo);  // 显示图片
                myDeleteFile(photoFile);  // 删除原图
                photoFile = new File(compressFilePath);  // 加载压缩后的图像

                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), photoFile.getAbsolutePath(),
                            photoFile.getName(), null);//图片插入到系统图库
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.parse("file://" + photoFile.getAbsolutePath()));
                sendBroadcast(intent);
                break;
            default: break;
        }
    }

    /*
     * 工具类
     */
    // 页面传递数据
    private void initIntent(){
        Intent intent = getIntent();
        int operation_type = intent.getIntExtra(WorkerInfoActivity.OPERATION_TYPE, -1);
        if (operation_type==0) {
            operationType = "添加新员工信息";
        } else if(operation_type==1) {
            operationType = "修改员工信息";
            workerName = intent.getStringExtra(WorkerInfoActivity.WORKER_NAME);
            workerPhoneNumber = intent.getStringExtra(WorkerInfoActivity.WORKER_PHONE_NUMBER);
            workerIdCardNumber = intent.getStringExtra(WorkerInfoActivity.WORKER_ID_CARD_NUMBER);
            mRemotePath = "tempUser/" + workerPhoneNumber + "/";  // 图片上传地址
            remoteFileName = "operation.jpg";
            remoteFileUrl = AppConfig.FILE_SERVER_YBLIU_PATH + mRemotePath + remoteFileName;
        }
        else finish();
    }
    // 照相机
    public void startCameraActivity() {
        Intent intent =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   //跳转至拍照页面
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        fileName = format.format(date);
        photoFile = new File(CAMERA_PATH, "IMAGE_"+ fileName + ".jpg");
        Log.i(TAG,getPackageName() + ".fileprovider");
        photoUrl = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
        Log.i(TAG,photoUrl.toString());
        // 拍照后的保存路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUrl);
        startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
    }
    // Luban算法压缩图片
    private void compressImage(final String filePath, final String savePath){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CompressImage.getInstance().imageLubrnCompress(filePath, savePath, new CompressLubanListener() {
                    @Override
                    public void onCompressLubanSuccessed(String imgPath, Bitmap bitmap) {
                        /**
                         * 返回值: imgPath----压缩后图片的绝对路径
                         *        bitmap----返回的图片
                         */
                        Log.i(TAG, "Compress Success:" + imgPath);
                    }

                    @Override
                    public void onCompressLubanFailed(String imgPath, String msg) {
                        /**
                         * 返回值: imgPath----原图片的绝对路径
                         *        msg----返回的错误信息
                         */
                        Log.i(TAG, "Compress Failed:"+ imgPath + " " + msg);
                    }

                });
            }
        });
    }
    // 删除压缩的图片
    private void myDeleteFile (File file){
        String path = file.getPath();
        // 删除系统缩略图
        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{path});
        // 删除手机中图片
        file.delete();
    }

    /*
     * 上传身份证图片至服务器
     */
    private void startSendImage(){
        new Thread() {
            public void run() {
                try {
                    // 上传文件
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
                    mFTPClient.uploadingSingleRenameFile(photoFile, remoteFileName);  // 上传图片
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(UPLODA_IMAGE_SUCCESS_MSG);
                } catch (IOException e) {
                    // 上传文件失败
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(UPLODA_IMAGE_FAILURED_MSG);
                }
            }
        }.start();
    }
    // FTP 初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }
    // 加载弹窗
    private void initLoadingDialog(){
        mLoadingDialog = new LoadingDialog(OperateWorkerInfoActivity.this, "正在上传...");
        mLoadingDialog.setCancelable(false);
    }


    /*
     * 权限申请
     */
    /*
     * 申请权限
     */
    private void requestPermission() {
        XXPermissions.with(OperateWorkerInfoActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CAMERA)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();

                        }else {
                            Toast.makeText(OperateWorkerInfoActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(OperateWorkerInfoActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(OperateWorkerInfoActivity.this);
                        }else {
                            Toast.makeText(OperateWorkerInfoActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }
    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(OperateWorkerInfoActivity.this, Permission.Group.STORAGE)
                && XXPermissions.isHasPermission(OperateWorkerInfoActivity.this, Permission.CAMERA))
            return true;
        return false;
    }
}
