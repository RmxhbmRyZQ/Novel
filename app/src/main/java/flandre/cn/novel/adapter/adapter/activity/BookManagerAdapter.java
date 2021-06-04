package flandre.cn.novel.adapter.adapter.activity;

import android.support.annotation.Nullable;
import android.view.View;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import flandre.cn.novel.R;
import flandre.cn.novel.bean.data.novel.NovelInfo;
import flandre.cn.novel.ui.view.CheckBoxView;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

import java.util.ArrayList;
import java.util.List;

public class BookManagerAdapter extends BaseQuickAdapter<NovelInfo, BaseViewHolder> implements BaseQuickAdapter.OnItemChildClickListener {
    private boolean manage = true;  // 是否不显示选择框
    private List<String> position = new ArrayList<>();  // 记录选择了的 ID

    public BookManagerAdapter(int layoutResId, @Nullable List<NovelInfo> data) {
        super(layoutResId, data);
        setOnItemChildClickListener(this);
    }

    public void setManage(boolean manage) {
        this.manage = manage;
    }

    public boolean isManage() {
        return manage;
    }

    public List<String> getPosition() {
        return position;
    }

    @Override
    protected void convert(BaseViewHolder helper, NovelInfo item) {
        helper.setText(R.id.title, item.getName());
        helper.setText(R.id.author, item.getAuthor());
        helper.setImageBitmap(R.id.image, NovelInfo.getBitmap(item.getImagePath(), mContext));
        helper.setTextColor(R.id.title, NovelConfigureManager.getConfigure().getNameTheme());
        helper.setTextColor(R.id.author, NovelConfigureManager.getConfigure().getAuthorTheme());
        CheckBoxView checkBoxView = helper.getView(R.id.check);
        checkBoxView.setUnCheckColor(NovelConfigureManager.getConfigure().getAuthorTheme());
        checkBoxView.setColor(NovelConfigureManager.getConfigure().getMainTheme());
        helper.addOnClickListener(R.id.check);

        if (manage) {
            checkBoxView.setVisibility(View.GONE);
        } else {
            checkBoxView.setVisibility(View.VISIBLE);
            if (position.contains(String.valueOf(item.getId()))){
                checkBoxView.setUnUICheck(true);
            }else {
                checkBoxView.setUnUICheck(false);
            }
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        CheckBoxView checkBoxView = (CheckBoxView) view;
        boolean check = checkBoxView.isCheck();
        checkBoxView.setCheck(!check);
        if (check == checkBoxView.isCheck()) return;
        if (checkBoxView.isCheck()) {
            this.position.add(String.valueOf(getData().get(position).getId()));
        } else {
            this.position.remove(String.valueOf(getData().get(position).getId()));
        }

    }
}
