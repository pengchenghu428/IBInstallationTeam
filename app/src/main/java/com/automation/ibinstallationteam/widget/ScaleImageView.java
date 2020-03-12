package com.automation.ibinstallationteam.widget;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/2/25.
 * Author Email: 15651851181@163.com
 * Describe: 大图查看器
 */
public class ScaleImageView {
    private byte status;//用来表示当前大图查看器的状态

    private AppCompatActivity activity;

    private List<String> urls;
    private List<Bitmap> bitmaps;

    private int selectedPosition;//表示当前被选中的ViewPager的item的位置

    private Dialog dialog;//用于承载整个大图查看器的Dialog

    private RelativeLayout relativeLayout;
    private ImageView close; // 返回图标
    private TextView imageDate; // 日期
    private TextView imageOthers; // 时间和地点
    private TextView imageCount; // 显示页数
    private ViewPager viewPager; // 滑动显示

    private List<View> views; // viewpager适配器的数据源
    private ScaleImageAdapter adapter;

    private int startPosition;//打开大图查看器时，想要查看的ViewPager的item的位置

    public ScaleImageView(AppCompatActivity activity) {
        this.activity = activity;
        initWidgets();
    }

    // 设置数据
    public void setUrls_and_Bitmaps(List<String> urls, List<Bitmap> bitmaps, int startPosition){
        if(this.urls == null) this.urls = new ArrayList<>();  // 字符串
        else this.urls.clear();
        this.urls.addAll(urls);

        if(this.bitmaps == null) this.bitmaps = new ArrayList<>();  // 位图
        else this.bitmaps.clear();
        this.bitmaps.addAll(bitmaps);

        this.startPosition = startPosition;  // 开始位置
        String text = ++startPosition + "/" + this.urls.size();
        imageCount.setText(text);
    }

    // 初始化控件
    public void initWidgets(){
        relativeLayout = (RelativeLayout) activity.getLayoutInflater().inflate(
                R.layout.dialog_scale_image, null);
        close = (ImageView) relativeLayout.findViewById(R.id.close_iv);
        imageDate = (TextView) relativeLayout.findViewById(R.id.title_tv);
        imageOthers = (TextView) relativeLayout.findViewById(R.id.sub_title_tv);
        imageCount = (TextView) relativeLayout.findViewById(R.id.selected_tv);
        viewPager = (ViewPager) relativeLayout.findViewById(R.id.scale_image_view_paper);

        dialog = new Dialog(activity, R.style.Dialog_Fullscreen);
        dialog.setContentView(relativeLayout);

        // 消息响应
        // 返回
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // 图片浏览
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedPosition = position;
                String text = ++position + "/" + views.size();
                imageCount.setText(text);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    // 创建
    public void create(){
        dialog.show();
        views = new ArrayList<>();
        adapter = new ScaleImageAdapter(views, dialog);
        for(int i=0; i<urls.size(); i++){
            // 页面
            FrameLayout frameLayout = (FrameLayout) activity.getLayoutInflater().
                    inflate(R.layout.view_scale_image, null);
            PhotoView photoView = (PhotoView)
                    frameLayout.findViewById(R.id.scale_image_view);
            views.add(frameLayout);
            photoView.setImageBitmap(bitmaps.get(i));
//            photoView.setImage(ImageSource.bitmap(bitmaps.get(i)));
        }
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startPosition);
        imageDate.setText("2019年12月10日");  // 待更改,设置左上角图片信息
        imageOthers.setText("12:25 | 江苏省 南京市 玄武区");
    }

}
