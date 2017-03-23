package com.scu.miomin.shswiperefreshlayout;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scu.miomin.shswiperefreshlayout.R;

import java.util.List;

public class SimpleLvAdapter extends BaseAdapter {

    private List datas;
    private Context context;

    // 构造器
    public SimpleLvAdapter(Context context, List datas) {
        super();
        this.datas = datas;
        this.context = context;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // 保留一个item的控件
        viewHolder holder = null;

        if (convertView == null) {
            // 拿到ListViewItem的布局（一行，需要单独定义一个），转换为View类型的对象
            convertView = View.inflate(context, R.layout.simple_item, null);
            holder = new viewHolder();
            holder.id_num = (TextView) convertView.findViewById(R.id.id_num);
            convertView.setTag(holder);
        } else {
            holder = (viewHolder) convertView.getTag();
        }

        holder.id_num.setText("" + datas.get(position));
        return convertView;
    }

    class viewHolder {
        TextView id_num;
    }
}