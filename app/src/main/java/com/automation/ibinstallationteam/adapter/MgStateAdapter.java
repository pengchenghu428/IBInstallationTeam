package com.automation.ibinstallationteam.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.automation.ibinstallationteam.R;

import java.util.List;

/**
 * Created by pengchenghu on 2019/3/27.
 * Author Email: 15651851181@163.com
 * Describe: 区域管理员吊篮状态筛选栏
 */
public class MgStateAdapter extends ArrayAdapter<String> {
    private int resourceId;
    private int selectedPosition;

    public MgStateAdapter(Context context, int textViewResourceId, List<String> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        selectedPosition = 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent ){
        View view;
        ViewHolder viewHolder;
        String state = getItem(position);

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.stateName = (TextView) view.findViewById(R.id.state_text_view);
            viewHolder.stateView = (View) view.findViewById(R.id.state_view);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();  //重新获取ViewHolder
        }

        viewHolder.stateName.setText(state); // 设置功能名称
        if(position == selectedPosition){
            viewHolder.stateName.setTextColor(Color.parseColor("#009688"));
            viewHolder.stateView.setVisibility(View.VISIBLE);    // 显示标线
        }else{
            viewHolder.stateName.setTextColor(Color.parseColor("#c0c0c0"));
            viewHolder.stateView.setVisibility(View.GONE);    // 不显示标线
        }

        return view;
    }

    /*
     * 设置选中位置
     */
    public void setSelectedPosition(int selectedPosition){
        this.selectedPosition = selectedPosition;
        this.notifyDataSetChanged();
    }

    /*
     * 视图类
     */
    class ViewHolder {
        TextView stateName;
        View stateView;
    }
}
