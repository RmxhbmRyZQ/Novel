package flandre.cn.novel.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import flandre.cn.novel.service.NovelService;
import flandre.cn.novel.R;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

/**
 * 缓存章节的弹出对话框
 * 2019.??
 */
public class DownloadDialogFragment extends AttachDialogFragment implements View.OnClickListener {
    private TextView twenty;
    private TextView fifty;
    private TextView total;
    private onDownloadListener listener;

    public void setListener(onDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.download_fragment_layout, container, false);
        TextView title = view.findViewById(R.id.title);
        twenty = view.findViewById(R.id.twenty);
        fifty = view.findViewById(R.id.fifty);
        total = view.findViewById(R.id.total);
        View view1 = view.findViewById(R.id.sep);

        title.setTextColor(NovelConfigureManager.getConfigure().getNameTheme());
        twenty.setTextColor(NovelConfigureManager.getConfigure().getNameTheme());
        fifty.setTextColor(NovelConfigureManager.getConfigure().getNameTheme());
        total.setTextColor(NovelConfigureManager.getConfigure().getNameTheme());
        view1.setBackgroundColor(NovelConfigureManager.getConfigure().getIntroduceTheme() & 0x22FFFFFF | 0x22000000);
        view.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        setupListener();
        return view;
    }

    private void setupListener(){
        twenty.setTag(20);
        fifty.setTag(50);
        total.setTag(NovelService.DOWNLOAD_ALL);
        twenty.setOnClickListener(this);
        fifty.setOnClickListener(this);
        total.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        listener.onDownload(v, (Integer) v.getTag());
        dismiss();
    }

    interface onDownloadListener{
        void onDownload(View v, int type);
    }
}
