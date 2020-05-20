package com.automation.ibinstallationteam.activity.common;

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
import android.widget.TextView;
import android.widget.Toast;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.activity.manage.image.CheckExampleImgActivity;
import com.automation.ibinstallationteam.activity.manage.image.FinishImgActivity;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.PortionMap;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.automation.ibinstallationteam.utils.ftp.FTPUtil;
import com.automation.ibinstallationteam.widget.ScaleImageView;
import com.automation.ibinstallationteam.widget.dialog.LoadingDialog;
import com.automation.ibinstallationteam.widget.image.SmartImageView;
import com.automation.ibinstallationteam.widget.image.WebImage;
import com.heynchy.compress.CompressImage;
import com.heynchy.compress.compressinterface.CompressLubanListener;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* 多张图片上传
* */

public class MultiImgUploadActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "SingleImgUploadActivity";

    // Handler 消息
    public static final int UPLODA_IMAGE_SUCCESS_MSG = 101;
    public static final int UPLODA_IMAGE_FAILURED_MSG = 102;

    // 页面跳转标志
    public static final int TAKE_LEFT_PHOTO_FROM_CAMERA = 1;  // 照相机
    public static final int TAKE_RIGHT_PHOTO_FROM_CAMERA = 2;  // 照相机

    // 相册位置
    public static final String CAMERA_PATH = Environment.getExternalStorageDirectory() +
            File.separator + Environment.DIRECTORY_DCIM + File.separator+"Camera"+ File.separator;

    // 控件
    private TextView mCheckExampleTv;
    private SmartImageView mLeftUploadImageIv;  // 左图上传
    private Button mLeftUploadBtn;
    private SmartImageView mRightUploadImageIv;  // 右图上传
    private Button mRightUploadBtn;

    // 全局变量
    private String projectId;  // 项目
    private String basketId;  // 吊篮
    private int imageType;  // 图片类型
    private int basketFlag;

    private String mUploadImageType = "提升机";  // 上传图片类型，如提升机、安全锁等；
    private String remoteFileName; // 远程文件名
    private String leftFileName;  // 左图片名
    private File leftPhotoFile ; // 图片文件
    private Uri leftPhotoUrl ; // 图片URL
    private String leftRemoteFileUrl;
    private String rightFileName;  // 右图片名
    private File rightPhotoFile ; // 图片文件
    private Uri rightPhotoUrl ; // 图片URL
    private String rightRemoteFileUrl;

    private List<Bitmap> mWorkPhotos = new ArrayList<>();  // bitmap 位图
    private List<String> mFileNameList = new ArrayList<>(); // 文件名

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
                    ToastUtil.showToastTips(MultiImgUploadActivity.this,
                            "图片上传成功");
                    break;
                case UPLODA_IMAGE_FAILURED_MSG:
                    mLoadingDialog.dismiss();
                    ToastUtil.showToastTips(MultiImgUploadActivity.this,
                            "图片上传成功");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_img_upload);

        if(!isHasPermission()) requestPermission();

        initIntent();
        initWidgets();
        initFTPClient(); // 初始化文件服务器
    }

    /* 初始化
     * */
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText(mUploadImageType);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mCheckExampleTv = (TextView) findViewById(R.id.check_example_img_tv);
        mCheckExampleTv.setOnClickListener(this);
        if(imageType<6)
            mCheckExampleTv.setVisibility(View.VISIBLE);  // 显示示例图片选项（电柜和提升机）
        else
            mCheckExampleTv.setVisibility(View.GONE);  // 不显示示例图片选项

        mLeftUploadImageIv = (SmartImageView) findViewById(R.id.left_image_display_iv);
        mLeftUploadImageIv.setImageUrl(leftRemoteFileUrl, R.mipmap.ic_add_upload_image);
        mLeftUploadImageIv.setOnClickListener(this);
        mLeftUploadImageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(basketFlag==1){
                    ToastUtil.showToastTips(MultiImgUploadActivity.this,
                            "正在审核或已完成的吊篮，无权限修改图片！");
                    return false;
                }
                startCameraActivity("left");  // 长按进入拍摄模式
                return false;
            }
        });
        mLeftUploadBtn = (Button) findViewById(R.id.left_upload_btn);
        mLeftUploadBtn.setOnClickListener(this);

        mRightUploadImageIv = (SmartImageView) findViewById(R.id.right_image_display_iv);
        mRightUploadImageIv.setImageUrl(rightRemoteFileUrl, R.mipmap.ic_add_upload_image);
        mRightUploadImageIv.setOnClickListener(this);
        mRightUploadImageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(basketFlag==1){
                    ToastUtil.showToastTips(MultiImgUploadActivity.this,
                            "正在审核或已完成的吊篮，无权限修改图片！");
                    return false;
                }
                startCameraActivity("right");  // 长按进入拍摄模式
                return false;
            }
        });
        mRightUploadBtn = (Button) findViewById(R.id.right_upload_btn);
        mRightUploadBtn.setOnClickListener(this);

        if(basketFlag==1){
            mLeftUploadBtn.setVisibility(View.INVISIBLE);
            mRightUploadBtn.setVisibility(View.INVISIBLE);
        }

        initLoadingDialog();
    }

    /* 消息响应
     */
    // 一般按钮响应
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.check_example_img_tv:  // 跳转到查看示例图片页面
                Intent intent = new Intent(MultiImgUploadActivity.this, CheckExampleImgActivity.class);
                intent.putExtra("image_type_name", PortionMap.englishPortion.get(imageType));
                startActivity(intent);
                break;
            case R.id.left_image_display_iv:  // 点击图片，查看大图，尚未完成
                getBitmaps("left");
                // 显示dislog
                if (mWorkPhotos.size() > 0) {
                    ScaleImageView scaleImageView = new ScaleImageView(MultiImgUploadActivity.this);
                    scaleImageView.setUrls_and_Bitmaps(mFileNameList, mWorkPhotos, 0);
                    scaleImageView.create();
                }
                break;
            case R.id.left_upload_btn:
                if(leftPhotoFile==null){
                    String tips = "";
                    if (mLeftUploadImageIv.getBitmap() == null) tips = "图片为空，请重新拍摄后上传";
                    else tips = "该图片已上传";
                    ToastUtil.showToastTips(MultiImgUploadActivity.this, tips);
                    break;
                }
                mLoadingDialog.show();
                startSendImage("left");  // 上传图片
                break;
            case R.id.right_image_display_iv:  // 点击图片，查看大图，尚未完成
                getBitmaps("right");
                // 显示dislog
                if (mWorkPhotos.size() > 0) {
                    ScaleImageView scaleImageView1 = new ScaleImageView(MultiImgUploadActivity.this);
                    scaleImageView1.setUrls_and_Bitmaps(mFileNameList, mWorkPhotos, 0);
                    scaleImageView1.create();
                }
                break;
            case R.id.right_upload_btn:
                if(rightPhotoFile==null){
                    String tips = "";
                    if (mRightUploadImageIv.getBitmap() == null) tips = "图片为空，请重新拍摄后上传";
                    else tips = "该图片已上传";
                    ToastUtil.showToastTips(MultiImgUploadActivity.this, tips);
                    break;
                }
                mLoadingDialog.show();
                startSendImage("right");  // 上传图片
                break;
        }
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_LEFT_PHOTO_FROM_CAMERA:        // 拍摄
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(MultiImgUploadActivity.this, "取消了拍照", Toast.LENGTH_SHORT).show();
                    return;
                }
                String photoFilePath = CAMERA_PATH + "IMAGE_"+ leftFileName +".jpg";
                String compressFilePath = CAMERA_PATH + "IMAGE_"+ leftFileName +"_compress.jpg";
                compressImage(photoFilePath, compressFilePath);  // 压缩图片
                Bitmap photo = BitmapFactory.decodeFile(compressFilePath);
                mLeftUploadImageIv.setImageBitmap(photo);  // 显示图片
                myDeleteFile(leftPhotoFile);  // 删除原图
                leftPhotoFile = new File(compressFilePath);  // 加载压缩后的图像

                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), leftPhotoFile.getAbsolutePath(),
                            leftPhotoFile.getName(), null);//图片插入到系统图库
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.parse("file://" + leftPhotoFile.getAbsolutePath()));
                sendBroadcast(intent);
                break;
            case TAKE_RIGHT_PHOTO_FROM_CAMERA:        // 拍摄
                if(resultCode == RESULT_CANCELED){
                    Toast.makeText(MultiImgUploadActivity.this, "取消了拍照", Toast.LENGTH_SHORT).show();
                    return;
                }
                String photoFilePath2 = CAMERA_PATH + "IMAGE_"+ rightFileName +".jpg";
                String compressFilePath2 = CAMERA_PATH + "IMAGE_"+ rightFileName +"_compress.jpg";
                compressImage(photoFilePath2, compressFilePath2);  // 压缩图片
                Bitmap photo2 = BitmapFactory.decodeFile(compressFilePath2);
                mRightUploadImageIv.setImageBitmap(photo2);  // 显示图片
                myDeleteFile(rightPhotoFile);  // 删除原图
                rightPhotoFile = new File(compressFilePath2);  // 加载压缩后的图像
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), rightPhotoFile.getAbsolutePath(),
                            rightPhotoFile.getName(), null);//图片插入到系统图库
                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent2 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent2.setData(Uri.parse("file://" + rightPhotoFile.getAbsolutePath()));
                sendBroadcast(intent2);
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
        projectId = intent.getStringExtra(FinishImgActivity.PROJECT_ID);
        basketId = intent.getStringExtra(FinishImgActivity.BASKET_ID);
        imageType = intent.getIntExtra(FinishImgActivity.IMAGE_TYPE_ID, -1);
        basketFlag = intent.getIntExtra(FinishImgActivity.BASKET_FLAG, 0);
        mRemotePath = "project/" + projectId + "/" + basketId + "/";  // 图片上传地址
        mUploadImageType = PortionMap.chinesePortion.get(imageType);
        remoteFileName = PortionMap.englishPortion.get(imageType);
        leftRemoteFileUrl = AppConfig.FILE_SERVER_YBLIU_PATH + mRemotePath + remoteFileName + "_left.jpg";
        rightRemoteFileUrl = AppConfig.FILE_SERVER_YBLIU_PATH + mRemotePath + remoteFileName + "_right.jpg";
    }
    // 照相机
    public void startCameraActivity(String type) {
        Intent intent =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   //跳转至拍照页面
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        if(type.equals("left")) {
            leftFileName = format.format(date);
            leftPhotoFile = new File(CAMERA_PATH, "IMAGE_" + leftFileName + ".jpg");
            Log.i(TAG, getPackageName() + ".fileprovider");
            leftPhotoUrl = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", leftPhotoFile);
            Log.i(TAG, leftPhotoUrl.toString());
            // 拍照后的保存路径
            intent.putExtra(MediaStore.EXTRA_OUTPUT, leftPhotoUrl);
            startActivityForResult(intent, TAKE_LEFT_PHOTO_FROM_CAMERA);
        }else{
            rightFileName = format.format(date);
            rightPhotoFile = new File(CAMERA_PATH, "IMAGE_" + rightFileName + ".jpg");
            Log.i(TAG, getPackageName() + ".fileprovider");
            rightPhotoUrl = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", rightPhotoFile);
            Log.i(TAG, rightPhotoUrl.toString());
            // 拍照后的保存路径
            intent.putExtra(MediaStore.EXTRA_OUTPUT, rightPhotoUrl);
            startActivityForResult(intent, TAKE_RIGHT_PHOTO_FROM_CAMERA);
        }
    }
    // 压缩图像
    // 进行Luban算法压缩图片
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
    private void startSendImage(String type){
        final File photoFile;
        final String fileName;
        if (type.equals("left")){
            photoFile = leftPhotoFile;
            fileName = remoteFileName + "_left.jpg";
        }else{
            photoFile = rightPhotoFile;
            fileName = remoteFileName + "_right.jpg";
        }

        new Thread() {
            public void run() {
                try {
                    // 上传文件
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
                    mFTPClient.uploadingSingleRenameFile(photoFile, fileName);  // 上传图片
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
        mLoadingDialog = new LoadingDialog(MultiImgUploadActivity.this, "正在上传...");
        mLoadingDialog.setCancelable(false);
    }


    /*
     * 申请权限
     */
    private void requestPermission() {
        XXPermissions.with(MultiImgUploadActivity.this)
                .constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.Group.STORAGE) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.CAMERA)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            onResume();

                        }else {
                            Toast.makeText(MultiImgUploadActivity.this,
                                    "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            Toast.makeText(MultiImgUploadActivity.this, "被永久拒绝授权，请手动授予权限",
                                    Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(MultiImgUploadActivity.this);
                        }else {
                            Toast.makeText(MultiImgUploadActivity.this, "获取权限失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }
    // 是否有权限：摄像头、拨打电话
    private boolean isHasPermission() {
        if (XXPermissions.isHasPermission(MultiImgUploadActivity.this, Permission.Group.STORAGE)
                && XXPermissions.isHasPermission(MultiImgUploadActivity.this, Permission.CAMERA))
            return true;
        return false;
    }

    // 初始化图片位图:直接从缓存中获取
    private void getBitmaps(String type){
        mWorkPhotos.clear();
        mFileNameList.clear();

        String url;
        if (type.equals("left"))
            url = leftRemoteFileUrl;
        else
            url = rightRemoteFileUrl;
        Bitmap bm = WebImage.webImageCache.get(url);
        if (bm != null) {
            mWorkPhotos.add(null);
            mWorkPhotos.set(0, WebImage.webImageCache.get(url));
            mFileNameList.add(mUploadImageType);
        }
    }
}
