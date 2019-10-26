package com.automation.ibinstallationteam.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;


/**
 * Created by pengchenghu on 2019/4/12.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class LoadingDialog extends Dialog {
    private TextView tv;
    private String str;
    /**
     * style很关键
     */
    public LoadingDialog(Context context) {
        super(context, R.style.loadingDialogStyle);
    }
    public LoadingDialog(Context context, String str) {
        super(context, R.style.loadingDialogStyle);
        this.str = str;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        tv = (TextView) findViewById(R.id.tv);
        tv.setText(str);
        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.LinearLayout);
        linearLayout.getBackground().setAlpha(210);
    }

    /*
     * 设置显示的字符串
     */
    public void setDisplayTextView(String str){
        this.str = str;
        tv.setText(str);
    }
}
