package flandre.cn.novel.adapter.adapter.fragment;

import android.support.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import flandre.cn.novel.R;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

import java.util.List;

public class SearchRecordAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public SearchRecordAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.text, item);
        helper.setTextColor(R.id.text, NovelConfigureManager.getConfigure().getNameTheme());
        helper.addOnClickListener(R.id.text);
    }
}
