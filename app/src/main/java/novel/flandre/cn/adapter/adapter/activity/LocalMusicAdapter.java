package novel.flandre.cn.adapter.adapter.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import novel.flandre.cn.R;
import novel.flandre.cn.bean.data.music.MusicInfo;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LocalMusicAdapter extends BaseQuickAdapter<MusicInfo, BaseViewHolder>
        implements BaseQuickAdapter.OnItemChildClickListener, BaseQuickAdapter.OnItemChildLongClickListener {
    private boolean checkEnable;
    private List<Integer> checkPosition = new ArrayList<>();
    private OnMusicCall onCall;

    public boolean isCheckEnable() {
        return checkEnable;
    }

    public void setCheckEnable(boolean checkEnable) {
        this.checkEnable = checkEnable;
    }

    public List<Integer> getCheckPosition() {
        return checkPosition;
    }

    public void setOnCall(OnMusicCall onCall) {
        this.onCall = onCall;
    }

    public LocalMusicAdapter(int layoutResId, @Nullable List<MusicInfo> data) {
        super(layoutResId, data);
        setOnItemChildClickListener(this);
        setOnItemChildLongClickListener(this);
    }

    @Override
    protected void convert(BaseViewHolder helper, MusicInfo item) {
        helper.setText(R.id.name, item.getName());
        helper.setText(R.id.singer, item.getSinger());
        helper.setTextColor(R.id.name, NovelConfigureManager.getConfigure().getNameTheme());
        helper.setTextColor(R.id.singer, NovelConfigureManager.getConfigure().getAuthorTheme());

        View view = helper.getView(R.id.isPlaying);
        view.setBackgroundColor(NovelConfigureManager.getConfigure().getMainTheme());
        view.setVisibility(item.isPlaying() ? View.VISIBLE : View.GONE);
        if (checkEnable) {
            helper.getView(R.id.control).setVisibility(View.GONE);
            helper.setVisible(R.id.check, true);
            helper.setChecked(R.id.check, checkPosition.contains(helper.getLayoutPosition()));
            helper.setBackgroundRes(R.id.check, NovelConfigureManager.getConfigure().getMode() ==
                    NovelConfigure.DAY ? R.drawable.check_select_day : R.drawable.check_select_night);
        } else {
            helper.setChecked(R.id.check, false);
            helper.setVisible(R.id.control, true);
            helper.getView(R.id.check).setVisibility(View.GONE);
            helper.setImageResource(R.id.control, NovelConfigureManager.getConfigure().getMode() ==
                    NovelConfigure.DAY ? R.drawable.more_day : R.drawable.more_night);
        }
        setListener(helper);
    }

    private void setListener(BaseViewHolder helper) {
        helper.addOnClickListener(R.id.wrap);
        helper.addOnClickListener(R.id.control);
        helper.addOnLongClickListener(R.id.wrap);
    }

    private void setDialogTheme(AlertDialog alertDialog) {
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mController = mAlert.get(alertDialog);
            Field mMessage = mController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mController);
            mMessageView.setTextColor(NovelConfigureManager.getConfigure().getTextColor());

            Field mWindow = mController.getClass().getDeclaredField("mWindow");
            mWindow.setAccessible(true);
            Window window = (Window) mWindow.get(mController);
            window.setBackgroundDrawable(new ColorDrawable(NovelConfigureManager.getConfigure().getBackgroundTheme()));
//                int color = NovelConfigureManager.getConfigure().getMode() == NovelConfigure.NIGHT ? NovelConfigureManager.
//                        getConfigure().getNameTheme() : NovelConfigureManager.getConfigure().getMainTheme();
            int color = NovelConfigureManager.getConfigure().getNameTheme();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
        } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position) {
        final List<MusicInfo> data = getData();
        switch (view.getId()) {
            case R.id.wrap:
                // 如果是选择的歌曲的话, 点击选择(取消)歌曲
                CheckBox checkBox = (CheckBox) adapter.getViewByPosition(position, R.id.check);
                if (checkEnable) {
                    boolean check = checkBox.isChecked();
                    if (check)
                        checkPosition.remove(position);
                    else checkPosition.add(position);
                    checkBox.setChecked(!check);
                    return;
                }
                // 把所有的歌曲放入播放列表, 播放点击的歌曲
                if (position < 0 || position >= data.size()) return;
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                AlertDialog alertDialog = builder.setMessage("是否播放" + data.get(position).getName() + "？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onCall.onPlayMusic(position);
                            }
                        }).setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                alertDialog.show();
//                    setDialogTheme(alertDialog);
                break;
            case R.id.control:
                // 点击右侧的控制点时, 显示选项
                if (position < 0 || position >= data.size()) return;
                AlertDialog.Builder musicDialog = new AlertDialog.Builder(mContext);
                final MusicInfo musicInfo = data.get(position);
                musicDialog.setTitle(musicInfo.getName());
                String[] items = new String[]{"下一首播放", "删除(播放列表)", "选择歌曲", "分享"};
                musicDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // 放入播放列表
                                onCall.onAddMusic(musicInfo);
                                break;
                            case 1:
                                // 从播放列表删除
                                onCall.onDeleteMusic(musicInfo);
                                break;
                            case 2:
                                // 选择歌曲
                                if (checkEnable) break;
                                onCall.onSelectMusic();
                                checkPosition.add(data.indexOf(musicInfo));
                                checkEnable = true;
                                notifyDataSetChanged();
                                break;
                            case 3:
                                onCall.onShareMusic(musicInfo);
                        }
                    }
                });
                musicDialog.show();
                break;
        }
    }

    @Override
    public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
        // 已经是选择模式的话就没必要进入了
        if (checkEnable) return false;
        onCall.onSelectMusic();
        checkPosition.add(position);
        checkEnable = true;
        notifyDataSetChanged();
        return true;
    }

    public static interface OnMusicCall {
        public void onPlayMusic(int position);

        public void onAddMusic(MusicInfo musicInfo);

        public void onDeleteMusic(MusicInfo musicInfo);

        public void onShareMusic(MusicInfo musicInfo);

        public void onSelectMusic();
    }
}
