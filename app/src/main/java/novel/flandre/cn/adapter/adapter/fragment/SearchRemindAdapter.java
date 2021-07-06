package novel.flandre.cn.adapter.adapter.fragment;

import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import novel.flandre.cn.R;
import novel.flandre.cn.bean.data.novel.NovelRemind;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.List;

public class SearchRemindAdapter extends BaseQuickAdapter<NovelRemind, BaseViewHolder> {
    public SearchRemindAdapter(int layoutResId, @Nullable List<NovelRemind> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, NovelRemind item) {
        helper.setText(R.id.name, item.getName());
        helper.setText(R.id.chapter, item.getChapter());
        helper.setTextColor(R.id.name, NovelConfigureManager.getConfigure().getNameTheme());
        helper.setTextColor(R.id.chapter, NovelConfigureManager.getConfigure().getAuthorTheme());
        helper.addOnClickListener(R.id.wrap);
    }
}
