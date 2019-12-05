package com.automation.ibinstallationteam.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.adapter.PortionAdapter;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.Portion;
import com.automation.ibinstallationteam.entity.PortionMap;
import com.automation.ibinstallationteam.utils.ftp.FTPUtil;
import com.automation.ibinstallationteam.widget.SmartGridView;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* 完工图片上传
 */

public class FinishImgActivity extends AppCompatActivity {

    private static final String TAG = "FinishImgActivity";

    // 消息处理变量
    private static final int UPDATE_IMAGE_STATE_MSG = 101;

    // 页面跳转全局变量
    public static final String PROJECT_ID = "project_id";  // 项目ID
    public static final String BASKET_ID = "basket_id";  // 吊篮ID
    public static final String IMAGE_TYPE_ID = "image_type_id";  // 上传图片类型

    // 全局变量
    private String projectId = "201910110001";
    private String basketId = "201910110001";

    // 控件声明
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    private SmartGridView mPortionGv;  // 部件网格控件

    // var switch gridview
    private List<Portion> mPortions;  // 部件变量列表
    private PortionAdapter mPortionAdapter;  // 部件变量适配器

    // FTP 文件服务器
    private FTPUtil mFTPClient;
    private String mRemotePath;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_IMAGE_STATE_MSG:
                    mPortionAdapter.notifyDataSetChanged();
                    break;
                default: break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_img);

        getIntentData();
        initWidgets();
        initFTPClient();
    }

    /* 初始化控件
    * */
    private void initWidgets(){
        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(  //设置 Header 为 贝塞尔雷达 样式
                new BezierCircleHeader(this));
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                mSmartRefreshLayout.finishRefresh();
            }
        });

        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("完工图片");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // GridView: 部件变量状态显示
        mPortionGv = (SmartGridView) findViewById(R.id.main_portion_gv);  // 获取资源控件
        initPortionList();    // 初始化列表内容
        mPortionAdapter = new PortionAdapter(FinishImgActivity.this,
                R.layout.item_portion, mPortions);  // 初始化适配器
        mPortionGv.setAdapter(mPortionAdapter);  // 装载适配器
        mPortionGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 4){
                    // 上传单张图片
                    Intent intent = new Intent(FinishImgActivity.this, SingleImgUploadActivity.class);
                    intent.putExtra(PROJECT_ID, projectId);
                    intent.putExtra(BASKET_ID, basketId);
                    intent.putExtra(IMAGE_TYPE_ID, position);
                    startActivity(intent);
                }else{
                    // 上传左、右图片
                    Intent intent = new Intent(FinishImgActivity.this, MultiImgUploadActivity.class);
                    intent.putExtra(PROJECT_ID, projectId);
                    intent.putExtra(BASKET_ID, basketId);
                    intent.putExtra(IMAGE_TYPE_ID, position);
                    startActivity(intent);
                }
            }
        });
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

    /*
     * 后台通信
     */
    // 检查文件是否存在
    private void checkImageExist(){
        mRemotePath = "project/" + projectId + "/" + basketId;  // 图片上传地址
        new Thread() {
            public void run() {
                try {
                    // 上传文件
                    mFTPClient.openConnect();  // 建立连接
                    mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
                    List<String>  filenames = mFTPClient.listCurrentFileNames();
                    for (int idx=0; idx<PortionMap.englishPortion.size(); idx++){
                        if (filenames.contains(PortionMap.englishPortion.get(idx) + ".jpg")){
                            mPortions.get(idx).setState(1);
                            continue;
                        }
                        if(filenames.contains(PortionMap.englishPortion.get(idx) + "_left.jpg") &&
                                filenames.contains(PortionMap.englishPortion.get(idx) + "_right.jpg")){
                            mPortions.get(idx).setState(1);
                            continue;
                        }
                        mPortions.get(idx).setState(0);
                    }
                    mFTPClient.closeConnect();  // 关闭连接
                    mHandler.sendEmptyMessage(UPDATE_IMAGE_STATE_MSG);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    // FTP 初始化
    private void initFTPClient(){
        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
    }
    /*
     * 获取页面传递消息
     */
    private void getIntentData(){
        Intent intent = getIntent();
        projectId = intent.getStringExtra(InstallManageActivity.PROJECT_ID);
        basketId = intent.getStringExtra(InstallManageActivity.BASKET_ID);
    }

    /*
     * 生命周期
     */
    @Override
    public void onResume(){
        super.onResume();
        checkImageExist();
    }

    /*
     * 其它函数
     */
    // 初始化部件列表
    private void initPortionList(){
        mPortions = new ArrayList<>();
        Portion electricalBox = new Portion("电柜", R.mipmap.ic_electrical_box, 0);
        mPortions.add(electricalBox);
        Portion camera = new Portion("摄像头", R.mipmap.ic_camera, 0);
        mPortions.add(camera);
        Portion safeRope = new Portion("安全绳", R.mipmap.ic_safe_rope, 0);
        mPortions.add(safeRope);
        Portion cable = new Portion("电缆", R.mipmap.ic_cable, 0);
        mPortions.add(cable);
        Portion elevator = new Portion("提升机", R.mipmap.ic_elevator, 0);
        mPortions.add(elevator);
        Portion safeLock = new Portion("安全锁", R.mipmap.ic_safe_lock, 0);
        mPortions.add(safeLock);
        Portion mainSteel = new Portion("主钢丝", R.mipmap.ic_main_steel, 0);
        mPortions.add(mainSteel);
        Portion sideSteel = new Portion("副钢丝", R.mipmap.ic_side_steel, 0);
        mPortions.add(sideSteel);
        Portion heavyPunch = new Portion("重锤", R.mipmap.ic_heavy_punch, 0);
        mPortions.add(heavyPunch);
        Portion limitPosition = new Portion("上限位器", R.mipmap.ic_limit_position, 0);
        mPortions.add(limitPosition);
        Portion weighingMachine = new Portion("称重器", R.mipmap.ic_weighing_machine, 0);
        mPortions.add(weighingMachine);
        Portion bigArm = new Portion("大臂", R.mipmap.ic_big_arm, 0);
        mPortions.add(bigArm);
        Portion balanceWeight = new Portion("配重", R.mipmap.ic_balance_weight, 0);
        mPortions.add(balanceWeight);
    }
}
