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
import com.automation.ibinstallationteam.entity.WorkerInfo;
import com.automation.ibinstallationteam.utils.StringReplaceUtil;
import com.automation.ibinstallationteam.widget.image.SmartImageView;

import java.util.List;

/**
 * Created by pengchenghu on 2019/11/21.
 * Author Email: 15651851181@163.com
 * Describe: 作业人员列表适配器
 */
public class WorkerInfoAdapter extends RecyclerView.Adapter<WorkerInfoAdapter.ViewHolder>{
    private Context mContext;
    private List<WorkerInfo> mWorkInfoList;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        SmartImageView operationImage;  // 操作证书
        TextView workerName;  // 工人姓名
        TextView workerNumber;  // 工人手机号码
        ImageView callPhone;  // 拨号按钮
        TextView workerIdCardNumber;  // 工人身份证

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final WorkerInfoAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);

            // 控件初始化
            mView = itemView;
            operationImage = itemView.findViewById(R.id.operation_iv);
            workerName = itemView.findViewById(R.id.name_tv);
            workerNumber = itemView.findViewById(R.id.phone_number_tv);
            callPhone = itemView.findViewById(R.id.call_phone);
            workerIdCardNumber = itemView.findViewById(R.id.id_card_number_tv);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);  // 点击单元格响应
            callPhone.setOnClickListener(new View.OnClickListener(){  // 点击删除按键响应
                @Override
                public void onClick(View v) {
                    onItemClickListener.onCallPhoneClick(v, getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    /* 构造函数
     */
    public WorkerInfoAdapter(Context mContext, List<WorkerInfo> workerInfo){
        this.mContext = mContext;
        mWorkInfoList = workerInfo;
    }

    @NonNull
    @Override
    public WorkerInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_worker_info,viewGroup,false);
        final WorkerInfoAdapter.ViewHolder holder = new WorkerInfoAdapter.ViewHolder(view, mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerInfoAdapter.ViewHolder viewHolder, int i) {
        WorkerInfo workerInfo = mWorkInfoList.get(i);

        String remoteFileUrl = AppConfig.FILE_SERVER_YBLIU_PATH + "tempUser/" + workerInfo.getPhoneNumber() + "/operation.jpg";
        viewHolder.operationImage.setImageUrl(remoteFileUrl, R.mipmap.ic_baiansheng);
        viewHolder.workerName.setText(workerInfo.getName());
        viewHolder.workerNumber.setText(workerInfo.getPhoneNumber());
        viewHolder.workerIdCardNumber.setText(StringReplaceUtil.idCardReplaceWithStar(workerInfo.getIdCardNumber()));
    }

    @Override
    public int getItemCount() {
        return mWorkInfoList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(WorkerInfoAdapter.OnItemClickListener mOnItemClickListener) {
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
        public void onCallPhoneClick(View view, int position);
    }
}
