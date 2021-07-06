package novel.flandre.cn.ui.fragment;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.fragment.MusicDialogAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.data.music.MusicInfo;
import novel.flandre.cn.service.PlayMusicService;
import novel.flandre.cn.ui.activity.LocalMusicActivity;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.List;

;

/**
 * 音乐播放列表弹窗
 * 2020.4.2
 */
public class MusicDialogFragment extends AttachDialogFragment implements MusicDialogAdapter.OnClickListener {
    private List<MusicInfo> infos;
    private TextView playList;
    private TextView clear;
    private TextView status;
    private MusicDialogAdapter mAdapter;

    public void setInfos(List<MusicInfo> infos) {
        if (infos == null) return;
        this.infos = infos;
        if (mAdapter != null) {
            mAdapter.setNewData(infos);
            playList.setText("播放列表 ( " + infos.size() + " ) ");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getDialog().getWindow();
        // 设置停留再底部
        WindowManager.LayoutParams params = window.getAttributes();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        window.setAttributes(params);

        View view = inflater.inflate(R.layout.music_dialog_fragment, container, false);
        setupView(view);
        setData();
        setListener();
        return view;
    }

    private void setupView(View view) {
        view.findViewById(R.id.top).setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        playList = view.findViewById(R.id.play_list);
        clear = view.findViewById(R.id.clear);
        status = view.findViewById(R.id.status);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        Decoration decoration = new Decoration(mContext);
        mAdapter = new MusicDialogAdapter(R.layout.music_list, infos);
        mAdapter.setClickListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.data);
        recyclerView.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(false);
    }

    public void adapterUpdate() {
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    private void setData() {
        playList.setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        status.setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        clear.setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        clear.setText("清空");
        if (infos != null) playList.setText("播放列表 ( " + infos.size() + " ) ");
        try {
            int order = ((LocalMusicActivity) mContext).getMusicService().getPlayOrder();
            status.setTag(order);
            status.setText(PlayMusicService.STATUS[order]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setListener() {
        // 修改播放顺序
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int order = (int) status.getTag();
                switch (order) {
                    case PlayMusicService.STATUS_ONE_LOOPING:
                        order = PlayMusicService.STATUS_ALL_LOOPING;
                        break;
                    case PlayMusicService.STATUS_ALL_LOOPING:
                        order = PlayMusicService.STATUS_ALL_RANDOM;
                        break;
                    case PlayMusicService.STATUS_ALL_RANDOM:
                        order = PlayMusicService.STATUS_ALL_TOTALLY_RANDOM;
                        break;
                    case PlayMusicService.STATUS_ALL_TOTALLY_RANDOM:
                        order = PlayMusicService.STATUS_ONE_LOOPING;
                }
                try {
                    ((LocalMusicActivity) mContext).getMusicService().setPlayOrder(order);
                    status.setTag(order);
                    status.setText(PlayMusicService.STATUS[order]);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        // 清空播放列表
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((LocalMusicActivity) mContext).getMusicService().deleteAllPlayQueue();
                    mAdapter.getData().clear();
                    mAdapter.notifyDataSetChanged();
                    dismiss();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.6);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public void onClickItem(View view, int pos) {
        if (pos < 0 || pos >= mAdapter.getData().size()) return;
        try {
            ((LocalMusicActivity) mContext).getMusicService().playTarget(mAdapter.getData().get(pos).getSongId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClickController(View view, int pos) {

        if (pos < 0 || pos >= mAdapter.getData().size()) return;
        try {
            ((LocalMusicActivity) mContext).getMusicService().deletePlayQueue(mAdapter.getData().get(pos).getSongId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mAdapter.getData().remove(pos);
        playList.setText("播放列表 ( " + infos.size() + " ) ");
        mAdapter.notifyItemChanged(pos);
    }
}
