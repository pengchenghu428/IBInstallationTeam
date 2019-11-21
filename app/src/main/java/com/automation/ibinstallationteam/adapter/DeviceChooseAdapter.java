package com.automation.ibinstallationteam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
public class DeviceChooseAdapter extends ArrayAdapter<Device> {
    private int resourceId;

    public DeviceChooseAdapter(Context context, int textViewResourceId, List<Device> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        DeviceChooseAdapter.ViewHolder viewHolder;
        Device device = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new DeviceChooseAdapter.ViewHolder();
            viewHolder.deviceImage = (ImageView) view.findViewById(R.id.device_image_iv);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name_tv);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (DeviceChooseAdapter.ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        viewHolder.deviceImage.setImageResource(device.getImageId());
        viewHolder.deviceName.setText(device.getName());

        return view;
    }

    class ViewHolder {
        ImageView deviceImage;
        TextView deviceName;
    }
}
