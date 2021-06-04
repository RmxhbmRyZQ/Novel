package flandre.cn.novel.ui.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.widget.*;
import com.github.promeg.pinyinhelper.Pinyin;
import flandre.cn.novel.MusicAidlInterface;
import flandre.cn.novel.R;
import flandre.cn.novel.adapter.adapter.activity.LocalMusicAdapter;
import flandre.cn.novel.adapter.decoration.Decoration;
import flandre.cn.novel.utils.tools.*;
import flandre.cn.novel.utils.database.SharedTools;
import flandre.cn.novel.ui.fragment.AlarmDialogFragment;
import flandre.cn.novel.ui.fragment.MusicControllerFragment;
import flandre.cn.novel.ui.fragment.MusicDialogFragment;
import flandre.cn.novel.bean.data.music.MusicInfo;
import flandre.cn.novel.utils.parse.ShareFile;
import flandre.cn.novel.service.PlayMusicService;
import flandre.cn.novel.ui.view.SlideBar;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static flandre.cn.novel.utils.tools.PermissionManager.*;
import static flandre.cn.novel.service.PlayMusicService.music_pos;

/**
 * 本地音乐播放
 * 2020.4.1
 */
@SuppressWarnings("ALL")
public class LocalMusicActivity extends BaseActivity implements
        AlarmDialogFragment.OnClickItemListener, LocalMusicAdapter.OnMusicCall {
    private LocalMusicAdapter mAdapter;
    private MusicDialogFragment mDialogFragment;
    private AlarmDialogFragment mAlarmDialogFragment;
    private Map<Long, MusicInfo> musicData;  // 所有的歌曲
    private MusicControllerFragment musicControlerFragment;  // 歌曲的控制组件
    private long nowSongId = -1;  // 当前播放的歌曲ID
    private Map<String, Integer> musicPosition;
    private RelativeLayout relativeLayout;
    private TextView permission;
    private SlideBar slideBar;
    private LinearLayoutManager mManager;

    private boolean sortBySongName;  // true表示按照歌手排序, false按照歌名排序
    private LinearLayout checkControl;
    private View sep;

    public MusicAidlInterface getMusicService() {
        return musicService;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 检查是否有播放音乐的权限
     */
    private void checkPermission() {
        // 当版本达到23时, 需要向用户申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_CODE);
        } else {
            SharedTools.getSharedTools().setMusicEnable(true);
            setupMusicService();
            loadData();
        }
    }

    private void checkPermission(final String permission, final int code) {
        final PermissionManager manager = new PermissionManager(this);
        // 如果已经具有权限直接跑业务逻辑
        if (manager.checkPermission(permission)) {
            this.permission.setVisibility(View.GONE);
            setupMusicService();
            loadData();
        } else if (manager.shouldShowRequestPermissionRationale(permission)) {
            this.permission.setVisibility(View.VISIBLE);
            this.permission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 如果是第二次申请权限, 向用户表名权限的用途
                    Snackbar snackbar = Snackbar.make(relativeLayout, CODE_INFO.get(code),
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, new View.OnClickListener() {  // 点击确定时询问
                                @Override
                                public void onClick(View view) {
                                    manager.askPermission(permission, code);
                                }
                            });
                    View view = snackbar.getView();
                    NovelConfigure configure = NovelConfigureManager.getConfigure(getApplicationContext());
                    ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(Color.parseColor("#AAFFFFFF"));
                    ((TextView) view.findViewById(R.id.snackbar_action)).setTextColor(Color.parseColor("#AAFFFFFF"));
                    view.setBackgroundColor(configure.getMainTheme());
                    snackbar.show();
                }
            });
        } else {
            // 第一次可以直接申请权限
            this.permission.setVisibility(View.VISIBLE);
            this.permission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manager.askPermission(permission, code);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);
        NovelConfigureManager.getConfigure(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("本地音乐播放");
        actionBar.setBackgroundDrawable(new ColorDrawable(NovelConfigureManager.getConfigure().getMainTheme()));
        actionBar.setDisplayHomeAsUpEnabled(true);
        relativeLayout = findViewById(R.id.total);
        relativeLayout.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        slideBar = findViewById(R.id.slideBar);
        permission = findViewById(R.id.permission);
        permission.setTextColor(NovelConfigureManager.getConfigure().getAuthorTheme());
        findViewById(R.id.control).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent();
                intent.setAction(PlayMusicService.NOTIFICATION_CHANGE);
                sendBroadcast(intent);
                return true;
            }
        });
        if (savedInstanceState == null) {
            musicControlerFragment = new MusicControllerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.control, musicControlerFragment, "ControlFragment").commit();
        } else {
            musicControlerFragment = (MusicControllerFragment) getSupportFragmentManager().findFragmentByTag("ControlFragment");
            getSupportFragmentManager().beginTransaction().attach(musicControlerFragment);
        }
        mDialogFragment = new MusicDialogFragment();

        slideBar.setTouchLetterListener(new SlideBar.OnTouchLetterListener() {
            @Override
            public void onTouchLetter(String letter) {
                if (musicPosition.get(letter) != null) {
                    int i = musicPosition.get(letter);
                    mManager.scrollToPositionWithOffset(i, 0);
                }
            }
        });
        TextView showLetter = findViewById(R.id.showLetter);
        showLetter.setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        slideBar.setShowLetter(showLetter);
        setupRecycle();
        setupCheckBox();
        addMusicListener(this);
        mAlarmDialogFragment = AlarmDialogFragment.newInstance("定时关闭");
        mAlarmDialogFragment.setListener(this);
        sortBySongName = SharedTools.getSharedTools().getSongSort();
        checkPermission();
    }

    private void setupRecycle() {
        mManager = new LinearLayoutManager(this);
        mManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView recyclerView = findViewById(R.id.music);
        recyclerView.setLayoutManager(mManager);
        recyclerView.addItemDecoration(new Decoration(this));
        recyclerView.setHasFixedSize(false);
        mAdapter = new LocalMusicAdapter(R.layout.music_list, null);
        mAdapter.setOnCall(this);
        mAdapter.bindToRecyclerView(recyclerView);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    slideBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupCheckBox() {
        checkControl = findViewById(R.id.checkControl);
        ImageView image = findViewById(R.id.image);
        final TextView addNext = findViewById(R.id.addNext);
        TextView cancel = findViewById(R.id.cancel);
        sep = findViewById(R.id.sep);

        image.setImageResource(NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY ?
                R.drawable.play_day : R.drawable.play_night);
        addNext.setTextColor(NovelConfigureManager.getConfigure().getNameTheme());
        cancel.setTextColor(NovelConfigureManager.getConfigure().getAuthorTheme());
        sep.setBackgroundColor(NovelConfigureManager.getConfigure().getIntroduceTheme() & 0x22FFFFFF | 0x22000000);

        addNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCheckBox();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCheckBox();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCheckBox();
            }
        });
    }

    /**
     * 加载音乐数据
     */
    private void loadData() {
        musicData = new HashMap<>();
        List<MusicInfo> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(uri, music_pos, "title != '' and _size > 1048576 and duration > 60000",
                null, "title_key");
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)).toLowerCase().trim();
            if (!(name.endsWith("mp3") || name.endsWith("MP3")))
                continue;
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setSongId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            musicInfo.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            musicInfo.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            musicInfo.setSinger(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            musicInfo.setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            if (musicInfo.getSinger().contains("<unknown>") && musicInfo.getName().contains("-")) {
                musicInfo.setSinger(musicInfo.getName().split("-")[0].trim());
                musicInfo.setName(musicInfo.getName().split("-")[1].trim());
            }
            list.add(musicInfo);
            musicData.put(musicInfo.getSongId(), musicInfo);
        }
        cursor.close();
        sort(list);
    }

    private void sort(List<MusicInfo> list) {
        musicPosition = new HashMap<>();
        for (MusicInfo musicInfo : musicData.values()) {
            musicInfo.setSort(Pinyin.toPinyin(sortBySongName ? musicInfo.getSinger().charAt(0) :
                    musicInfo.getName().charAt(0)).substring(0, 1).toUpperCase());
        }
        Collections.sort(list, new MusicComparator());
        // 整理每个字母对应的位置
        for (int i = 0; i < list.size(); i++) {
            String sort = list.get(i).getSort();
            int sortInt = sort.charAt(0);
            // 如果是数字则赋值为~
            if ((sortInt ^ 0b00110000) <= 9) sort = SlideBar.LETTER[0];
            else if (sortInt < 'A' || sortInt > 'Z') sort = SlideBar.LETTER[SlideBar.LETTER.length - 1];
            if (musicPosition.get(sort) == null)
                musicPosition.put(sort, i);
        }
        mAdapter.setNewData(list);
    }

    @Override
    void onServiceConnected(int service) {
        if (service == BaseActivity.MUSIC_SERVICE_CONNECTED) {
            // 当服务连接时, 为音乐控制组件设置数据
            musicControlerFragment.setData();
            if (!musicControlerFragment.isProgressChanging()) {
                musicControlerFragment.setProgressChanging(true);
                musicControlerFragment.updateProgress();
            }
            try {
                MusicInfo musicInfo = musicService.getPlayInfo();
                if (musicInfo != null) {
                    nowSongId = musicInfo.getSongId();
                    musicData.get(musicInfo.getSongId()).setPlaying(true);
                    mAdapter.notifyDataSetChanged();
                    mDialogFragment.adapterUpdate();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.music_menu, menu);
        final MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<MusicInfo> list = new ArrayList<>();
                if (s.equals("")) {
                    for (MusicInfo musicInfo : LocalMusicActivity.this.musicData.values())
                        list.add(musicInfo);
                } else {
                    for (MusicInfo musicInfo : LocalMusicActivity.this.musicData.values())
                        if (musicInfo.getName().contains(s) || musicInfo.getSinger().contains(s)) list.add(musicInfo);
                }
                sort(list);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (sortBySongName) {
            menu.findItem(R.id.songName).setVisible(false);
            menu.findItem(R.id.singerName).setVisible(true);
        } else {
            menu.findItem(R.id.songName).setVisible(true);
            menu.findItem(R.id.singerName).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.list:
                if (musicService != null && !mDialogFragment.isAdded()) {
                    try {
                        long[] queue = musicService.getPlayQueue();
                        List<MusicInfo> musicInfos = new ArrayList<>();
                        for (long l : queue) {
                            MusicInfo musicInfo = musicData.get(l);
                            if (musicInfo != null)
                                musicInfos.add(musicInfo);
                        }
                        mDialogFragment.setInfos(musicInfos);
                        mDialogFragment.show(getSupportFragmentManager(), "MusicDialog");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.alarm:
                if (mAlarmDialogFragment.isAdded()) break;
                mAlarmDialogFragment.show(getSupportFragmentManager(), "AlarmDialogFragment");
                break;
            case R.id.songName:
                SharedTools.getSharedTools().setSongSort(true);
                sortBySongName = true;
                sort(mAdapter.getData());
                invalidateOptionsMenu();
                break;
            case R.id.singerName:
                SharedTools.getSharedTools().setSongSort(false);
                sortBySongName = false;
                sort(mAdapter.getData());
                invalidateOptionsMenu();
                break;
        }
//        return super.onOptionsItemSelected(item);
        return true;
    }

    /**
     * 设置播放歌曲的左边有一个距行分隔条
     */
    private void setNowPlay() {
        if (nowSongId != -1 && musicService != null) {
            try {
                MusicInfo musicInfo = musicService.getPlayInfo();
                if (musicInfo != null) {
                    musicData.get(nowSongId).setPlaying(false);
                    musicData.get(musicInfo.getSongId()).setPlaying(true);
                    nowSongId = musicInfo.getSongId();
                    mAdapter.notifyDataSetChanged();
                    mDialogFragment.adapterUpdate();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNextSong() {
        setNowPlay();
    }

    @Override
    public void onLastSong() {
        setNowPlay();
    }

    @Override
    public void onClearPlayList() {
        if (nowSongId != -1) {
            musicData.get(nowSongId).setPlaying(false);
            mAdapter.notifyDataSetChanged();
            mDialogFragment.adapterUpdate();
            nowSongId = -1;
        }
    }

    @Override
    public void onPlayMusic() {
        if (nowSongId != -1)
            musicData.get(nowSongId).setPlaying(false);
        if (musicService != null) {
            try {
                MusicInfo musicInfo = musicService.getPlayInfo();
                if (musicInfo != null) {
                    nowSongId = musicInfo.getSongId();
                    musicData.get(musicInfo.getSongId()).setPlaying(true);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mAdapter.notifyDataSetChanged();
        mDialogFragment.adapterUpdate();
    }

    /**
     * 取消选择状态
     */
    private void cancelCheckBox() {
        mAdapter.setCheckEnable(false);
        mAdapter.getCheckPosition().clear();
        mAdapter.notifyDataSetChanged();
        sep.setVisibility(View.GONE);
        checkControl.setVisibility(View.GONE);
    }

    /**
     * 把选择的歌曲添加到播放列表
     */
    private void addCheckBox() {
        if (mAdapter.getCheckPosition().size() == 0) {
            cancelCheckBox();
            return;
        }
        try {
            for (Integer i : mAdapter.getCheckPosition()) {
                MusicInfo musicInfo = mAdapter.getData().get(i);
                if (!musicService.addPlayQueue(musicInfo.getSongId())) {
                    musicService.addPlayInfo(musicInfo.getSongId(), musicInfo);
                }
            }
            musicService.saveData();
            cancelCheckBox();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.isCheckEnable()) {
            cancelCheckBox();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_CODE:
                // 如果没有权限, 则关闭该功能
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    SharedTools.getSharedTools().setMusicEnable(false);
                    Toast.makeText(this, "我们需要读取权限才可以使用此功能", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    SharedTools.getSharedTools().setMusicEnable(true);
                    this.permission.setVisibility(View.GONE);
                    setupMusicService();
                    loadData();
                }
                break;
            case ACCESS_NOTIFICATION_POLICY_CODE:
                break;
        }
    }

    @Override
    public void clickItem(int pos) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent;
        PendingIntent pendingIntent;
        switch (pos) {
            case 0:
                intent = new Intent(PlayMusicService.NOTIFICATION_PAUSE);
                pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                alarmManager.cancel(pendingIntent);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.SECOND, 600 * pos);
                intent = new Intent(PlayMusicService.NOTIFICATION_PAUSE);
                pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Toast.makeText(this, "音乐将在" + NovelTools.resolver(600 * pos * 1000) + "后停止", Toast.LENGTH_SHORT).show();
                break;
            case 7:
                Toast.makeText(this, "开发者认为你不需要这个功能", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onPlayMusic(int position) {
        List<MusicInfo> data = mAdapter.getData();
        long[] queue = new long[data.size()];
        for (int i = 0; i < data.size(); i++) {
            queue[i] = data.get(i).getSongId();
        }
        try {
            long[] leak = musicService.setPlayQueue(queue);
            if (leak != null) {
                for (long l : leak)
                    musicService.addPlayInfo(l, musicData.get(l));
            }
            musicService.playTarget(data.get(position).getSongId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddMusic(MusicInfo musicInfo) {
        try {
            if (!musicService.addPlayQueue(musicInfo.getSongId())) {
                musicService.addPlayInfo(musicInfo.getSongId(), musicInfo);
            }
            // 第一个会自带保存功能所以不需要我们保存
            if (musicService.getPlayQueueSize() != 1) {
                musicService.saveData();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteMusic(MusicInfo musicInfo) {

        try {
            musicService.deletePlayQueue(musicInfo.getSongId());
//                                        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicInfo.getSongId());
//                                        getContentResolver().delete(uri, null, null);
            Toast.makeText(LocalMusicActivity.this, "已从播放列表里删除", Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShareMusic(MusicInfo musicInfo) {

        File file = new File(musicInfo.getData());
        if (!file.exists()) {
            Toast.makeText(LocalMusicActivity.this, "歌曲不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        new ShareFile(LocalMusicActivity.this).share(file, "*/*", "分享音乐");
    }

    @Override
    public void onSelectMusic() {
        checkControl.setVisibility(View.VISIBLE);
        sep.setVisibility(View.VISIBLE);
    }

    class MusicComparator implements Comparator<MusicInfo> {

        @Override
        public int compare(MusicInfo o1, MusicInfo o2) {
            String sort1 = o1.getSort();
            String sort2 = o2.getSort();
            if (isNone(sort1) && isNone(sort2))
                return 0;
            if (isNone(sort1))
                return -1;
            if (isNone(sort2))
                return 1;
            return sort1.compareTo(sort2);
        }

        boolean isNone(String sort) {
            return sort.trim().equals("");
        }
    }
}
