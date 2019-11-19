package com.automation.ibinstallationteam.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.entity.Order;
import com.automation.ibinstallationteam.widget.image.SmartImageView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by pengchenghu on 2019/11/19.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context mContext;
    private List<Order> mOrderList;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        SmartImageView ivLogo;  // logo
        TextView tvName;  // 工单名称
        TextView tvFinish;  // 完成数目
        TextView tvTotal;  // 总数目
        TextView tvDateHint;  // 日期提示
        TextView tvDateContent;  // 具体日期

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OrderAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);

            // 控件初始化
            mView = itemView;
            ivLogo = itemView.findViewById(R.id.company_logo_smartImg);
            tvName = itemView.findViewById(R.id.name_iv);
            tvFinish = itemView.findViewById(R.id.finish_num_tv);
            tvTotal = itemView.findViewById(R.id.total_num_tv);
            tvDateHint = itemView.findViewById(R.id.order_date_hint_tv);
            tvDateContent = itemView.findViewById(R.id.order_date_content_tv);

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
    public OrderAdapter(Context mContext, List<Order> orderList){
        this.mContext = mContext;
        mOrderList = orderList;
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_order,viewGroup,false);
        final ViewHolder holder = new ViewHolder(view, mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder viewHolder, int i) {
        Order order = mOrderList.get(i);
        //viewHolder.ivLogo.setImageUrl();
        viewHolder.tvName.setText(order.getName());
        viewHolder.tvFinish.setText(Integer.toString(order.getFinishNum()));
        viewHolder.tvTotal.setText(Integer.toString(order.getTotalNum()));
        if(order.getFinishNum() == order.getTotalNum())
            viewHolder.tvDateHint.setText("完成日期：");
        else
            viewHolder.tvDateHint.setText("预计完成日期：");
        viewHolder.tvDateContent.setText(order.getCompletedTime());
    }

    @Override
    public int getItemCount() {
        return mOrderList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(OrderAdapter.OnItemClickListener mOnItemClickListener) {
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
