package com.automation.ibinstallationteam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;
import com.automation.ibinstallationteam.entity.Portion;

import java.util.List;

/**
 * Created by pengchenghu on 2019/11/19.
 * Author Email: 15651851181@163.com
 * Describe: 部件的适配器
 */
public class PortionAdapter extends ArrayAdapter<Portion> {
    private int resourceId;

        public PortionAdapter(Context context, int textViewResourceId, List<Portion> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        ViewHolder viewHolder;
        Portion portion = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.portionImage = (ImageView) view.findViewById(R.id.portion_image);
            viewHolder.infoTypeImage = (ImageView) view.findViewById(R.id.info_type_image);
            viewHolder.portionName = (TextView) view.findViewById(R.id.portion_name);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        viewHolder.portionName.setText(portion.getName()); // 设置功能名称
        viewHolder.portionImage.setImageResource(portion.getImageId());    // 设置功能图片
        if(portion.getState() == 1) {  // 设置提醒信息
            viewHolder.infoTypeImage.setImageResource(R.mipmap.ic_normal);
            viewHolder.infoTypeImage.setVisibility(View.VISIBLE);
        } else{
            viewHolder.infoTypeImage.setVisibility(View.GONE);
        }

        return view;
    }

    class ViewHolder {
        ImageView portionImage;
        ImageView infoTypeImage;
        TextView portionName;
    }
}
