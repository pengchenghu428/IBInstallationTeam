package com.automation.ibinstallationteam.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by pengchenghu on 2019/2/24.
 * Author Email: 15651851181@163.com
 * Describe: 解决在scrollview中嵌套gridview只显示一行的问题
 */
public class SmartGridView extends GridView {

    public SmartGridView(Context context) {
        super(context);
    }

    public SmartGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
