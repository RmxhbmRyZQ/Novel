package flandre.cn.novel.adapter.adapter.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import flandre.cn.novel.R;

import java.util.List;

public class SpinnerAdapter extends BaseAdapter {
    private List<String> list;
    private LayoutInflater inflater;

    public SpinnerAdapter(List<String> list, Context context) {
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    public void update(List<String> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (list == null) return 0;
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String s = list.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spin_list, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(s);
        viewHolder.textView.setTextColor(Color.WHITE);
        return convertView;
    }

    private static class ViewHolder {
        private TextView textView;
    }
}
