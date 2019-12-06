package com.automation.ibinstallationteam.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.Portion;
import com.automation.ibinstallationteam.entity.PortionMap;
import com.automation.ibinstallationteam.widget.image.SmartImageView;

/*
 * 查看示例图片
 */

public class CheckExampleImgActivity extends AppCompatActivity {

    // 控件
    private SmartImageView mExampleImgSIv;

    // 全局变量
    private String mImageName;
    private String mImgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_example_img);

        getIntentInfo();
        initWidgets();
    }

    private void initWidgets(){
        // 顶部导航栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView titleText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        titleText.setText("查看" + PortionMap.chinesePortion.get(PortionMap.englishPortion.indexOf(mImageName)) + "示例图片");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用

        mExampleImgSIv = (SmartImageView) findViewById(R.id.example_img_imageview);
        mExampleImgSIv.setImageUrl(mImgUrl);
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

    /*
     * 本地消息交互
     */
    private void getIntentInfo(){
        Intent intent = getIntent();
        mImageName = intent.getStringExtra("image_type_name");

        mImgUrl = AppConfig.FILE_SERVER_YBLIU_PATH + "examples/" + mImageName + ".jpg";
    }
}
