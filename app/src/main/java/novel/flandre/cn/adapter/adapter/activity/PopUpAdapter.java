package novel.flandre.cn.adapter.adapter.activity;

import android.view.View;
import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import novel.flandre.cn.R;
import novel.flandre.cn.bean.data.activity.Item;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.List;

public class PopUpAdapter extends BaseQuickAdapter<Item, BaseViewHolder> implements BaseQuickAdapter.OnItemChildClickListener {

    private OnPopUpClickListener listener;

    public PopUpAdapter(int layoutResId, @Nullable List<Item> data) {
        super(layoutResId, data);
        setOnItemChildClickListener(this);
    }

    @Override
    protected void convert(BaseViewHolder helper, Item item) {
        helper.setImageResource(R.id.img, item.getImageId());
        helper.setText(R.id.txt, item.getText());
        helper.setTextColor(R.id.txt, NovelConfigureManager.getConfigure().getNameTheme());
        helper.addOnClickListener(R.id.item);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        listener.popUpClickListener(view, position);
    }

    public void setListener(OnPopUpClickListener listener) {
        this.listener = listener;
    }

    public interface OnPopUpClickListener {
        public void popUpClickListener(View view, int pos);
    }
}
