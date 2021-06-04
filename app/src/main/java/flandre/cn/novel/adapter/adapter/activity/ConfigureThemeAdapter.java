package flandre.cn.novel.adapter.adapter.activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import flandre.cn.novel.R;
import flandre.cn.novel.bean.data.activity.ConfigureThemeData;
import flandre.cn.novel.utils.tools.NovelConfigure;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

import java.util.List;

import static flandre.cn.novel.bean.data.activity.ConfigureThemeData.*;

public class ConfigureThemeAdapter extends BaseMultiItemQuickAdapter<ConfigureThemeData, BaseViewHolder>
        implements BaseQuickAdapter.OnItemChildClickListener {
    private int current;  // 当前选择的页面的索引, 保存时根据此设置配置文件

    public int getCurrent() {
        return current;
    }

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public ConfigureThemeAdapter(List<ConfigureThemeData> data) {
        super(data);
        // 绑定 type 和布局文件的关系，这时会根据类型来加载不同的布局
        addItemType(HEAD_TYPE, R.layout.head_list);
        addItemType(EDIT_TYPE, R.layout.theme_list);
        addItemType(ONE_CHOICE_TYPE, R.layout.source_list);
        addItemType(MULTI_CHOICE_TYPE, R.layout.source_list);
        setOnItemChildClickListener(this);
    }

    @Override
    protected void convert(BaseViewHolder helper, ConfigureThemeData item) {
        NovelConfigure configure = NovelConfigureManager.getConfigure();
        switch (item.getItemType()) {
            case HEAD_TYPE:
                helper.setText(R.id.head, item.getHead().getHeadText());
                helper.setTextColor(R.id.head, configure.getIntroduceTheme());
                break;
            case EDIT_TYPE:
                helper.setText(R.id.introduce, item.getEdit().getIntroduce());
                helper.setTextColor(R.id.introduce, configure.getNameTheme());

                EditText editText = helper.getView(R.id.input);
                if (editText.getTag() != null)
                    editText.removeTextChangedListener((TextWatcher) editText.getTag());
                Watcher watcher = new Watcher(helper.getLayoutPosition());
                editText.setTag(watcher);
                editText.setText(item.getEdit().getEditString());
                editText.setTextColor(configure.getNameTheme());
                editText.addTextChangedListener(watcher);

                helper.itemView.setBackgroundColor(configure.getBackgroundTheme());
                break;
            case ONE_CHOICE_TYPE:
                helper.setText(R.id.name, item.getOneChoice().getName());
                helper.setTextColor(R.id.name, configure.getNameTheme());
                helper.setBackgroundRes(R.id.choice, configure.getMode() == NovelConfigure.DAY ?
                        R.drawable.choice_day : R.drawable.choice_night);
                if (item.getOneChoice().isSelected()) current = helper.getLayoutPosition();
                helper.setVisible(R.id.choice, item.getOneChoice().isSelected());
                helper.itemView.setBackgroundColor(configure.getBackgroundTheme());
                helper.addOnClickListener(R.id.item);
                break;
            case MULTI_CHOICE_TYPE:
                helper.setText(R.id.name, item.getMultiChoice().getName());
                helper.setTextColor(R.id.name, configure.getNameTheme());
                helper.setBackgroundRes(R.id.choice, configure.getMode() == NovelConfigure.DAY ?
                        R.drawable.choice_day : R.drawable.choice_night);
                helper.setVisible(R.id.choice, item.getMultiChoice().isSelected());
                helper.itemView.setBackgroundColor(configure.getBackgroundTheme());
                helper.addOnClickListener(R.id.item);
                break;
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        ConfigureThemeData data = (ConfigureThemeData) adapter.getData().get(position);
        switch (data.getItemType()) {
            case HEAD_TYPE:
                break;
            case EDIT_TYPE:
                break;
            case ONE_CHOICE_TYPE:
                for (int i = 0; i < adapter.getData().size(); i++) {
                    ConfigureThemeData d = (ConfigureThemeData) adapter.getData().get(i);
                    if (d.getItemType() != ONE_CHOICE_TYPE) continue;
                    if (i != position) {
                        d.getOneChoice().setSelected(false);
                        adapter.getViewByPosition(i, R.id.choice).setVisibility(View.INVISIBLE);
                    } else {
                        current = i;
                        d.getOneChoice().setSelected(true);
                        view.findViewById(R.id.choice).setVisibility(View.VISIBLE);
                    }
                }
                break;
            case MULTI_CHOICE_TYPE:
                data.getMultiChoice().setSelected(!data.getMultiChoice().isSelected());
                view.findViewById(R.id.choice).setVisibility(data.getMultiChoice().isSelected() ? View.VISIBLE : View.INVISIBLE);
                break;
        }
    }


    class Watcher implements TextWatcher {
        private int pos;

        Watcher(int pos) {
            this.pos = pos;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            // 修改数据时, 暂时把数据保存到saveData
            getData().get(pos).getEdit().setEditString(s.toString());
        }
    }
}
