package flandre.cn.novel.ui.activity;

import android.content.Intent;
import android.os.*;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import flandre.cn.novel.utils.database.SharedTools;
import flandre.cn.novel.ui.fragment.AlarmTriggerDialogFragment;
import flandre.cn.novel.bean.data.novel.NovelChapter;
import flandre.cn.novel.bean.data.novel.NovelInfo;
import flandre.cn.novel.service.NovelService;
import flandre.cn.novel.R;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.utils.database.SQLTools;
import flandre.cn.novel.utils.database.SQLiteNovel;
import flandre.cn.novel.ui.fragment.LoadDialogFragment;
import flandre.cn.novel.ui.fragment.TextPopupFragment;
import flandre.cn.novel.utils.tts.TTSController;
import flandre.cn.novel.ui.view.page.AutoMove;
import flandre.cn.novel.ui.view.page.PageAnimation;
import flandre.cn.novel.ui.view.page.PageView;
import flandre.cn.novel.ui.view.page.PageViewTextManager;

import java.util.*;

/**
 * 2019.12.7
 */
public class TextActivity extends BaseActivity implements PageViewTextManager.LoadTextListener {
    private NovelInfo novelInfo;
    private SQLiteNovel sqLiteNovel;
    private PageView pageView;
    private AutoMove mAutoMove;
    private FrameLayout popup;  // 点击中间时的弹出菜单
    private LoadDialogFragment loadDialogFragment;  // 加载页面
    public TextPopupFragment fragment;
    private AlarmTriggerDialogFragment dialogFragment;
    private Handler mHandler;
    private TTSController mTtsController;

    private String table;  // 表名
    private PageViewTextManager pageViewTextManager;

    public NovelInfo getNovelInfo() {
        return novelInfo;
    }

    public String getTable() {
        return table;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        setupNovelService();
        setupMusicService();

        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(getApplicationContext());

        NovelConfigureManager.getConfigure(getApplicationContext());

        setUpView(savedInstanceState);

        novelInfo = pageViewTextManager.getNovelInfo();
        table = pageViewTextManager.getTable();
        mHandler = new Handler();

        if (table != null) SQLTools.setStartTime(String.valueOf(novelInfo.getId()), sqLiteNovel);
    }

    public NovelService getService() {
        return mService;
    }

    private void setUpView(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            fragment = new TextPopupFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.popup, fragment, "TextPopupFragment");
            transaction.commit();
        } else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragment = (TextPopupFragment) getSupportFragmentManager().findFragmentByTag("TextPopupFragment");
            fragmentTransaction.attach(fragment);
        }
        dialogFragment = new AlarmTriggerDialogFragment();
        loadDialogFragment = new LoadDialogFragment();
        loadDialogFragment.setCancelable(false);

        popup = findViewById(R.id.popup);
        RelativeLayout relativeLayout = findViewById(R.id.viewPage);
        pageView = new PageView(this);
        PageAnimation pageAnimation = NovelConfigureManager.getPageAnimation(pageView);
        pageView.setPageAnimation(pageAnimation);

        pageViewTextManager = new PageViewTextManager(pageView, this);
        pageViewTextManager.init(savedInstanceState, this);

        pageView.setOnPageTurnListener(pageViewTextManager);
        pageView.setPadding(26, 20, 23, 20);
        relativeLayout.addView(pageView);
    }

    @Override
    void onServiceConnected(int service) {
        if (service == NOVEL_SERVICE_CONNECTED) pageViewTextManager.setService(mService);
        pageViewTextManager.onServiceConnected(service);
    }

    public List<? extends NovelChapter> getList() {
        return pageViewTextManager.getList();
    }

    public int getChapter() {
        return pageViewTextManager.getChapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAutoMove != null) mAutoMove.move();
        // 更改主题
        changeTheme();
        pageView.setTime(new Date().getTime());
        pageViewTextManager.checkAlarm();
    }

    public void choiceChapter(int i) {
//        if (mAutoMove != null) mAutoMove.restore();
        popup.setVisibility(View.GONE);
        if (i != 0) {
            pageViewTextManager.choiceChapter(i);
        }
    }

    public void startAuto() {
        if (mAutoMove == null)
            mAutoMove = new AutoMove(pageView, mHandler);
        mAutoMove.prepare();
        pageViewTextManager.onShowAction();
    }

    public void stopAuto() {
        mAutoMove.cancel();
        mAutoMove = null;
    }

    public void speaker() {
        if (mAutoMove != null) fragment.autoMove();
        if (mTtsController == null) mTtsController = new TTSController(pageView, this);
        mTtsController.start();
        pageViewTextManager.onShowAction();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        pageViewTextManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * 音量键换页
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return pageView.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // 如果处于语言播报时，那么先停下
        if (mTtsController != null) mTtsController.stop();
        super.onBackPressed();
    }

    public void changeTheme() {
        pageView.setTextSize(NovelConfigureManager.getConfigure().getTextSize());
        pageView.setTextColor(NovelConfigureManager.getConfigure().getTextColor());
        pageView.setDescriptionColor(NovelConfigureManager.getConfigure().getChapterColor());
        pageView.setColor(NovelConfigureManager.getConfigure().getBackgroundColor());
        pageView.setAlwaysNext(NovelConfigureManager.getConfigure().isAlwaysNext());
        pageView.update();
        if (mAutoMove == null && mTtsController == null &&
                // 当翻页动画改变时要重新设置翻页的类
                !pageView.getPageAnimation().getClass().getName().equals(NovelConfigureManager.getConfigure().getNowPageView())) {
            pageView.setPageAnimation(NovelConfigureManager.getPageAnimation(pageView));
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 当加载窗弹起时，防止用音量控制翻页
        if (pageViewTextManager.isLoadIsAdd() && (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAutoMove != null) mAutoMove.pause();
        SharedTools.getSharedTools().setLastReadTime();  // 设置本小说最后阅读时间
        if (table != null) {
            SQLTools.setTime(novelInfo.getId(), sqLiteNovel);
            sendBroadcast(new Intent(IndexActivity.LOAD_DATA));  // 调动IndexActivity的loadData
        }
    }

    @Override
    public void showLoadDialog() {
        loadDialogFragment.show(getSupportFragmentManager(), "LoadDialog");
    }

    @Override
    public void cancelLoadDialog() {
        loadDialogFragment.dismiss();
    }

    @Override
    public void showActionBar() {
        popup.setVisibility(View.VISIBLE);
        if (mAutoMove != null) {
            mAutoMove.pause();
        }
    }

    @Override
    public void cancelActionBar() {
        popup.setVisibility(View.GONE);
        if (mAutoMove != null) {
            mAutoMove.move();
        }
    }

    @Override
    public void showAlarmDialog() {
        if (dialogFragment.isAdded()) return;  // 防止重复添加错误
        int alarmLeftTime = SharedTools.getSharedTools().getAlarmLeftTime();
        if (alarmLeftTime != 0) dialogFragment.setRestTime(alarmLeftTime);  // 设置剩余时间
        dialogFragment.setForce(NovelConfigureManager.getConfigure().isAlarmForce());
        dialogFragment.show(getSupportFragmentManager(), "AlarmTrigger");
        if (mAutoMove != null) {  // 自动阅读时会停止自动阅读
            mAutoMove.cancel();
            mAutoMove = null;
        }
        if (mTtsController != null) mTtsController.pause();  // AI 朗读时会停止朗读
    }

    @Override
    public void onRedirect() {
        if (mAutoMove != null) mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAutoMove != null) mAutoMove.move();
            }
        }, 2000);
    }
}
