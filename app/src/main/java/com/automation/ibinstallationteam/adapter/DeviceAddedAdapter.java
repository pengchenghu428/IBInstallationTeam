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
import com.automation.ibinstallationteam.entity.Device;

import java.util.List;

/**
 * Created by pengchenghu on 2019/11/21.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class DeviceAddedAdapter extends RecyclerView.Adapter<DeviceAddedAdapter.ViewHolder> {
    private Context mContext;
    private List<Device> mDeviceList;
    private OnItemClickListener mOnItemClickListener;  // 消息监听

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        ImageView deviceImage;  // 设备图片
        TextView deviceName;  // 设备名称
        TextView deviceNumber;  // 设备序列号
        ImageView deleteBtn;  // 删除按钮

        private OnItemClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final DeviceAddedAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);

            // 控件初始化
            mView = itemView;
            deviceImage = itemView.findViewById(R.id.device_image_iv);
            deviceName = itemView.findViewById(R.id.device_name_tv);
            deviceNumber = itemView.findViewById(R.id.device_number_tv);
            deleteBtn = itemView.findViewById(R.id.device_delete_iv);

            // 消息监听
            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);  // 点击单元格响应
            deleteBtn.setOnClickListener(new View.OnClickListener(){  // 点击删除按键响应
                @Override
                public void onClick(View v) {
                    onItemClickListener.onDeleteClick(v, getAdapterPosition());
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
    public DeviceAddedAdapter(Context mContext, List<Device> deviceList){
        this.mContext = mContext;
        mDeviceList = deviceList;
    }

    @NonNull
    @Override
    public DeviceAddedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_device_bound,viewGroup,false);
        final DeviceAddedAdapter.ViewHolder holder = new DeviceAddedAdapter.ViewHolder(view, mOnItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAddedAdapter.ViewHolder viewHolder, int i) {
        Device device = mDeviceList.get(i);

        viewHolder.deviceImage.setImageResource(device.getImageId());
        viewHolder.deviceName.setText(device.getName());
        viewHolder.deviceNumber.setText(device.getNumber());
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    /*
     * 设置监听
     */
    public void setOnItemClickListener(DeviceAddedAdapter.OnItemClickListener mOnItemClickListener) {
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
        public void onDeleteClick(View view, int position);
    }
}
