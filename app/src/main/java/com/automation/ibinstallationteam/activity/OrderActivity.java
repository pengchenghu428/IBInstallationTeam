package com.automation.ibinstallationteam.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Toast;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.adapter.MgStateAdapter;
import com.automation.ibinstallationteam.entity.UserInfo;
import com.automation.ibinstallationteam.utils.ToastUtil;
import com.scwang.smartrefresh.header.BezierCircleHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity implements View.OnTouchListener {

    private final static String TAG = "OrderActivity";

    // 顶层布局
    private LinearLayout mTopLayout;
    // 吊篮状态选择栏
    private GridView mBasketStateGv; // 吊篮状态
    private List<String> mStateLists; // 状态名称
    private MgStateAdapter mgStateAdapter; //适配器
    private int pre_selectedPosition = 0;

    /* 主体内容部分*/
    private SmartRefreshLayout mSmartRefreshLayout; // 下拉刷新
    // 工单列表视图
    private RelativeLayout mListRelativeLayout;
    private RecyclerView mOrderListRecyclerView;

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

    /*
     * 消息函数
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

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
        mBasketStateGv = (GridView) findViewById(R.id.mg_order_state);
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
                mSmartRefreshLayout.finishRefresh();
            }
        });

        // 设置手势监听
        mGestureDetector = new GestureDetector(this, myGestureListener);
        mSmartRefreshLayout.setOnTouchListener(this); // 将主容器的监听交给本activity，本activity再交给mGestureDetector
        mSmartRefreshLayout.setLongClickable(true); // 必需设置这为true 否则也监听不到手势
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

            }else if(x2>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY){
                int tmp_position = pre_selectedPosition - 1;
                pre_selectedPosition = (tmp_position > 0) ? tmp_position : 0;
                mgStateAdapter.setSelectedPosition(pre_selectedPosition);
            }
            return false;
        };
    };
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }


}
