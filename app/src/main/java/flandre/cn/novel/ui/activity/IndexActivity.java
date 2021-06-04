package flandre.cn.novel.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import flandre.cn.novel.adapter.adapter.activity.IndexAdapter;
import flandre.cn.novel.adapter.adapter.activity.PopUpAdapter;
import flandre.cn.novel.utils.parse.ShareParse;
import flandre.cn.novel.utils.tools.NovelTools;
import flandre.cn.novel.utils.database.SharedTools;
import flandre.cn.novel.ui.fragment.*;
import flandre.cn.novel.service.NovelService;
import flandre.cn.novel.utils.tools.NovelConfigure;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.adapter.decoration.Decoration;
import flandre.cn.novel.R;
import flandre.cn.novel.utils.database.SQLiteNovel;
import flandre.cn.novel.bean.data.activity.Item;

import java.io.*;
import java.util.*;

/**
 * Index页面
 * 2019.12.4
 */
public class IndexActivity extends BaseActivity implements PopUpAdapter.OnPopUpClickListener, AlarmDialogFragment.OnClickItemListener {
    public static final String CHANGE_THEME = "flandre.cn.novel.changetheme";
    public static final String LOAD_DATA = "flandre.cn.novel.loaddata";

    private IndexReceiver receiver;

    // 底部的三个选择卡
    private TextView[] text;
    private LinearLayout[] select;
    private ImageView[] image;

    private ViewPager pager;
    private DrawerLayout drawerLayout;
    private LinearLayout popLeft;  // 侧面弹出菜单
    private Toolbar bar;
    private LinearLayout gridLayout;
    private BookFragment bookFragment;
    private RankFragment rankFragment;
    private UserFragment userFragment;
    private List<Item> items;  // 适配器使用的数据
    private PopUpAdapter adapter;
    private ImageView imageView;  // 侧面弹出菜单头顶的图片
    private AlarmDialogFragment alarmDialogFragment;
    private boolean isPlayMusic = false;  // 是否在服务准备好时自动播放音乐
    private boolean isPlaying = false;  // 是否正在播放音乐

    private Handler handler;  // UI线程消息处理
    private long time;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 保存当前时间与播放的状态
        outState.putLong("SaveTime", new Date().getTime());
        if (musicService != null)
            outState.putBoolean("isPlaying", isPlaying);
        super.onSaveInstanceState(outState);
    }

    @Override
    void onServiceConnected(int service) {
        // 分配模式的意外退出时, 会自动播放音乐
        if (service == BaseActivity.MUSIC_SERVICE_CONNECTED && isPlayMusic) {
            try {
                musicService.play();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        unpackSave(savedInstanceState);
        setupMusicService();
        addMusicListener(this);
        setupReceiver();

        // 加载配置文件
        NovelConfigureManager.getConfigure(getApplicationContext());

        // 数据库连接
        SQLiteNovel.getSqLiteNovel(getApplicationContext());

        //设置线程
        setupHandler();

        setupValues();
        setupTool();
        setupPager(savedInstanceState);
        setTextListener();
        setupPopLeft();
        changePartly();
        setupNovelService();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.get("path") != null) {
            Bundle path = new Bundle();
            String s = (String) bundle.get("path");
            path.putString("path", s);
            bookFragment.setArguments(path);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.get("path") != null) {
            bookFragment.getRefresh().setRefreshing(true);
            String path = (String) bundle.get("path");
            new ShareParse(path, this).setOnfinishParse(bookFragment).parseFile(bookFragment.getRefresh());
        }
    }

    private void setupHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case 0x103:
                        bookFragment.deleteBook(message);
                        break;
                }
                return true;
            }
        });
    }

    private void unpackSave(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long time = new Date().getTime() - savedInstanceState.getLong("SaveTime");
            if (time < 10000 && savedInstanceState.getBoolean("isPlaying"))
                isPlayMusic = true;
        }
    }

    private void setupReceiver() {
        receiver = new IndexReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CHANGE_THEME);
        filter.addAction(LOAD_DATA);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onPlayMusic() {
        isPlaying = true;
    }

    @Override
    public void onPauseMusic() {
        isPlaying = false;
    }

    private void setupValues() {
        select = new LinearLayout[3];
        select[0] = findViewById(R.id.book_line);
        select[1] = findViewById(R.id.rank_line);
        select[2] = findViewById(R.id.user_line);

        text = new TextView[3];
        text[0] = findViewById(R.id.book);
        text[1] = findViewById(R.id.rank);
        text[2] = findViewById(R.id.user);

        image = new ImageView[3];
        image[0] = findViewById(R.id.book_image);
        image[1] = findViewById(R.id.rank_image);
        image[2] = findViewById(R.id.user_image);

        gridLayout = findViewById(R.id.tab);
        pager = findViewById(R.id.pager);
        popLeft = findViewById(R.id.left);
        bar = findViewById(R.id.tool);
        drawerLayout = findViewById(R.id.drawer);
        image[0].setSelected(true);
        text[0].setSelected(true);
    }

    private void setupTool() {
        setSupportActionBar(bar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    private void setupPager(Bundle savedInstanceState) {
        pager.setOffscreenPageLimit(2);
        IndexAdapter mainAdapter = new IndexAdapter(getSupportFragmentManager());
        if (savedInstanceState == null) {
            bookFragment = new BookFragment();
            rankFragment = new RankFragment();
            userFragment = new UserFragment();
        } else {
            bookFragment = (BookFragment) getSupportFragmentManager().findFragmentByTag(BookFragment.TAG);
            rankFragment = (RankFragment) getSupportFragmentManager().findFragmentByTag(RankFragment.TAG);
            userFragment = (UserFragment) getSupportFragmentManager().findFragmentByTag(UserFragment.TAG);
        }
        mainAdapter.addFragment(bookFragment, BookFragment.TAG);
        mainAdapter.addFragment(rankFragment, RankFragment.TAG);
        mainAdapter.addFragment(userFragment, UserFragment.TAG);
        pager.setAdapter(mainAdapter);
        pager.setCurrentItem(0);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switchTabs(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void setTextListener() {
        select[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(0);
            }
        });

        select[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(1);
            }
        });

        select[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(2);
            }
        });
    }

    private void setupPopLeft() {
        alarmDialogFragment = AlarmDialogFragment.newInstance("闹钟设置");
        alarmDialogFragment.setListener(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pop_left_layput, popLeft, false);
        imageView = view.findViewById(R.id.novel_img);
        imageView.setBackground(getResources().getDrawable(NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY
                ? R.drawable.novel_top_day : R.drawable.novel_top_night));
        RecyclerView recyclerView = view.findViewById(R.id.novel_rec);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new Decoration(this));
        adapter = new PopUpAdapter(R.layout.dialog_list, null);
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
        popLeft.addView(view);
        // 把PopLeft事件消化完, 不会交给其他处理(不写这个可能会当做点击了小说进入阅读界面)
        popLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        addItem();
    }

    private void addItem() {
        items = new ArrayList<>();
        if (NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY) {
            addItem("阅读时长", R.drawable.read_time_day);
            addItem("书籍管理", R.drawable.book_manage_day);
//            addItem("阅读记录", R.drawable.read_record_day);
            addItem("更改主题", R.drawable.setting_day);
            addItem("设置来源", R.drawable.source_day);
            addItem("小说闹钟", R.drawable.alarm_day);
            addItem("夜间模式", R.drawable.night);
            addItem("退出程序", R.drawable.exit_day);
        } else {
            addItem("阅读时长", R.drawable.read_time_night);
            addItem("书籍管理", R.drawable.book_manage_night);
//            addItem("阅读记录", R.drawable.read_record_night);
            addItem("更改主题", R.drawable.setting_night);
            addItem("设置来源", R.drawable.source_night);
            addItem("小说闹钟", R.drawable.alarm_night);
            addItem("日间模式", R.drawable.day);
            addItem("退出程序", R.drawable.exit_night);
        }
        adapter.setNewData(items);
    }

    private void addItem(String string, int id) {
        items.add(new Item(string, id));
    }

    private void changePartly() {
        pager.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        bar.setBackgroundColor(NovelConfigureManager.getConfigure().getMainTheme());
        popLeft.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        gridLayout.setBackgroundColor(NovelConfigureManager.getConfigure().getMainTheme());
    }

    @Override
    protected void onResume() {
        super.onResume();
        userFragment.changeTheme();

    }

    private void switchTabs(int position) {
        for (int i = 0; i < select.length; i++) {
            if (i == position) {
                text[i].setSelected(true);
                image[i].setSelected(true);
            } else {
                text[i].setSelected(false);
                image[i].setSelected(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 在ActionBar上添加菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.index_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                // 点击放大镜时去搜索页面
                Intent intent = new Intent(this, SearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(Gravity.LEFT);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0x1:
                changeTheme();
                break;
            case 0x2:
            case 0x3:
                userFragment.handleFile(requestCode, resultCode, data);
                break;

        }
    }

    @Override
    public void popUpClickListener(View view, int pos) {
        switch (pos) {
            case 0:
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(IndexActivity.this, ReadTimeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivityForResult(intent, 0x1);
                            }
                        }, 250);
                break;
            case 1:
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(IndexActivity.this, BookManageActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivityForResult(intent, 0x1);
                            }
                        }, 250);
                break;
            case 2:
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(IndexActivity.this, ConfigureThemeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivityForResult(intent, 0x1);
                            }
                        }, 250);
                break;
            case 3:
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(IndexActivity.this, ConfigureSourceActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                            }
                        }, 250);
                break;
            case 4:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alarmDialogFragment.show(getSupportFragmentManager(), "AlarmDialog");
                    }
                }, 250);
                break;
            case 5:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changeConfigure();
                    }
                }, 250);
                break;
            case 6:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                finish();
                break;
        }
        drawerLayout.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        long time = System.currentTimeMillis();
        if (time - this.time < 1000) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
            this.time = time;
        }
    }

    /**
     * 切换主题
     * 用户具有两套主题, 一套是默认的主题, 一套是夜间的主题
     */
    private void changeConfigure() {
        NovelConfigureManager.changeConfigure();
        try {
            NovelConfigureManager.saveConfigure(NovelConfigureManager.getConfigure(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        changeTheme();
    }

    /**
     * 根据配置文件马上更新主题
     */
    private void changeTheme() {
        changePartly();
        addItem();
        imageView.setBackground(getResources().getDrawable(NovelConfigureManager.getConfigure().getMode() ==
                NovelConfigure.DAY ? R.drawable.novel_top_day : R.drawable.novel_top_night));
        bookFragment.changeTheme();
        rankFragment.changeTheme();
        userFragment.changeTheme();
    }

    public BookFragment getBookFragment() {
        return bookFragment;
    }

    public Handler getHandler() {
        return handler;
    }

    public NovelService getService() {
        return mService;
    }

    public UserFragment getUserFragment() {
        return userFragment;
    }

    @Override
    public void clickItem(int pos) {
        SharedTools sharedTools = SharedTools.getSharedTools();
        switch (pos) {
            case 0:
                sharedTools.setAlarm(AlarmDialogFragment.NO_ALARM_STATE);
                sharedTools.setAlarmTime(AlarmDialogFragment.NO_ALARM_STATE);
                Toast.makeText(this, "取消了闹钟！", Toast.LENGTH_SHORT).show();
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                int time = 600 * pos * 1000;
                sharedTools.setAlarm(time);
                sharedTools.setAlarmTime(time);
                Toast.makeText(this, "闹钟将在" + NovelTools.resolver(time) + "后提示", Toast.LENGTH_SHORT).show();
                break;
            case 7:
                Toast.makeText(this, "开发者认为你不需要这个功能", Toast.LENGTH_SHORT).show();
                break;
        }
        userFragment.changeTheme();
    }

    class IndexReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case CHANGE_THEME:
                    changeTheme();
                    break;
                case LOAD_DATA:
                    if (bookFragment != null)
                        bookFragment.loadData();
                    break;
            }
        }
    }
}
