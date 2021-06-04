package flandre.cn.novel.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import flandre.cn.novel.MusicAidlInterface;
import flandre.cn.novel.R;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.ui.activity.LocalMusicActivity;
import flandre.cn.novel.bean.data.music.MusicInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 音乐的控件
 * 2020.4.1
 */
public class MusicControllerFragment extends AttachFragment {
    private SeekBar progress;
    private TextView position;
    private TextView duration;
    private TextView name;
    private TextView singer;
    private ImageView last;
    private ImageView next;
    private ImageView controller;
    private boolean isProgressChanging = false;
    private Handler handler;
    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };  // 播放音乐时持续改变进度条

    public void setProgressChanging(boolean progressChanging) {
        isProgressChanging = progressChanging;
    }

    public boolean isProgressChanging() {
        return isProgressChanging;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LocalMusicActivity) mContext).addMusicListener(this);
        handler = new Handler(mContext.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_controller_fragment_layout, container, false);
        view.setBackgroundColor(NovelConfigureManager.getConfigure().getMainTheme());
        progress = view.findViewById(R.id.progress);
        name = view.findViewById(R.id.name);
        name.requestFocus();
        singer = view.findViewById(R.id.singer);
        last = view.findViewById(R.id.last);
        controller = view.findViewById(R.id.control);
        next = view.findViewById(R.id.next);
        position = view.findViewById(R.id.position);
        duration = view.findViewById(R.id.duration);
        setTheme();
        setListener();
        return view;
    }

    private void setTheme() {
        last.setImageResource(R.drawable.last_music_night);
        controller.setImageResource(R.drawable.play_night);
        next.setImageResource(R.drawable.next_music_night);
        progress.setProgress(0);
        progress.setMax(100);
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.music_bar_icon);
        progress.setThumb(drawable);
        name.setTextColor(Color.parseColor("#AAFFFFFF"));
        singer.setTextColor(Color.parseColor("#AAFFFFFF"));
        duration.setTextColor(Color.parseColor("#AAFFFFFF"));
        position.setTextColor(Color.parseColor("#AAFFFFFF"));
        position.setText("00:00");
        duration.setText("05:00");
        name.setText("空闲中");
        singer.setText("长按我可能改变通知栏颜色");
    }

    private void setListener() {
        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((LocalMusicActivity) mContext).getMusicService() == null) return;
                try {
                    ((LocalMusicActivity) mContext).getMusicService().lastMusic();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((LocalMusicActivity) mContext).getMusicService() == null) return;
                try {
                    ((LocalMusicActivity) mContext).getMusicService().nextMusic();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    handler.removeCallbacks(updateProgress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (((LocalMusicActivity) mContext).getMusicService() == null) return;
                try {
                    ((LocalMusicActivity) mContext).getMusicService().setCurrentPosition(seekBar.getProgress());
                    handler.postDelayed(updateProgress, 100);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        controller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((LocalMusicActivity) mContext).getMusicService() == null) return;
                try {
                    if (((LocalMusicActivity) mContext).getMusicService().isPlaying())
                        ((LocalMusicActivity) mContext).getMusicService().pause();
                    else ((LocalMusicActivity) mContext).getMusicService().play();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setData() {
        try {
            MusicAidlInterface musicService = ((LocalMusicActivity) mContext).getMusicService();
            MusicInfo musicInfo = musicService.getPlayInfo();
            if (musicInfo != null) {
                name.setText(musicInfo.getName());
                singer.setText(musicInfo.getSinger());
                try {
                    controller.setImageResource(musicService.isPlaying() ? R.drawable.pause_night : R.drawable.play_night);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                int duration = musicService.getPlayDuration();
                int current = musicService.getCurrentPosition();
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                progress.setMax(duration);
                progress.setProgress(current);
                this.duration.setText(format.format(new Date(duration)));
                position.setText(format.format(new Date(current)));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateProgress() {
        try {
            if (((LocalMusicActivity) mContext).getMusicService() == null) {
                isProgressChanging = false;
                return;
            }
            if (((LocalMusicActivity) mContext).getMusicService().isPlaying()) {
                int current = ((LocalMusicActivity) mContext).getMusicService().getCurrentPosition();
                progress.setProgress(current);
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                position.setText(format.format(new Date(current)));
                handler.postDelayed(updateProgress, 100);
            } else {
                isProgressChanging = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((LocalMusicActivity) mContext).getMusicService() != null) setData();
        if (!isProgressChanging) {
            isProgressChanging = true;
            updateProgress();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isProgressChanging) {
            isProgressChanging = false;
            handler.removeCallbacks(updateProgress);
        }
    }

    @Override
    public void onPlayMusic() {
        if (((LocalMusicActivity) mContext).getMusicService() != null) setData();
        if (!isProgressChanging) {
            isProgressChanging = true;
            handler.postDelayed(updateProgress, 100);
        }
    }

    @Override
    public void onPauseMusic() {
        if (((LocalMusicActivity) mContext).getMusicService() != null) setData();
        if (isProgressChanging) {
            isProgressChanging = false;
            handler.removeCallbacks(updateProgress);
        }
    }

    @Override
    public void onNextSong() {
        if (((LocalMusicActivity) mContext).getMusicService() != null) setData();
        if (!isProgressChanging) {
            isProgressChanging = true;
            handler.postDelayed(updateProgress, 100);
        }
    }

    @Override
    public void onLastSong() {
        if (((LocalMusicActivity) mContext).getMusicService() != null) setData();
        if (!isProgressChanging) {
            isProgressChanging = true;
            handler.postDelayed(updateProgress, 100);
        }
    }

    @Override
    public void onProgressChange() {
        if (((LocalMusicActivity) mContext).getMusicService() != null) setData();
    }

    @Override
    public void onClearPlayList() {
        position.setText("00:00");
        duration.setText("05:00");
        name.setText("空闲中");
        singer.setText("长按我可能改变通知栏颜色");
        controller.setImageResource(R.drawable.play_night);
        progress.setProgress(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((LocalMusicActivity) mContext).removeMusicListener(this);
        handler.removeCallbacks(updateProgress);
    }
}
