package com.automation.ibinstallationteam.activity.manage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.adapter.BasketAdapter;
import com.automation.ibinstallationteam.adapter.MgStateAdapter;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.Basket;
import com.automation.ibinstallationteam.entity.UserInfo;
import com.automation.ibinstallationteam.utils.okhttp.BaseCallBack;
import com.automation.ibinstallationteam.utils.okhttp.BaseOkHttpClient;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;

public class BasketActivity extends AppCompatActivity implements View.OnTouchListener{

    private final static String TAG = "BasketActivity";

    // Handler 消息
    private final static int SWITCH_BASKET_STATE_MSG = 101;

    // 页面消息传递
    public final static String PROJECT_ID = "project_id";
    public final static String BASKET_ID = "basket_id";
    public final static String BASKET_FLAG = "basket_flag";
    public final static String BASKET_STATE = "basket_state";
    public final static String SITE_ID = "site_id";

    // 吊篮状态选择
    private GridView mBasketStateGv;  // 吊篮状态
    private List<String> mStateLists; // 状态名称
    private MgStateAdapter mgStateAdapter; // 适配器
    private int pre_selectedPosition = 0;

    /* 主体内容部分*/
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    // 工单列表视图
    private RelativeLayout mListRelativeLayout;
    private RecyclerView mBasketListRecyclerView;
    private List<List<Basket>>  mBasketSummaryList = new ArrayList<>();
    private List<Basket> mBasketSelectedList = new ArrayList<>();
    private BasketAdapter mBasketAdapter;

    // 无d吊篮
    private RelativeLayout mBlankRelativeLayout;
    private TextView mBlankHintTextView;

    // 上下左右滑动监听
    private static final int FLING_MIN_DISTANCE = 50;   //最小距离
    private static final int FLING_MIN_VELOCITY = 0;   //最小速度
    private GestureDetector mGestureDetector;

    // 业务数据
    private String mProjectId;

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
                case SWITCH_BASKET_STATE_MSG:  // 切换吊篮状态
                    mBasketSelectedList.clear();
                    mBasketSelectedList.addAll(mBasketSummaryList.get(pre_selectedPosition));
                    mBasketAdapter.notifyDataSetChanged();

                    if(mBasketSelectedList.size()==0){  // 暂无工单，则隐藏
                        mListRelativeLayout.setVisibility(View.GONE);
                        mBlankRelativeLayout.setVisibility(View.VISIBLE);
                    }else{
                        mBlankRelativeLayout.setVisibility(View.GONE);
                        mListRelativeLayout.setVisibility(View.VISIBLE);
                    }
                    mSmartRefreshLayout.finishRefresh();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        getUserInfo();
        getIntentInfo();
        getBasketInfoFromInternet();
        initWidgets();
    }

    /*
     * 页面初始化
     */
    // 控件初始化
    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("吊篮管理");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 状态选择栏初始化
        mBasketStateGv = (GridView) findViewById(R.id.mg_basket_state);
        mStateLists = new ArrayList<>();
        initStateList();
        mgStateAdapter = new MgStateAdapter(this, R.layout.item_order_state_switch, mStateLists);
        mgStateAdapter.setSelectedPosition(pre_selectedPosition);
        mBasketStateGv.setAdapter(mgStateAdapter);
        mBasketStateGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // 点击选择不同状态的工单
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pre_selectedPosition = position;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);
            }
        });

        /*
         主体内容部分
          */
        // 下拉刷新
        mSmartRefreshLayout = (SmartRefreshLayout)findViewById(R.id.smart_refresh_layout);
        mSmartRefreshLayout.setRefreshHeader(new BezierCircleHeader(this)); // 设置 Header 为 贝塞尔雷达 样式
        mSmartRefreshLayout.setPrimaryColorsId(R.color.smart_loading_background_color);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() { // 添加下拉刷新监听
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getBasketInfoFromInternet();
            }
        });

        // 无工单
        mBlankRelativeLayout = (RelativeLayout) findViewById(R.id.basket_no_avaliable);

        // 工单列表
        mListRelativeLayout = (RelativeLayout) findViewById(R.id.basket_avaliable_rl);
        mBasketListRecyclerView = (RecyclerView) findViewById(R.id.basket_rv) ;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mBasketListRecyclerView.setLayoutManager(layoutManager);
        mBasketAdapter = new BasketAdapter(this, mBasketSelectedList);
        mBasketListRecyclerView.setAdapter(mBasketAdapter);
        mBasketAdapter.setOnItemClickListener(new BasketAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击item响应
                Log.i(TAG, "You have clicked the "+position+" item");
                Intent intent = new Intent(BasketActivity.this, InstallManageActivity.class);
                intent.putExtra(PROJECT_ID, mProjectId);
                intent.putExtra(BASKET_ID, mBasketSelectedList.get(position).getId());
                intent.putExtra(BASKET_FLAG, pre_selectedPosition);
                intent.putExtra(BASKET_STATE, mBasketSelectedList.get(position).getBasketState());
                intent.putExtra(SITE_ID, mBasketSelectedList.get(position).getSiteId());
                startActivity(intent);
            }
        });
//        initBasketList();

        // 设置手势监听
        mGestureDetector = new GestureDetector(this, myGestureListener);
        mSmartRefreshLayout.setOnTouchListener(this); // 将主容器的监听交给本activity，本activity再交给mGestureDetector
        mSmartRefreshLayout.setLongClickable(true); // 必需设置这为true 否则也监听不到手势

//        mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: // 返回按钮
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * 后台通信
     */
    private void getBasketInfoFromInternet(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .addParam("projectId", mProjectId)
                .addParam("type", 2)  // 2：所有吊篮状态
                .get()
                .url(AppConfig.GET_PROJECT_BY_INSTALLER)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        String data = o.toString();
                        JSONObject jsonObject = JSON.parseObject(data);
                        boolean isLogin = jsonObject.getBooleanValue("isLogin");
                        if(isLogin)
                            parseProjectDetails(jsonObject.getString("info"));

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
    private void parseProjectDetails(String data){
        mBasketSummaryList.clear();
        initBasketList();

        JSONObject jsonObject = JSON.parseObject(data);
        Iterator<String> basketIds = jsonObject.keySet().iterator();
        while(basketIds.hasNext()) {
            String basketId = basketIds.next();

            JSONObject basketObject = JSON.parseObject(jsonObject.getString(basketId));
            JSONObject basketInfo = JSON.parseObject(basketObject.getString(basketId));
            Basket basket = new Basket();
            basket.setId(basketId);
            basket.setWorkerInfo(basketObject.getIntValue(basketId+"_userState"));  // 用户信息 0 未完成 1 完成
            basket.setDeviceBound(basketObject.getIntValue(basketId+"_deviceState"));  // 设备绑定 0 未完成 1 完成
            basket.setFinishImg( basketInfo.getIntValue("pic_flag"));  // 图片上传完整 1 完成 0 未完成
            basket.setBasketState(basketObject.getIntValue(basketId+"_state"));
            basket.setSiteId(basketObject.getString(basketId+"_siteNo"));
            basket.setProjectId(mProjectId);

            int flag = basketInfo.getIntValue("flag");  // flag: 0 进行中 1 未完成
            if(flag==0) mBasketSummaryList.get(0).add(basket);
            else if(flag==1) mBasketSummaryList.get(1).add(basket);
        }
        mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);
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
        mProjectId = intent.getStringExtra(OrderActivity.PROJECT_ID);
    }

    /*
     * 生命周期函数
     */
    protected void onResume(){
        getBasketInfoFromInternet();
        super.onResume();
    }

    /* 手势监听
     */
    /*
     * 手势监听类
     */
    GestureDetector.SimpleOnGestureListener myGestureListener = new GestureDetector.SimpleOnGestureListener(){
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x = e1.getX()-e2.getX();
            float x2 = e2.getX()-e1.getX();

            if(x>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY){
                int tmp_position = pre_selectedPosition + 1;
                pre_selectedPosition = (tmp_position < mStateLists.size()) ? tmp_position : (mStateLists.size() - 1);
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);

            }else if(x2>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY){
                int tmp_position = pre_selectedPosition - 1;
                pre_selectedPosition = (tmp_position > 0) ? tmp_position : 0;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(SWITCH_BASKET_STATE_MSG);
            }
            return false;
        };
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /*Others
     */
    private void initStateList(){
        mStateLists.add("进行中");
        mStateLists.add("已完成");
    }
    private void initBasketList(){
        for (int i=0; i<2; i++){
            mBasketSummaryList.add(new ArrayList<Basket>());
        }
//        // 进行中
//        List<Basket> tmpBaskets  = new ArrayList<>();
//        Basket basket1 = new Basket("201906220001", false, false, false);
//        tmpBaskets.add(basket1);
//        Basket basket2 = new Basket("201906220002", true, false, false);
//        tmpBaskets.add(basket2);
//        Basket basket3 = new Basket("201906220003", true, true, false);
//        tmpBaskets.add(basket3);
//        mBasketSummaryList.add(tmpBaskets);  // 进行中
//
//        // 已完成
//        List<Basket> tmp2Baskets  = new ArrayList<>();
//        Basket basket4 = new Basket("201901015001", true, true, true);
//        tmp2Baskets.add(basket4);
//        Basket basket5 = new Basket("201901015001", true, true, true);
//        tmp2Baskets.add(basket5);
//        Basket basket6 = new Basket("201901015001", true, true, true);
//        tmp2Baskets.add(basket6);
//        mBasketSummaryList.add(tmp2Baskets);  // 进行中
    }

}
