package flandre.cn.novel.adapter.adapter.fragment;

import android.support.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import flandre.cn.novel.R;
import flandre.cn.novel.bean.data.activity.Item;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

import java.util.List;

public class UserAdapter extends BaseQuickAdapter<Item, BaseViewHolder> {
    public UserAdapter(int layoutResId, @Nullable List<Item> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Item item) {
        helper.setImageResource(R.id.icon, item.getImageId());
        helper.setText(R.id.txt, item.getText());
        helper.setTextColor(R.id.txt, NovelConfigureManager.getConfigure().getAuthorTheme());
        helper.addOnClickListener(R.id.bottom);
    }
}
