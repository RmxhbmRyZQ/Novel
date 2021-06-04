package flandre.cn.novel.adapter.adapter.fragment;

import android.support.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import flandre.cn.novel.R;
import flandre.cn.novel.bean.data.novel.NovelInfo;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

import java.util.List;

public class BookAdapter extends BaseQuickAdapter<NovelInfo, BaseViewHolder> {
    public BookAdapter(int layoutResId, @Nullable List<NovelInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, NovelInfo item) {
        helper.setImageBitmap(R.id.image, NovelInfo.getBitmap(item.getImagePath(), mContext));
        helper.setText(R.id.title, item.getName());
        helper.setText(R.id.chapter, item.getChapter());
        helper.setTextColor(R.id.title, NovelConfigureManager.getConfigure().getNameTheme());
        helper.setTextColor(R.id.chapter, NovelConfigureManager.getConfigure().getIntroduceTheme());
        helper.addOnClickListener(R.id.root);
        helper.addOnLongClickListener(R.id.root);
    }
}
