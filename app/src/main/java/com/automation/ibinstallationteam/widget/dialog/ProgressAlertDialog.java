package com.automation.ibinstallationteam.widget.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;

import java.text.NumberFormat;

/**
 * Created by pengchenghu on 2019/4/12.
 * Author Email: 15651851181@163.com
 * Describe: 进度条
 */
public class ProgressAlertDialog extends AlertDialog {

    //
    private final static String TAG="ProgressDialog";

    // 控件
    private ProgressBar mProgress;
    private TextView mProgressNumber;
    private TextView mProgressPercent;
    private TextView mProgressMessage;

    // 显示数据
    private int mMax;
    private CharSequence mMessage;
    private boolean mHasStarted;
    private int mProgressVal;
    private String mProgressNumberFormat;
    private NumberFormat mProgressPercentFormat;

    // 消息处理
    private Handler mViewUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int progress = mProgress.getProgress();
            int max = mProgress.getMax();
//            double dProgress = (double)progress;
//            double dMax = (double)max;
            if (mProgressNumberFormat != null) {
                String format = mProgressNumberFormat;
                mProgressNumber.setText(String.format(format, progress, max));
            } else {
                mProgressNumber.setText("");
            }
            if (mProgressPercentFormat != null) {
                double percent = (double) progress / (double) max;
                SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                        0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mProgressPercent.setText(tmp);
            } else {
                mProgressPercent.setText("");
            }
        }
    };

    public ProgressAlertDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress_bar);
        setCanceledOnTouchOutside(false);
        initFormats();
        initView();
    }
    // 初始化显示格式
    private void initFormats() {
        mProgressNumberFormat = "%d/%d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    // 初始化资源控件
    private void initView(){
        mProgress=(ProgressBar) findViewById(R.id.progress);
        mProgressNumber=(TextView) findViewById(R.id.progress_number);
        mProgressPercent=(TextView) findViewById(R.id.progress_percent);
        mProgressMessage=(TextView) findViewById(R.id.progress_message);
        onProgressChanged();
        if (mMessage != null) {
            setMessage(mMessage);
        }
        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
    }

    // 进度条改变
    private void onProgressChanged() {
        mViewUpdateHandler.sendEmptyMessage(0);
    }

    // 设置进度条样式
    public void setProgressStyle(int style) {
        //mProgressStyle = style;
    }

    // 获取最大值
    public int getMax() {
        if (mProgress != null) {
            return mProgress.getMax();
        }
        return mMax;
    }

    // 设置最大值
    public void setMax(int max) {
        if (mProgress != null) {
            mProgress.setMax(max);
            onProgressChanged();
        } else {
            mMax = max;
        }
    }

    // 设置
    public void setIndeterminate(boolean indeterminate) {
        if (mProgress != null) {
            mProgress.setIndeterminate(indeterminate);
        } else {
            //mIndeterminate = indeterminate;
        }
    }

    // 设置进度条进度
    public void setProgress(int value) {
        if (mHasStarted) {
            mProgress.setProgress(value);
            onProgressChanged();
        } else {
            mProgressVal = value;
        }
    }

    // 设置弹窗消息
    @Override
    public void setMessage(CharSequence message) {
        // TODO Auto-generated method stub
        //super.setMessage(message);
        if(mProgressMessage!=null){
            mProgressMessage.setText(message);
        }
        else{
            mMessage = message;
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mHasStarted = true;
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        mHasStarted = false;
    }
}
