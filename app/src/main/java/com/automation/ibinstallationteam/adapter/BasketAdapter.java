package com.automation.ibinstallationteam.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.application.AppConfig;
import com.automation.ibinstallationteam.entity.Basket;
import com.automation.ibinstallationteam.entity.Order;
import com.automation.ibinstallationteam.entity.Portion;
import com.automation.ibinstallationteam.entity.PortionMap;
import com.automation.ibinstallationteam.utils.ftp.FTPUtil;
import com.automation.ibinstallationteam.widget.image.SmartImageView;
import com.automation.ibinstallationteam.widget.image.WebImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengchenghu on 2019/11/19.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.ViewHolder>{

    private Context mContext;
    private List<Basket> mBasketList;
    private FTPUtil mFTPClient;
    private List<String> mIndexUrls;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        SmartImageView ivLogo;  // logo
        TextView tvId;  // 吊篮ID
        TextView tvSiteId; // 现场编号
        ImageView ivWorkerInfo;  //
        ImageView ivFinishImg;  //
        ImageView ivDeviceBound;  //

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final BasketAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);

            // 控件初始化
            mView = itemView;
            ivLogo = itemView.findViewById(R.id.basket_logo_smartImg);
            tvId = itemView.findViewById(R.id.basket_id_tv);
            tvSiteId = itemView.findViewById(R.id.site_id_tv);
            ivWorkerInfo = itemView.findViewById(R.id.worker_info_iv);
            ivFinishImg = itemView.findViewById(R.id.finish_img_iv);
            ivDeviceBound = itemView.findViewById(R.id.device_bound_iv);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // getpostion()为Viewholder自带的一个方法，用来获取RecyclerView当前的位置，将此作为参数，传出去
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    /* 构造函数
     */
    public BasketAdapter(Context mContext, List<Basket> basketList){
        this.mContext = mContext;
        mBasketList = basketList;
//        initFTPClient();
    }

    @NonNull
    @Override
    public BasketAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_basket,viewGroup,false);
        final BasketAdapter.ViewHolder holder = new BasketAdapter.ViewHolder(view, mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BasketAdapter.ViewHolder viewHolder, int i) {
        Basket basket = mBasketList.get(i);

        // 优先显示主视图
        String remoteUrl = AppConfig.FILE_SERVER_YBLIU_PATH + "project/" + basket.getProjectId() + "/" + basket.getId() + "/";
        viewHolder.ivLogo.setImageUrl(remoteUrl + PortionMap.englishPortion.get(1) + ".jpg");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(viewHolder.ivLogo.getBitmap() == null){
            viewHolder.ivLogo.setImageUrl(remoteUrl + PortionMap.englishPortion.get(0) + ".jpg");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(viewHolder.ivLogo.getBitmap() == null){
                viewHolder.ivLogo.setImageUrl(remoteUrl + PortionMap.englishPortion.get(2) + ".jpg");
            }
        }

        viewHolder.tvId.setText(basket.getId());
        if(!(basket.getSiteId()==null) && !(basket.getSiteId().equals("")))
            viewHolder.tvSiteId.setText(basket.getSiteId());

        if(basket.isWorkerInfo())
            viewHolder.ivWorkerInfo.setImageResource(R.mipmap.ic_normal);
        else
            viewHolder.ivWorkerInfo.setImageResource(R.mipmap.ic_abnormal);

        if(basket.isFinishImg())
            viewHolder.ivFinishImg.setImageResource(R.mipmap.ic_normal);
        else
            viewHolder.ivFinishImg.setImageResource(R.mipmap.ic_abnormal);

        if(basket.isDeviceBound())
            viewHolder.ivDeviceBound.setImageResource(R.mipmap.ic_normal);
        else
            viewHolder.ivDeviceBound.setImageResource(R.mipmap.ic_abnormal);
    }

    @Override
    public int getItemCount() {
        return mBasketList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(BasketAdapter.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /*
     * 点击接口函数
     */
    public interface OnItemClickListener {
        /**
         * 当RecyclerView某个被点击的时候回调
         * @param view 点击item的视图
         * @param position 点击得到位置
         */
        public void onItemClick(View view, int position);
    }

    /*
     * FTP 相关
     */
//    private void initUrls(){
//        mIndexUrls = new ArrayList<>();
//        for(int idx=0; idx < mBasketList.size(); idx++){
//            mIndexUrls.add("");
//            getIndexUrl(idx);
//        }
//    }
//    // 检查文件是否存在
//    private void getIndexUrl(final int idx){
//        Basket basket = mBasketList.get(idx);
//        final String mRemotePath = "project/" + basket.getProjectId() + "/" + basket.getId();  // 图片上传地址
//        new Thread() {
//            public void run() {
//                try {
//                    // 上传文件
//                    mFTPClient.openConnect();  // 建立连接
//                    mFTPClient.uploadingInit(mRemotePath); // 上传文件初始化
//                    List<String>  filenames = mFTPClient.listCurrentFileNames();
//                    int[] integers = {1, 0, 2};
//                    for (int i : integers ){
//                        if (filenames.contains(PortionMap.englishPortion.get(i) + ".jpg")){
////                            mIndexUrl = mRemotePath + '/' + PortionMap.englishPortion.get(idx) + ".jpg";
//                            mIndexUrls.set(idx, mRemotePath + '/' + PortionMap.englishPortion.get(i) + ".jpg");
//                            break;
//                        }
//                    }
//                    mFTPClient.closeConnect();  // 关闭连接
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }
//    // FTP 初始化
//    private void initFTPClient(){
//        mFTPClient = new FTPUtil(AppConfig.FILE_SERVER_YBLIU_IP, AppConfig.FILE_SERVER_YBLIU_PORT,
//                AppConfig.FILE_SERVER_USERNAME, AppConfig.FILE_SERVER_PASSWORD);
//    }
}
