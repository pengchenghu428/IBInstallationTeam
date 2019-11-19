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
import com.automation.ibinstallationteam.entity.Basket;
import com.automation.ibinstallationteam.entity.Order;
import com.automation.ibinstallationteam.widget.image.SmartImageView;

import java.util.List;

/**
 * Created by pengchenghu on 2019/11/19.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.ViewHolder>{

    private Context mContext;
    private List<Basket> mBasketList;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        SmartImageView ivLogo;  // logo
        TextView tvId;  // 吊篮名称
        ImageView ivWorkerInfo;  // 完成数目
        ImageView ivFinishImg;  // 总数目
        ImageView ivDeviceBound;  // 日期提示

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final BasketAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);

            // 控件初始化
            mView = itemView;
            ivLogo = itemView.findViewById(R.id.basket_logo_smartImg);
            tvId = itemView.findViewById(R.id.basket_id_tv);
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

        viewHolder.tvId.setText(basket.getId());
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
}
