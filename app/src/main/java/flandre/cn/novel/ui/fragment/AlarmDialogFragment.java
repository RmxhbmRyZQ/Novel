package flandre.cn.novel.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import flandre.cn.novel.R;
import flandre.cn.novel.adapter.adapter.fragment.AlarmDialogAdapter;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.adapter.decoration.Decoration;

import java.util.ArrayList;
import java.util.List;

/**
 * 小说闹钟弹窗
 * 2020.4.7
 */
public class AlarmDialogFragment extends AttachDialogFragment {
    public static final int NO_ALARM_STATE = -0x1000000;

    private List<String> choice = new ArrayList<String>() {{
        add("不开启");
        add("10分钟后");
        add("20分钟后");
        add("30分钟后");
        add("40分钟后");
        add("50分钟后");
        add("60分钟后");
        add("自定义");
    }};
    private int height;
    private OnClickItemListener listener;
    private String title;

    public void setListener(OnClickItemListener listener) {
        this.listener = listener;
    }

    public static AlarmDialogFragment newInstance(String title) {

        Bundle args = new Bundle();
        args.putString("title", title);

        AlarmDialogFragment fragment = new AlarmDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AlarmDialog);
        title = getArguments().getString("title");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alarm_dialog_fragment, container, false);
        view.findViewById(R.id.top).setBackgroundColor(NovelConfigureManager.getConfigure().getMainTheme());
        ((TextView) view.findViewById(R.id.title)).setText(title);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        RecyclerView recyclerView = view.findViewById(R.id.choice);
        recyclerView.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        recyclerView.setLayoutManager(manager);
        AlarmDialogAdapter adapter = new AlarmDialogAdapter(R.layout.alarm_list, choice);
        recyclerView.addItemDecoration(new Decoration(mContext));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (listener != null) listener.clickItem(position);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(0, height);
        height = view.getMeasuredHeight();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, height);
        getDialog().setCanceledOnTouchOutside(true);
    }

    public interface OnClickItemListener {
        public void clickItem(int pos);
    }
}
