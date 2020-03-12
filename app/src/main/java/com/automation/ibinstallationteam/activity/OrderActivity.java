package com.automation.ibinstallationteam.activity;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.adapter.MgStateAdapter;
import com.automation.ibinstallationteam.adapter.OrderAdapter;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.Order;
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
import java.util.Set;

import okhttp3.Call;

public class OrderActivity extends AppCompatActivity implements View.OnTouchListener {

    private final static String TAG = "OrderActivity";

    // handler 消息
    private final static int SWITCH_ORDER_STATE_MSG = 101;

    // 页面消息传递
    public final static String PROJECT_ID = "project_id";

    // 顶层布局
    private LinearLayout mTopLayout;
    // 工单状态选择栏
    private GridView mOrderStateGv; // 工单状态
    private List<String> mStateLists; // 状态名称
    private MgStateAdapter mgStateAdapter; //适配器
    private int pre_selectedPosition = 0;

    /* 主体内容部分*/
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    // 工单列表视图
    private RelativeLayout mListRelativeLayout;
    private RecyclerView mOrderListRecyclerView;
    private List<List<Order>>  mOrderSummaryList = new ArrayList<>();
    private List<Order> mOrderSelectedList = new ArrayList<>();
    private OrderAdapter mOrderAdapter;

    // 无工单
    private RelativeLayout mBlankRelativeLayout;
    private TextView mBlankHintTextView;

    // 上下左右滑动监听
    private static final int FLING_MIN_DISTANCE = 50;   //最小距离
    private static final int FLING_MIN_VELOCITY = 0;   //最小速度
    private GestureDetector mGestureDetector;


    // 个人信息相关
    private UserInfo mUserInfo;
    private String mToken;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;

    // 项目相关信息


    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SWITCH_ORDER_STATE_MSG:  // 切换不同状态订单
                    mOrderSelectedList.clear();
                    mOrderSelectedList.addAll(mOrderSummaryList.get(pre_selectedPosition));
                    mOrderAdapter.notifyDataSetChanged();

                    if(mOrderSelectedList.size()==0){  // 暂无工单，则隐藏
                        mListRelativeLayout.setVisibility(View.GONE);
                        mBlankRelativeLayout.setVisibility(View.VISIBLE);
                    }else{
                        mBlankRelativeLayout.setVisibility(View.GONE);
                        mListRelativeLayout.setVisibility(View.VISIBLE);
                    }
                    mSmartRefreshLayout.finishRefresh();  // 结束刷新
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        getUserInfo();
        getProjectInfoFromInternet();
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
        titleText.setText("工单");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        // 状态选择栏初始化
        mOrderStateGv = (GridView) findViewById(R.id.mg_order_state);
        mStateLists = new ArrayList<>();
        initStateList();
        mgStateAdapter = new MgStateAdapter(this, R.layout.item_order_state_switch, mStateLists);
        mgStateAdapter.setSelectedPosition(pre_selectedPosition);
        mOrderStateGv.setAdapter(mgStateAdapter);
        mOrderStateGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // 点击选择不同状态的工单
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pre_selectedPosition = position;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(SWITCH_ORDER_STATE_MSG);
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
                getProjectInfoFromInternet();
            }
        });

        // 无工单
        mBlankRelativeLayout = (RelativeLayout) findViewById(R.id.order_no_avaliable);

        // 工单列表
        mListRelativeLayout = (RelativeLayout) findViewById(R.id.order_avaliable_rl);
        mOrderListRecyclerView = (RecyclerView) findViewById(R.id.order_rv) ;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mOrderListRecyclerView.setLayoutManager(layoutManager);
        mOrderAdapter = new OrderAdapter(this, mOrderSelectedList);
        mOrderListRecyclerView.setAdapter(mOrderAdapter);
        mOrderAdapter.setOnItemClickListener(new OrderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 点击item响应
                Log.i(TAG, "You have clicked the "+position+" item");
                Intent intent = new Intent(OrderActivity.this, BasketActivity.class);
                intent.putExtra(PROJECT_ID, mOrderSelectedList.get(position).getId());
                startActivity(intent);
            }
        });
//        initOrderList();

        // 设置手势监听
        mGestureDetector = new GestureDetector(this, myGestureListener);
        mSmartRefreshLayout.setOnTouchListener(this); // 将主容器的监听交给本activity，本activity再交给mGestureDetector
        mSmartRefreshLayout.setLongClickable(true); // 必需设置这为true 否则也监听不到手势

//        mHandler.sendEmptyMessage(SWITCH_ORDER_STATE_MSG);
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
    private void getProjectInfoFromInternet(){
        BaseOkHttpClient.newBuilder()
                .addHeader("Authorization", mToken)
                .addParam("userId", mUserInfo.getUserId())
                .addParam("projectId", "")
                .addParam("type", 1)
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
        mOrderSummaryList.clear();
        initOrderList();

        JSONObject jsonObject = JSON.parseObject(data);
        Iterator<String> projectIds = jsonObject.keySet().iterator();
        while(projectIds.hasNext()) {
            String projectId = (String) projectIds.next();
            JSONObject projectInfoJsonObject =  JSON.parseObject(jsonObject.getString(projectId));

            Order order = new Order();
            int allNum = projectInfoJsonObject.getIntValue(projectId+"allNum");
            int installSum = projectInfoJsonObject.getIntValue(projectId+"installedSum");
            order.setTotalNum(allNum);
            order.setFinishNum(installSum);
            // 防止后台穿错数据 0310
            if (projectInfoJsonObject.getString(projectId) != null) {
                JSONObject projectObject = JSON.parseObject(projectInfoJsonObject.getString(projectId));
                order.setName(projectObject.getString("projectName"));
                order.setCompletedTime(projectObject.getString("projectStart"));
                order.setId(projectId);
                mOrderSummaryList.get(0).add(order);

                if (allNum != installSum)
                    mOrderSummaryList.get(1).add(order);
                else
                    mOrderSummaryList.get(2).add(order);
            }
        }
        mHandler.sendEmptyMessage(SWITCH_ORDER_STATE_MSG);
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

    /*
     * 初始化状态筛选栏
     */
    private void initStateList(){
        mStateLists.add("全部");
        mStateLists.add("进行中");
        mStateLists.add("已完成");
    }

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
                mHandler.sendEmptyMessage(SWITCH_ORDER_STATE_MSG);

            }else if(x2>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY){
                int tmp_position = pre_selectedPosition - 1;
                pre_selectedPosition = (tmp_position > 0) ? tmp_position : 0;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
                mHandler.sendEmptyMessage(SWITCH_ORDER_STATE_MSG);
            }
            return false;
        };
    };
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }


    /* Other
     * */
    // 初始化工单列表
    private void initOrderList(){
        for(int i=0; i<3; i++) {
            mOrderSummaryList.add(new ArrayList<Order>());
        }

//        // 进行中
//        List<Order> tmpOrders  = new ArrayList<>();
//        Order order1 = new Order("南京碧桂园公司大楼幕墙", 15, 20, "2019/12/10");
//        tmpOrders.add(order1);
//        Order order2 = new Order("南京珠江路金鹰广场幕墙", 10, 12, "2019/12/01");
//        tmpOrders.add(order2);
//        Order order3 = new Order("南京新街口德基广场幕墙", 7, 8, "2019/11/15");
//        tmpOrders.add(order3);
//        mOrderSummaryList.add(tmpOrders);  // 进行中
//
//        // 已完成
//        List<Order> tmp2Orders = new ArrayList<>();
//        Order order4 = new Order("上海莘庄A写字楼幕墙", 16, 16, "2019/11/20");
//        tmp2Orders.add(order4);
//        Order order5 = new Order("南京江宁金鹰广场幕墙", 10, 10, "2019/11/10");
//        tmp2Orders.add(order5);
//        Order order6 = new Order("南京太平大厦幕墙", 28, 28, "2019/10/15");
//        tmp2Orders.add(order6);
//        mOrderSummaryList.add(tmp2Orders);  // 已完成
//
//        mOrderSummaryList.get(0).addAll(tmpOrders); // 全部
//        mOrderSummaryList.get(0).addAll(tmp2Orders); // 全部
    }
}
