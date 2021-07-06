package novel.flandre.cn.ui.fragment;

import android.app.Activity;
import android.content.Context;
import androidx.fragment.app.Fragment;
import novel.flandre.cn.service.DownloadListener;
import novel.flandre.cn.ui.activity.MusicListener;

public abstract class AttachFragment extends Fragment implements DownloadListener, MusicListener {
    public static final String TAG = "";
    Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDownloadFinish(int downloadFinish, int downloadCount, long downloadId) {

    }

    @Override
    public void onDownloadFail(long id) {

    }

    @Override
    public void onPlayMusic() {

    }

    @Override
    public void onPauseMusic() {

    }

    @Override
    public void onNextSong() {

    }

    @Override
    public void onLastSong() {

    }

    @Override
    public void onProgressChange() {

    }

    @Override
    public void onClearPlayList() {

    }
}
