package novel.flandre.cn.ui.activity;

import android.app.Dialog;
import android.content.*;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import novel.flandre.cn.MusicAidlInterface;
import novel.flandre.cn.R;
import novel.flandre.cn.bean.data.novel.NovelDownloadInfo;
import novel.flandre.cn.service.DownloadListener;
import novel.flandre.cn.service.NovelService;
import novel.flandre.cn.service.PlayMusicService;
import novel.flandre.cn.utils.database.SQLTools;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.database.SharedTools;

import java.util.ArrayList;
import java.util.List;

/**
 * 2020.3.27
 */
public abstract class BaseActivity extends AppCompatActivity implements DownloadListener, MusicListener {
    public final static int NOVEL_SERVICE_CONNECTED = 0;
    public final static int MUSIC_SERVICE_CONNECTED = 1;

    private static boolean isFirst = true;
    private static boolean isSave = false;

    private Dialog splashDialog;  // 加载窗口界面弹窗
    private List<DownloadListener> mDownloadListener = new ArrayList<>();  // 下载监听器
    private List<MusicListener> mMusicListener = new ArrayList<>();  // 音乐监听器
    private Receiver receiver;  // 广播接收者
    NovelService mService;  // 小说服务
    private ServiceConnection mConnection;  // 小说服务的连接
    private boolean isBind;  // 小说服务是否已经绑定

    MusicAidlInterface musicService;  // 音乐服务
    private ServiceConnection musicSConnection;  // 音乐服务的连接
    private boolean isMusicBind;  // 音乐服务是否以绑定
    private boolean hasBindMusic = false;  // 是否已经绑定的音乐服务
    private boolean bindMusicEnable = false;  // 是否决定绑定音乐服务

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 设定加载图片
        setScreen();
        super.onCreate(savedInstanceState);
        SharedTools.getSharedTools(getApplicationContext());  // 初始化
        // 绑定接收者
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NovelService.DOWNLOAD_FINISH);
        filter.addAction(NovelService.DOWNLOAD_FAIL);
        filter.addAction(PlayMusicService.MUSIC_PLAY);
        filter.addAction(PlayMusicService.MUSIC_PAUSE);
        filter.addAction(PlayMusicService.MUSIC_NEXT);
        filter.addAction(PlayMusicService.MUSIC_LAST);
        filter.addAction(PlayMusicService.PROGRESS_CHANGE);
        filter.addAction(PlayMusicService.CLEAR_PLAY_LIST);
        registerReceiver(receiver, filter);
    }

    private void checkForScreen(Intent intent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (isInMultiWindowMode() || isInPictureInPictureMode())) {
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        checkForScreen(intent);
        super.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        checkForScreen(intent);
        super.startActivity(intent, options);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        checkForScreen(intent);
        super.startActivityForResult(intent, requestCode, options);
    }

    /**
     * 加载音乐服务
     */
    void setupMusicService() {
        bindMusicEnable = true;
        if (hasBindMusic) return;
        if (!SharedTools.getSharedTools().getMusicEnable()) return;
        if (isFirst){
            Intent intent = new Intent(this, PlayMusicService.class);
            startService(intent);
            isFirst = false;
        }
        hasBindMusic = true;
        musicSConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicService = MusicAidlInterface.Stub.asInterface(service);
                isMusicBind = true;
                BaseActivity.this.onServiceConnected(BaseActivity.MUSIC_SERVICE_CONNECTED);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isMusicBind = false;
            }
        };
        Intent intent = new Intent(this, PlayMusicService.class);
        bindService(intent, musicSConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 加载小说服务
     */
    void setupNovelService() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                NovelService.NovelBind bind = (NovelService.NovelBind) service;
                mService = bind.getService();
                isBind = true;
                BaseActivity.this.onServiceConnected(BaseActivity.NOVEL_SERVICE_CONNECTED);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBind = false;
            }
        };
        Intent intent = new Intent(this, NovelService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 取消绑定小说服务
     */
    private void unBindNovelService() {
        if (mConnection != null && isBind) {
            unbindService(mConnection);
            isBind = false;
        }
    }

    /**
     * 设置加载窗口的页面
     */
    private void setScreen() {
        DisplayMetrics metrics = new DisplayMetrics();
        LinearLayout root = new LinearLayout(this);
        root.setMinimumHeight(metrics.heightPixels);
        root.setMinimumWidth(metrics.widthPixels);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
        root.setBackgroundResource(R.mipmap.loading);
        // Create and show the dialog
        splashDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        // check to see if the splash screen should be full screen
        if ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            splashDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        Window window = splashDialog.getWindow();
        window.setWindowAnimations(R.style.dialog_anim_fade_out);

        splashDialog.setContentView(root);
        splashDialog.setCancelable(false);
        splashDialog.show();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isSave = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (musicService != null && !isSave) {
            try {
                isSave = true;
                musicService.saveData();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bindMusicEnable && !hasBindMusic) {
            setupMusicService();
        }
        if (splashDialog != null) {
            splashDialog.dismiss();
            splashDialog = null;
        }
    }

    public boolean addDownloadFinishListener(DownloadListener finishListener) {
        return mDownloadListener.add(finishListener);
    }

    public boolean removeDownloadFinishListener(DownloadListener finishListener) {
        return mDownloadListener.remove(finishListener);
    }

    private void downloadFinishTrack(Intent intent) {
        int downloadFinish = intent.getIntExtra("finish", 0);
        int downloadCount = intent.getIntExtra("count", 0);
        long downloadId = intent.getLongExtra("id", 0);
        for (DownloadListener finishListener : mDownloadListener) {
            finishListener.onDownloadFinish(downloadFinish, downloadCount, downloadId);
        }
    }

    private void downloadFailTrack(Intent intent) {
        long id = intent.getLongExtra("id", 0);
        for (DownloadListener finishListener : mDownloadListener) {
            finishListener.onDownloadFail(id);
        }
    }

    public void addMusicListener(MusicListener musicListener) {
        mMusicListener.add(musicListener);
    }

    public void removeMusicListener(MusicListener musicListener) {
        mMusicListener.remove(musicListener);
    }

    private void musicPlayTrack() {
        for (MusicListener musicListener : mMusicListener) {
            musicListener.onPlayMusic();
        }
    }

    private void musicPauseTrack() {
        for (MusicListener musicListener : mMusicListener) {
            musicListener.onPauseMusic();
        }
    }

    private void nextSongTrack() {
        for (MusicListener musicListener : mMusicListener) {
            musicListener.onNextSong();
        }
    }

    private void lastSongTrack() {
        for (MusicListener musicListener : mMusicListener) {
            musicListener.onLastSong();
        }
    }

    private void changeProgressTrack() {
        for (MusicListener musicListener : mMusicListener) {
            musicListener.onProgressChange();
        }
    }

    private void clearPlayListTrack() {
        for (MusicListener musicListener : mMusicListener) {
            musicListener.onClearPlayList();
        }
    }

    void onServiceConnected(int service) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unBindNovelService();
        if (isMusicBind) {
            unbindService(musicSConnection);
            isMusicBind = false;
        }
    }

    @Override
    public void onDownloadFinish(int downloadFinish, int downloadCount, long downloadId) {

    }

    @Override
    public void onDownloadFail(long id) {
        NovelDownloadInfo downloadInfo = SQLTools.getDownloadInfo(SQLiteNovel.getSqLiteNovel(this.getApplicationContext()),
                "id = ?", new String[]{String.valueOf(id)}, null).get(0);
        Toast.makeText(this, downloadInfo.getTable() + " 下载失败，请重试", Toast.LENGTH_SHORT).show();
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

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case NovelService.DOWNLOAD_FINISH:
                    downloadFinishTrack(intent);
                    break;
                case NovelService.DOWNLOAD_FAIL:
                    downloadFailTrack(intent);
                    break;
                case PlayMusicService.MUSIC_PLAY:
                    musicPlayTrack();
                    break;
                case PlayMusicService.MUSIC_PAUSE:
                    musicPauseTrack();
                    break;
                case PlayMusicService.MUSIC_NEXT:
                    nextSongTrack();
                    break;
                case PlayMusicService.MUSIC_LAST:
                    lastSongTrack();
                    break;
                case PlayMusicService.PROGRESS_CHANGE:
                    changeProgressTrack();
                    break;
                case PlayMusicService.CLEAR_PLAY_LIST:
                    clearPlayListTrack();
                    break;
            }
        }
    }
}
