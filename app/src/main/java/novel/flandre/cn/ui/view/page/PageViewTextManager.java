package novel.flandre.cn.ui.view.page;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.bean.data.novel.NovelText;
import novel.flandre.cn.bean.data.novel.NovelTextItem;
import novel.flandre.cn.bean.data.novel.WrapperNovelText;
import novel.flandre.cn.bean.serializable.SelectList;
import novel.flandre.cn.net.Crawler;
import novel.flandre.cn.service.NovelService;
import novel.flandre.cn.ui.fragment.AlarmDialogFragment;
import novel.flandre.cn.utils.database.SQLTools;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.database.SharedTools;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.ArrayList;
import java.util.List;

import static novel.flandre.cn.ui.activity.BaseActivity.NOVEL_SERVICE_CONNECTED;
import static novel.flandre.cn.ui.fragment.AlarmTriggerDialogFragment.REST_TIME;

/**
 * 小说文本界面
 * 爬取下一章：当在{@link PageView#position}=3且进入下一章时加载章节,
 * {@link PageViewTextManager#crawlPosition}爬取位置是当前章节{@link PageViewTextManager#chapter}-2, 加载7章.
 * 爬取上一章：当在{@link PageView#position}=1且进入上一章时加载章节,
 * {@link PageViewTextManager#crawlPosition}爬取位置时当前章节{@link PageViewTextManager#chapter}-5, 加载7章.
 * 当全部文本加载好时, 会把当前的文本与章节替换, 并重设{@link PageView#position}
 * 2020.6.24
 */
public class PageViewTextManager implements PageView.PageTurn {
    public static final int BufferChapterCount = 7;

    private NovelInfo novelInfo;
    public List<NovelTextItem> list = null;  // 当小说没有被收藏时, 小说的章节以及网址
    private SQLiteNovel sqLiteNovel;
    private PageView pageView;
    private Activity activity;

    private NovelText[] novelTexts = new NovelText[BufferChapterCount];
    private int chapter;  // 当前章节

    private int flushCount = 0;  // 当前已经更新的章节数量
    private int crawlPosition;  // 根据爬取的章节调整后的位置
    private int crawlChapter;  // 爬取的章节
    private int lastChapter = 0;  // 最后一章的id
    private LoadTextListener loadTextListener;

    private String table;  // 表名

    private boolean emptyTable = false;  // 是否为空表
    private boolean flushFinish = true;  // 是否可以重新加载text
    private boolean showActionBar = true;  // 是否能展示ActionBar
    private boolean redirect = true;  // 是否从外部跳转而来
    private boolean loadIsAdd = false;  // 加载对话框是否加载
    private boolean showLoad = true;  // 是否显示对话框，用于区分是否是后台爬取数据
    private Handler handler;
    private NovelService mService;
    private Crawler mCrawler;
    private Crawler.OnRequestComplete<List<NovelTextItem>> listComplete = new Crawler.OnRequestComplete<List<NovelTextItem>>() {
        @Override
        public void onSuccess(List<NovelTextItem> data) {
            handler.obtainMessage(0x300, data).sendToTarget();
        }

        @Override
        public void onFail(Throwable e) {
            e.printStackTrace();
        }
    };

    public PageViewTextManager(PageView pageView, Activity activity) {
        this.pageView = pageView;
        this.activity = activity;
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(activity.getApplicationContext());

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case 0x300:
                        loadText(message);
                        break;
                    case 0x302:
                        replaceText(message);
                        break;
                    case 0x303:
                        redirectChapter(message);
                        break;
                }
                return true;
            }
        });
    }

    private Crawler getCrawler() {
        if (mCrawler == null) {
            if (novelInfo.getSource() == null) {
                mCrawler = NovelConfigureManager.getCrawler();
            } else {
                mCrawler = NovelConfigureManager.getCrawler(novelInfo.getSource());
            }
        }
        return mCrawler;
    }

    public void init(Bundle savedInstanceState, LoadTextListener loadTextListener) {
        this.loadTextListener = loadTextListener;
//        checkAlarm();
        unpack(savedInstanceState);
        setUpText(savedInstanceState);
    }

    public void checkAlarm(){
        SharedTools sharedTools = SharedTools.getSharedTools();
        if (System.currentTimeMillis() - sharedTools.getLastReadTime() > 1000 * 120) {  // 如果用户离开超过两分钟的话闹钟要重置
            sharedTools.setAlarm(sharedTools.getAlarmTime());
            sharedTools.setAlarmLeftTime(0);
        } else if (SharedTools.getSharedTools().getAlarmLeftTime() > 0) {  // 如果在闹钟读秒时把闹钟关掉那么在开启时会弹出闹钟
            loadTextListener.showAlarmDialog();
        }
    }

    private void unpack(Bundle savedInstanceState) {
        Bundle bundle;
        if (savedInstanceState == null)
            bundle = activity.getIntent().getExtras();
        else bundle = savedInstanceState;

        String name = bundle.getString("name");
        String author = bundle.getString("author");
        if (bundle.get("url") == null) {
            novelInfo = SQLTools.getNovelOneData(sqLiteNovel, name, author);
            table = novelInfo.getTable();
        } else {
            novelInfo = new NovelInfo();
            novelInfo.setId(-1);
            novelInfo.setName(name);
            novelInfo.setAuthor(author);
            novelInfo.setSource(bundle.getString("source"));
            novelInfo.setUrl(bundle.getString("url"));
            table = null;
        }
    }

    public List<NovelTextItem> getList() {
        return list;
    }

    private void setUpText(Bundle savedInstanceState) {
        String[] split;
        if (table == null) {
            if (savedInstanceState == null) {
                split = new String[]{"1", "1"};
                crawlChapter = chapter = Integer.parseInt(split[0]);
            } else {
                split = ((String) savedInstanceState.get("watch")).split(":");
                crawlChapter = Integer.parseInt(split[0]);
                chapter = Integer.parseInt(split[2]);
                list = ((SelectList) savedInstanceState.get("list")).getList();
                novelTexts = (NovelText[]) savedInstanceState.getSerializable("NovelText");
                crawlPosition = Integer.parseInt(split[3]);
            }
            pageView.setWatch(Integer.parseInt(split[1]));
            // 当Activity处于恢复时, 不需要再次从网上下载信息, 直接使用以前的
            if (list != null) {
                flushFinish = false;
                flushCount = 7;
                handler.obtainMessage(0x302, null).sendToTarget();
                return;
            }
            crawlPosition = crawlChapter;
            // 获取目录列表
            setLoad();
            getCrawler().list(novelInfo.getUrl(), null, listComplete);
        } else {
            if (savedInstanceState == null) {
                split = (novelInfo.getWatch()).split(":");
                crawlChapter = chapter = Integer.parseInt(split[0]);
                crawlPosition = crawlChapter > 1 ? crawlChapter - 1 : crawlChapter;
            } else {
                split = ((String) savedInstanceState.get("watch")).split(":");
                crawlChapter = Integer.parseInt(split[0]);
                chapter = Integer.parseInt(split[2]);
                crawlPosition = Integer.parseInt(split[3]);
            }
            pageView.setWatch(Integer.parseInt(split[1]));

            handler.sendEmptyMessage(0x300);
        }
    }

    public void choiceChapter(int i) {
        Message message = handler.obtainMessage(0x303, i);
        handler.sendMessage(message);
    }

    public void onSaveInstanceState(Bundle outState) {
        // 把观看的进度保存了
        outState.putString("watch", crawlChapter + ":" + pageView.getWatch() + ":" + chapter + ":" + crawlPosition);
        outState.putBoolean("star", table != null);
        outState.putString("name", novelInfo.getName());
        outState.putString("author", novelInfo.getAuthor());
        if (table == null) {
            outState.putSerializable("NovelText", pageView.getDrawText());
            SelectList<NovelTextItem> selectList = new SelectList<>();
            selectList.setList(list);
            outState.putSerializable("list", selectList);
            outState.putString("source", novelInfo.getSource());
            outState.putString("url", novelInfo.getUrl());
        }
    }

    public void onServiceConnected(int service) {
        // 成功把代码写的自己也看不懂, 这串代码是为了防止, 更新小说时瞬间点进来观看, 会把章节成倍放入数据库(应该没人会这么做)
        // 当小说服务连接时是空表时, 不是空表表示表不为空, 或者还没运行到加载文本的代码(转到加载文本时再处理)
        if (service == NOVEL_SERVICE_CONNECTED && emptyTable) {
            emptyTable = false;
            // 如果更新的小说是自己, 且已经更新完了, 那么发送信息去加载文本
            // 如果还没有更新完, 就添加监听器, 等它更新完再发送信息去加载文本
            if (mService.isContainId(novelInfo.getId())) {
                if (isUpdateFinish()) handler.sendEmptyMessage(0x300);
            } else {
                // 当更新的小说不是自己, 运行原来的程序
                handler.sendEmptyMessage(0x300);
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public NovelInfo getNovelInfo() {
        return novelInfo;
    }

    @Override
    public int getChapter() {
        return chapter;
    }

    public String getTable() {
        return table;
    }

    public void setService(NovelService service) {
        this.mService = service;
    }

    /**
     * 更新text
     * 如果load为true,更新完后显示出来
     */
    private void loadText(Message message) {
        Crawler crawler = getCrawler();
        // 当没有收藏图书时,获取列表数据,然后爬取text
        if (table == null) {
            handleListText(crawler, message);
        } else {
            // 收藏情况下的文本处理
            handleTableText(crawler, message);
        }
    }

    private void setLoad() {
        if (!loadIsAdd) {
            loadIsAdd = true;
            loadTextListener.showLoadDialog();
        }
    }

    public boolean isLoadIsAdd() {
        return loadIsAdd;
    }

    /**
     * 更新一条text文本
     * message.arg1: 更新的位置
     * message.obj: 更新的文本与章节
     */
    private void replaceText(Message message) {
        String table = null;
        int position = message.arg1;
        NovelText novelText;
        if (message.obj instanceof WrapperNovelText) {
            table = ((WrapperNovelText) message.obj).getTable();
            novelText = ((WrapperNovelText) message.obj).getNovelText();
        } else {
            novelText = (NovelText) message.obj;
        }
        // 如果obj时null表示小说未收藏时Activity的恢复状态
        if (message.obj != null) {
            novelTexts[position] = novelText;
            flushCount++;
        }
        // 当它们相等时, 表示此次text更新完成
        if (flushCount == BufferChapterCount) {
            // 首次加载最后章节
            if (lastChapter == 0) {
                if (this.table != null) {
                    Cursor cursor = sqLiteNovel.getReadableDatabase().query(this.table, null, null,
                            null, null, null, null);
                    lastChapter = cursor.getCount();
                    cursor.close();
                } else {
                    lastChapter = list.size();
                }
                pageView.setLastChapter(lastChapter);
            }
            // 更新章节文件
            pageView.updateText(novelTexts, chapter - crawlPosition);
            // 当展示了对话框且对话框已经在展示了把对话框去掉
            if (loadIsAdd) {
                loadTextListener.cancelLoadDialog();
                loadIsAdd = false;
            }
            if (redirect) {
                pageView.flashWatch();
                loadTextListener.onRedirect();
                redirect = false;
            }
            flushFinish = true;
        }
        if (message.arg2 != 1 && sqLiteNovel.freeStatus && table != null && !novelText.getText().equals("")) {
            ContentValues values = new ContentValues();
            values.put("text", novelTexts[position].getText());
            sqLiteNovel.getReadableDatabase().update(table, values, "id=?", new String[]{String.valueOf(crawlPosition + position)});
        }
    }

    /**
     * 章节跳转
     * message.obj: 跳转的位置
     */
    private void redirectChapter(Message message) {
        int id = (Integer) message.obj;
        crawlChapter = chapter = id;

        if (chapter != 1) {
            crawlPosition = crawlChapter - 1;
        } else {
            crawlPosition = crawlChapter;
        }

        flushCount = 0;
        showActionBar = true;
        flushFinish = false;
        redirect = true;
        showLoad = true;
        pageView.setWatch(1);
        pageView.setPageEnable(false);
        pageView.setMode(PageView.REDIRECT);

        handler.sendEmptyMessage(0x300);
    }

    private void handleListText(Crawler crawler, Message message) {
        boolean load = showLoad;
        showLoad = false;
        // 第一次加载时, 会加载章节
        if (list == null) {
            if (message.obj == null) {
                Toast.makeText(activity, "网络错误", Toast.LENGTH_SHORT).show();
                activity.finish();
                return;
            }
            list = (List<NovelTextItem>) message.obj;
        }
        // 爬取章节=当前章节-2, 爬取章节等于总章节-2, 表示当前章节是最后一章
        if (crawlPosition == list.size() - 2 && !redirect) {
//                Toast.makeText(TextActivity.this, "最后一章了", Toast.LENGTH_SHORT).show();
            flushFinish = true;
            return;
        }
        if (crawlPosition > list.size() - 6) {
            showLoad = load;
            crawlPosition = list.size() - 6;
            handler.sendEmptyMessage(0x300);
            return;
        }
        if (load) setLoad();
        for (int i = 0; i < 7; i++) {
            crawler.text(list.get(crawlPosition - 1 + i).getUrl(), table, null, new TextComplete(i));
        }
    }

    private void handleTableText(Crawler crawler, Message message) {
        boolean load = showLoad;
        showLoad = false;
        if (emptyTable) {
            // 若表为空时,把本书的所有章节都添加进数据库(不包括文本)
            if (message.obj == null) {
                Toast.makeText(activity, "网络错误", Toast.LENGTH_SHORT).show();
                activity.finish();
                return;
            }
            list = (List<NovelTextItem>) message.obj;
            sqLiteNovel.freeStatus = false;
            new NewTableTask().execute();
            emptyTable = false;
            if (crawlPosition > list.size() - 6) crawlPosition = list.size() - 6;
            if (chapter > list.size()) chapter = list.size();
            pageView.setWatch(1);
            // 爬取文本
            for (int i = 0; i < 7; i++) {
                crawler.text(list.get(crawlPosition - 1 + i).getUrl(), table, null, new TextComplete(i));
            }
        } else {
            // 拿到表数据若有文字了,就直接使用,没有从网上爬取
            Cursor cursor = sqLiteNovel.getReadableDatabase().query(table, new String[]{"url", "chapter", "text", "id"},
                    "id>=?", new String[]{crawlPosition + ""}, null, null,
                    null, String.valueOf(BufferChapterCount));
            // 表里面是否存在该章节
            if (cursor.moveToNext()) {
                int count = -1;
                List<Object> data = new ArrayList<>();
                do {
                    count++;
                    if (cursor.getString(2) == null) {
                        NovelTextItem novelTextItem = new NovelTextItem();
                        novelTextItem.setUrl(cursor.getString(0));
                        novelTextItem.setChapter(cursor.getString(1));
                        data.add(novelTextItem);
                    } else {
                        NovelText novelText = new NovelText();
                        novelText.setText(cursor.getString(2));
                        novelText.setChapter(cursor.getString(1));
                        data.add(novelText);
                    }
                } while (cursor.moveToNext());
                cursor.close();
                // 最后一章时阻止溢出
                if (count == 2 && !redirect) {
                    // Toast.makeText(TextActivity.this, "最后一章了", Toast.LENGTH_SHORT).show();
                    // SQLTools.setFinishTime((String) map.get("id"), sqLiteNovel, this);
                    flushFinish = true;
                    return;
                }
                // 使章节缓存总是够7章(当前=末尾-2->当前=末尾-6), BUG(当小说总体章数不足以7章时会发生什么)
                if (count < BufferChapterCount - 1) {
                    showLoad = load;
                    crawlPosition = crawlPosition - (BufferChapterCount - count - 1);
                    handler.sendEmptyMessage(0x300);
                    return;
                }
                // 如果存在文本直接拿, 没有从网上爬
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i) instanceof NovelTextItem) {
                        if (load) {
                            setLoad();
                        }
                        crawler.text(((NovelTextItem) data.get(i)).getUrl(), table, null, new TextComplete(i));
                    } else {
                        Message msg = handler.obtainMessage(0x302, i, 1, data.get(i));
                        handler.sendMessage(msg);
                    }
                }
            } else {
                cursor.close();
                setLoad();
                // 表为空时从网上爬取目录进行填充
                emptyTable = true;
                // 当服务已经加载好了
                if (mService != null) {
                    // 如果更新的小说是自己...., 不是 直接加载目录
                    if (mService.isContainId(novelInfo.getId())) {
                        if (isUpdateFinish()) {
                            // 为了避免更新全部后, 收藏一本新的然后直接观看会死循环的情况, 要先判断里面是否有数据
                            Cursor count = sqLiteNovel.getReadableDatabase().query(table, null, null,
                                    null, null, null, null);
                            // 有数据直接加载文本, 否则先加载目录
                            if (count.getCount() != 0) {
                                emptyTable = false;
                                handler.sendEmptyMessage(0x300);
                            } else {
                                crawler.list(novelInfo.getUrl(), null, listComplete);
                            }
                            count.close();
                        }
                    } else {
                        crawler.list(novelInfo.getUrl(), null, listComplete);
                    }
                }
            }
        }
    }

    /**
     * 返回当前小说是否更新完
     */
    private boolean isUpdateFinish() {
        return !mService.addUpdateListener(new NovelService.UpdateNovel() {
            @Override
            public void onUpdateStart() {

            }

            @Override
            public void onUpdateFail() {

            }

            @Override
            public void onUpdateFinish(int updateFinish, int updateCount, int id) {
                if (id == novelInfo.getId()) {
                    emptyTable = false;
                    handler.sendEmptyMessage(0x300);
                }
            }
        });
    }

    @Override
    public boolean onLastPage(int position, boolean isPositionChange, boolean isPageEnable, boolean isNowOverflow) {
        if (isPositionChange) {
            chapter--;
        }
        // 当textPosition为1时(上面有textPosition--), 加载文本
        if (position < 1 && flushFinish && crawlPosition != 1) {
            flushCount = 0;
            if (chapter > BufferChapterCount - 2) {
                crawlChapter = chapter;
                crawlPosition = crawlChapter - BufferChapterCount + 2;
            } else {
                crawlPosition = 1;
            }
            Message msg = handler.obtainMessage(0x300);
            handler.sendMessage(msg);
            flushFinish = false;
        }
        boolean ret = chapter == 1 && isNowOverflow;
        if (!isPageEnable && !ret) {
            setLoad();
        }
        return ret;
    }

    @Override
    public boolean onNextPage(int position, boolean isPositionChange, boolean isPageEnable, boolean isNowOverflow) {
        // 如果翻页了把这里的章节进行增加
        if (isPositionChange) {
            chapter++;
        }
        // 当观看的位置超过了限制时, 且刷新已经完成时, 进行章节缓冲的更换
        if (position > BufferChapterCount - 3 && flushFinish && lastChapter > 7) {
            flushCount = 0;
            crawlChapter = chapter;
            crawlPosition = crawlChapter - 2;
            Message message = handler.obtainMessage(0x300);
            handler.sendMessage(message);
            flushFinish = false;
        }
        boolean ret = chapter == lastChapter && isNowOverflow;
        // 当不是最后一章但是看到最后一页时, 显示加载中对话框
        if (!isPageEnable && !ret) {
            setLoad();
        }
        if (ret) SQLTools.setFinishTime(String.valueOf(novelInfo.getId()), sqLiteNovel);
        return ret;
    }

    @Override
    public void onShowAction() {
        if (showActionBar) {
            loadTextListener.showActionBar();
            showActionBar = false;
        } else {
            loadTextListener.cancelActionBar();
            showActionBar = true;
        }
    }

    /**
     * 把当前观看进度放入数据库,并更新页面
     */
    @Override
    public synchronized void onUpdateWatch(long addTime, int watch) {
        SharedTools sharedTools = SharedTools.getSharedTools();
        sharedTools.setReadTime(addTime);
        long alarm = sharedTools.getAlarm();
        // 存在闹钟时
        if (alarm != AlarmDialogFragment.NO_ALARM_STATE)
            if (alarm - addTime <= 0) {
                NovelConfigure configure = NovelConfigureManager.getConfigure();
                if (configure.isConstantAlarm())
                    sharedTools.setAlarm(sharedTools.getAlarmTime() + (configure.isAlarmForce() ? REST_TIME * 1000 : 0));  // 如果会强制停留120秒就给闹钟多加120秒
                else {
                    sharedTools.setAlarm(AlarmDialogFragment.NO_ALARM_STATE);
                    sharedTools.setAlarmTime(AlarmDialogFragment.NO_ALARM_STATE);
                }
                loadTextListener.showAlarmDialog();
            } else sharedTools.setAlarm(alarm - addTime);
        sharedTools.setTodayRead(addTime);
        if (sqLiteNovel.freeStatus && table != null) {
            SQLTools.setRead(String.valueOf(novelInfo.getId()), sqLiteNovel, addTime, chapter, watch);
        }
    }

    private class TextComplete implements Crawler.OnRequestComplete<WrapperNovelText>{
        private int index;

        public TextComplete(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public void onSuccess(WrapperNovelText data) {
            handler.obtainMessage(0x302, index, 0, data).sendToTarget();
        }

        @Override
        public void onFail(Throwable e) {
            e.printStackTrace();
            WrapperNovelText wrapperNovelText = new WrapperNovelText();
            NovelText novelText = new NovelText();
            novelText.setText("网络出现异常");
            novelText.setChapter("网络出现异常");
            wrapperNovelText.setNovelText(novelText);
            handler.obtainMessage(0x302, index, 1, wrapperNovelText).sendToTarget();
        }
    }

    class NewTableTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SQLiteDatabase database = sqLiteNovel.getReadableDatabase();
            database.beginTransaction();
            for (NovelTextItem textItem : list) {
                database.execSQL("insert into " + table +
                        " (url, chapter) values (?, ?)", new String[]{textItem.getUrl(), textItem.getChapter()});
            }
            database.setTransactionSuccessful();
            database.endTransaction();
            sqLiteNovel.freeStatus = true;
            return null;
        }
    }

    public interface LoadTextListener {
        public void showLoadDialog();

        public void cancelLoadDialog();

        public void showActionBar();

        public void cancelActionBar();

        public void showAlarmDialog();

        public void onRedirect();
    }
}
