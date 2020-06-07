package com.automation.ibinstallationteam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.entity.VarSwitch;

import java.util.List;

import static android.view.View.GONE;

/**
 * Created by pengchenghu on 2019/2/24.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class VarSwitchAdapter extends ArrayAdapter<VarSwitch> {
    private int resourceId;

    public VarSwitchAdapter(Context context, int textViewResourceId, List<VarSwitch> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        ViewHolder viewHolder;
        VarSwitch varSwitch = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.varSwitchImage = (ImageView) view.findViewById(R.id.var_switch_image);
            viewHolder.infoTypeImage = (ImageView) view.findViewById(R.id.info_type_image);
            viewHolder.varSwitchName = (TextView) view.findViewById(R.id.var_switch_name);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        if(varSwitch.getState() == -1){
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        viewHolder.varSwitchName.setText(varSwitch.getName()); // 设置功能名称
        viewHolder.varSwitchImage.setImageResource(varSwitch.getImageId());    // 设置功能图片
        if(varSwitch.getState() == 0) {  // 设置提醒信息
            viewHolder.infoTypeImage.setImageResource(R.mipmap.ic_safe);
            viewHolder.infoTypeImage.setVisibility(View.VISIBLE);
        } else if(varSwitch.getState() == 1) {
            viewHolder.infoTypeImage.setImageResource(R.mipmap.ic_warning);
            viewHolder.infoTypeImage.setVisibility(View.VISIBLE);
        }else if(varSwitch.getState() == 2){
            viewHolder.infoTypeImage.setVisibility(GONE);
        }

        return view;
    }

    class ViewHolder {
        ImageView varSwitchImage;
        ImageView infoTypeImage;
        TextView varSwitchName;
    }
}
