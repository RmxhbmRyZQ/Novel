package novel.flandre.cn.adapter.adapter.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import novel.flandre.cn.R;
import novel.flandre.cn.bean.data.novel.NovelChapter;
import novel.flandre.cn.ui.activity.TextActivity;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.List;

public class ChoiceChapterAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<? extends NovelChapter> mList;
    private TextActivity mContext;

    public ChoiceChapterAdapter(TextActivity context, List<? extends NovelChapter> list) {
        this.mContext = context;
        this.mList = list;
        this.inflater = (mContext).getLayoutInflater();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NovelChapter novelChapter = mList.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.menu_list, null);
            viewHolder = new ViewHolder();
            viewHolder.item = convertView.findViewById(R.id.text);
            viewHolder.item.setTextColor(NovelConfigureManager.getConfigure().getTextColor());
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.item.setText(novelChapter.getChapter());
        return convertView;
    }

    private class ViewHolder {
        private TextView item = null;
    }
}
