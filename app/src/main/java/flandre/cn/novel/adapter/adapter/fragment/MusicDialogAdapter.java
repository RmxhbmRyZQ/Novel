package flandre.cn.novel.adapter.adapter.fragment;

import android.support.annotation.Nullable;
import android.view.View;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import flandre.cn.novel.R;
import flandre.cn.novel.bean.data.music.MusicInfo;
import flandre.cn.novel.utils.tools.NovelConfigure;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

import java.util.List;

public class MusicDialogAdapter extends BaseQuickAdapter<MusicInfo, BaseViewHolder> implements BaseQuickAdapter.OnItemChildClickListener {
    private OnClickListener clickListener;

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public MusicDialogAdapter(int layoutResId, @Nullable List<MusicInfo> data) {
        super(layoutResId, data);
        setOnItemChildClickListener(this);
    }

    @Override
    protected void convert(BaseViewHolder helper, MusicInfo item) {
        helper.setText(R.id.name, item.getName());
        helper.setText(R.id.singer, item.getSinger());
        helper.setImageResource(R.id.control, NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY ?
                R.drawable.remove_day : R.drawable.remove_night);
        helper.setTextColor(R.id.name, NovelConfigureManager.getConfigure().getNameTheme());
        helper.setTextColor(R.id.singer, NovelConfigureManager.getConfigure().getIntroduceTheme());
        helper.setVisible(R.id.isPlaying, item.isPlaying());
        helper.setBackgroundColor(R.id.isPlaying, NovelConfigureManager.getConfigure().getMainTheme());
        helper.addOnClickListener(R.id.wrap);
        helper.addOnClickListener(R.id.control);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        switch (view.getId()){
            case R.id.wrap:
                if (clickListener != null)
                    clickListener.onClickItem(view, position);
                break;
            case R.id.control:
                if (clickListener != null)
                    clickListener.onClickController(view, position);
                break;
        }
    }

    public static interface OnClickListener{
        public void onClickItem(View view, int pos);

        public void onClickController(View view, int pos);
    }
}
