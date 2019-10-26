package com.automation.ibinstallationteam.widget.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.widget.image.SmartImageView;
import com.jungly.gridpasswordview.GridPasswordView;


/**
 * Created by pengchenghu on 2019/3/17.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class VerifyWorkDialog extends Dialog implements View.OnClickListener {

    // Handler 消息
    private final static int OPEN_KEYBOARD_MSG = 1;
    private final static int HIDE_KEYBOARD_MSG = 2;

    private ImageView closeImg;  // 关闭按钮
    private SmartImageView headImg; // 头像
    private TextView inputHintTxt;  // 输入提示框
    private TextView toggleInputTypeTxt; // 切换验证手段
    private TextView scanTypeTxt; // 扫码类型
    private TextView basketIdTxt;  // 吊篮ID
    private TextView outMsgTxt;  // 错误提示
    private GridPasswordView numPassword; // 数字密码输入框
    private ImageView fingerPrintPassword; // 指纹密码

    private Context mContext;
    private int mPasswordType = 0;  // 默认密码输入类型
    private int mScanType;    // 扫码确认工作类型 0:上工 1:下工
    private String mUserHeadUrl;  // 头像地址
    private String mBasketId; // 吊篮ID
    private String mPasswordTest = "123456"; // 测试密码
    // 消息监听
    private OnDialogOperateListener mOnDialogOperateListener;
    private GridPasswordView.OnPasswordChangedListener mPasswordChangedListener =  // 密码监听
            new GridPasswordView.OnPasswordChangedListener() {
        @Override
        public void onTextChanged(String psw) {
            // 数据改变
            outMsgTxt.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onInputFinish(String psw) {
            // 输入完成
            if(psw.equals(mPasswordTest)){
                mOnDialogOperateListener.getVerifyResult("Password Success");
                VerifyWorkDialog.this.dismiss();  // 密码框消失
            }else{
                outMsgTxt.setVisibility(View.VISIBLE);
                numPassword.clearPassword();
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            switch (msg.what) {
                case OPEN_KEYBOARD_MSG:
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    break;
                case HIDE_KEYBOARD_MSG:
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    break;
            }
        }
    };

    /*
     * 构造函数
     */
    public VerifyWorkDialog(Context context) {
        super(context);
    }

    public VerifyWorkDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public VerifyWorkDialog(Context context, int themeResId, int mScanType, String mUserHeadUrl, String mBasketId
                            ,OnDialogOperateListener mOnDialogOperateListener
                            //,GridPasswordView.OnPasswordChangedListener mPasswordChangedListener
                            ) {
        super(context, themeResId);
        this.mContext = context;
        this.mScanType = mScanType;
        this.mUserHeadUrl = mUserHeadUrl;
        this.mBasketId = mBasketId;
        this.mOnDialogOperateListener = mOnDialogOperateListener;
        //this.mPasswordChangedListener = mPasswordChangedListener;
    }

    public VerifyWorkDialog setHeadImageUrl(String mUserHeadUrl){
        this.mUserHeadUrl = mUserHeadUrl;
        return this;
    }

    public VerifyWorkDialog setGridPasswordListener(GridPasswordView.OnPasswordChangedListener mPasswordChangedListener){
        this.mPasswordChangedListener = mPasswordChangedListener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_verify_action);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView(){
        // head
        closeImg = (ImageView) findViewById(R.id.close_iv);
        closeImg.setOnClickListener(this);
        headImg = (SmartImageView) findViewById(R.id.head_iv);
        headImg.setImageUrl(mUserHeadUrl);  // 设定头像
        headImg.setCircle(true);
        inputHintTxt = (TextView) findViewById(R.id.input_hint);
        toggleInputTypeTxt = (TextView) findViewById(R.id.toggle_input_type);
        toggleInputTypeTxt.setOnClickListener(this);

        // body
        scanTypeTxt = (TextView) findViewById(R.id.scan_type);
        if(mScanType == 0) scanTypeTxt.setText(R.string.open_basket);
        else if(mScanType == 1) scanTypeTxt.setText(R.string.close_basket);
        basketIdTxt = (TextView) findViewById(R.id.basket_id);
        basketIdTxt.setText(mBasketId);

        // password
        outMsgTxt = (TextView) findViewById(R.id.output_msg);
        numPassword = (GridPasswordView) findViewById(R.id.num_password);
        numPassword.setOnPasswordChangedListener(mPasswordChangedListener);
        numPassword.requestFocus();
        fingerPrintPassword = (ImageView) findViewById(R.id.finger_print_password);
        mHandler.sendEmptyMessage(OPEN_KEYBOARD_MSG);  // 打开软键盘
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.close_iv:
                this.dismiss();
                break;
            case R.id.toggle_input_type:  // 切换密码验证方式
                toggleVerifyType();
                break;
        }
    }

    /*
     * 生命周期
     */
    @Override
    public void onStart(){
        super.onStart();
    }

    /*
     * 切换验证方式
     */
    private void toggleVerifyType(){
        if(0 == mPasswordType){
            // 视图更换
            mPasswordType = 1;
            inputHintTxt.setText(R.string.input_finger_print);
            toggleInputTypeTxt.setText(R.string.use_password);
            numPassword.setVisibility(View.GONE);
            fingerPrintPassword.setVisibility(View.VISIBLE);
            // 隐藏软键盘
            mHandler.sendEmptyMessage(HIDE_KEYBOARD_MSG);  // 打开软键盘
        }else if(1 == mPasswordType){
            // 视图更换
            mPasswordType = 0;
            inputHintTxt.setText(R.string.input_operate_password);
            toggleInputTypeTxt.setText(R.string.use_finger_print);
            fingerPrintPassword.setVisibility(View.GONE);
            numPassword.setVisibility(View.VISIBLE);
            numPassword.clearPassword();
            numPassword.requestFocus();
            // 获取焦点显示软键盘
            mHandler.sendEmptyMessage(OPEN_KEYBOARD_MSG);  // 打开软键盘
        }
    }

    /*
     * dialog 弹出位置
     */
    public void setProperty() {
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL);

        // 屏幕高度
        DisplayMetrics dm2 = Resources.getSystem().getDisplayMetrics();
        int mScreenHeight = dm2.heightPixels;

        lp.y = -(int)(mScreenHeight * 0.25);
        dialogWindow.setAttributes(lp);
    }


    /*
     * 自定义Dialog监听器
     */
    public interface OnDialogOperateListener{
        void getVerifyResult(String result);
    }
}
