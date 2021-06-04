package flandre.cn.novel.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.net.Crawler;
import flandre.cn.novel.utils.database.SQLTools;
import flandre.cn.novel.utils.database.SQLiteNovel;
import flandre.cn.novel.bean.data.novel.NovelDownloadInfo;
import flandre.cn.novel.bean.data.novel.NovelInfo;
import flandre.cn.novel.bean.data.novel.NovelText;
import flandre.cn.novel.bean.data.novel.NovelTextItem;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

/**
 * 提供小说的下载和更新
 * 2020.3.20
 */
public class NovelService extends Service implements Crawler.DownloadFinish, Crawler.UpdateFinish {
    public static final String DOWNLOAD_FINISH = "flandre.cn.novel.downloadfinish";
    public static final String DOWNLOAD_FAIL = "flandre.cn.novel.downloadfail";

    public static final int UPDATE_ALL = -1;  // 更新全部
    public static final int DOWNLOAD_ALL = -1;  // 下载全部
    private static final long SEPARATOR = 500;  // 下载时切换状态的间隔

    private static final int ON_UPDATE_START = 0;
    private static final int ON_UPDATE_FAIL = 1;
    private static final int ON_UPDATE_FINISH = 2;

    private IBinder mBinder;

    private boolean downloadEnable = true;  // 下载是否可用
    private boolean isStartDownload = false;  // 是否已经再下载了
    private NovelDownloadInfo downloadInfo;  // 下载的信息
    private ExecutorService downloadPool;  // 下载的线程池

    private int updateCount;  // 更新的数量
    private boolean updateEnable = true;  // 更新是否可用
    private int updateFinish;  // 更新已完成的数量
    private List<UpdateNovel> updateNovel = new ArrayList<>();
    private List<Integer> containId = new ArrayList<>();

    private Handler handler;
    private SQLiteNovel sqLiteNovel;

    public NovelService() {

    }

    public class NovelBind extends Binder {
        public NovelService getService() {
            return NovelService.this;
        }
    }

    @Override
    public void onCreate() {
        mBinder = new NovelBind();
        Context mContext = getBaseContext();
        handler = new Handler(mContext.getApplicationContext().getMainLooper());
        downloadInfo = new NovelDownloadInfo();
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(mContext.getApplicationContext());
        ContentValues values = new ContentValues();
        values.put("status", SQLiteNovel.DOWNLOAD_CONTINUE);
        sqLiteNovel.getReadableDatabase().update("download", values, "status = ?", new String[]{String.valueOf(SQLiteNovel.DOWNLOAD_PAUSE)});
        sqLiteNovel.getReadableDatabase().update("download", values, "status = ?", new String[]{String.valueOf(SQLiteNovel.DOWNLOAD_WAIT)});
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public String getDownloadTable() {
        return downloadInfo.getTable();
    }

    public boolean download(String novelId) {
        return download(novelId, DOWNLOAD_ALL);
    }

    public boolean download(String novelId, int limit) {
        return download(novelId, limit, null);
    }

    public boolean download(NovelDownloadInfo downloadInfo) {
        return download(null, 0, downloadInfo);
    }

    /**
     * 下载小说
     *
     * @param novelId           novel的id
     * @param limit             下载多少个, -1是下载全部, 只有novelDownloadInfo为null是才有用
     * @param novelDownloadInfo 下载的信息, 如果为null, 就是下载新的内容
     * @return 下载 or 添加
     */
    private boolean download(final String novelId, final int limit, final NovelDownloadInfo novelDownloadInfo) {
        if (downloadEnable && !isStartDownload) {
            downloadEnable = false;
            final String id = novelId != null ? novelId : String.valueOf(novelDownloadInfo.getNovelId());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isStartDownload = true;
                    startDownload(id, limit, novelDownloadInfo);
                }
            }, SEPARATOR);
            return true;
        } else {
            if (novelId != null) handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addDownload(novelId, limit);
                }
            }, SEPARATOR);
            return false;
        }
    }

    /**
     * 添加下载的等待列表
     *
     * @param novelId novel的id
     * @param limit   下载的数量, -1表示下载全部
     */
    private void addDownload(String novelId, int limit) {
        NovelDownloadInfo downloadInfo = new NovelDownloadInfo();
        NovelInfo novelInfo = SQLTools.getNovelOneData(sqLiteNovel, novelId);
        downloadInfo.setNovelId(Integer.parseInt(novelId));
        String chapter = novelInfo.getWatch().split(":")[0];
        Cursor data;
        if (limit == NovelService.DOWNLOAD_ALL)
            data = sqLiteNovel.getReadableDatabase().query(novelInfo.getTable(), new String[]{"id"},
                    "id >= ? and text is null", new String[]{chapter}, null, null, null);
        else
            data = sqLiteNovel.getReadableDatabase().query(novelInfo.getTable(), new String[]{"id", "url"},
                    "id >= ? and text is null", new String[]{chapter}, null, null, null, String.valueOf(limit));
        downloadInfo.setCount(data.getCount());
        data.moveToLast();
        downloadInfo.setLastId(data.getInt(0));
        downloadInfo.setTable(novelInfo.getName());
        downloadInfo.setFinish(0);
        downloadInfo.setNovelId(Integer.parseInt(novelId));
        saveDownload(downloadInfo, SQLiteNovel.DOWNLOAD_WAIT, true);
        data.close();
    }

    /**
     * 执行小说的下载
     */
    private void startDownload(String novelId, int limit, NovelDownloadInfo novelDownloadInfo) {
        downloadInfo.setNovelId(Integer.parseInt(novelId));

        // 拿到当前的观看位置
        NovelInfo novelInfo = SQLTools.getNovelOneData(sqLiteNovel, novelId);
        downloadInfo.setTable(novelInfo.getName());
        String chapter = novelInfo.getWatch().split(":")[0];
        String table = novelInfo.getTable();

        // 拿到没有文本的章节
        Cursor data;
        if (novelDownloadInfo != null)
            data = sqLiteNovel.getReadableDatabase().query(table, new String[]{"id", "url"}, "id <= ? and text is null",
                    new String[]{String.valueOf(novelDownloadInfo.getLastId())}, null, null, null);
        else if (limit == NovelService.DOWNLOAD_ALL)
            data = sqLiteNovel.getReadableDatabase().query(table, new String[]{"id", "url"},
                    "id >= ? and text is null", new String[]{chapter}, null, null, null);
        else
            data = sqLiteNovel.getReadableDatabase().query(table, new String[]{"id", "url"},
                    "id >= ? and text is null", new String[]{chapter}, null, null, null, String.valueOf(limit));

        boolean exist = data.moveToNext();
        if (exist) {
            List<Map<String, String>> list = new ArrayList<>();
            do {
                Map<String, String> info = new HashMap<>();

                info.put("id", data.getString(0));
                info.put("url", data.getString(1));
                list.add(info);

            } while (data.moveToNext());
            data.moveToLast();
            int lastId = data.getInt(0);
            data.close();

            // 要下载的数量
            if (novelDownloadInfo == null) {
                downloadInfo.setCount(list.size());
                downloadInfo.setLastId(lastId);
                downloadInfo.setId(saveDownload(downloadInfo, SQLiteNovel.DOWNLOAD_PAUSE, true));
                downloadInfo.setFinish(0);
            } else {
                downloadInfo.setId(novelDownloadInfo.getId());
                downloadInfo.setLastId(novelDownloadInfo.getLastId());
                downloadInfo.setCount(novelDownloadInfo.getCount());
                // 出现意外情况时中止下载
                int finish = novelDownloadInfo.getCount() - list.size();
                if (finish < 0) {
                    saveDownload(novelDownloadInfo, SQLiteNovel.DOWNLOAD_CONTINUE, false);
                    Intent intent = new Intent();
                    intent.setAction(NovelService.DOWNLOAD_FAIL);
                    intent.putExtra("id", novelDownloadInfo.getId());
                    sendBroadcast(intent);
                    continueDownload();
                    return;
                }
                downloadInfo.setFinish(finish);
            }

            // 开线程继续小说下载
            Crawler crawler = NovelConfigureManager.getCrawler(novelInfo.getSource());
            downloadPool = Executors.newFixedThreadPool(crawler.getResolve().getThreadCount());
            for (Map<String, String> map : list) {
                crawler.download(map.get("url"), Integer.parseInt(map.get("id")), table, downloadPool, this);
            }
        } else {
            if (novelDownloadInfo == null) {
                downloadInfo.setFinish(0);
                downloadInfo.setCount(0);
            } else {
                downloadInfo.setFinish(novelDownloadInfo.getCount());
                downloadInfo.setCount(novelDownloadInfo.getCount());
                downloadInfo.setId(novelDownloadInfo.getId());
                sendDownloadFinishBroadcast();
            }
            continueDownload();
        }
    }

    public boolean stopDownload(final boolean isContinue, final boolean isSave) {
        return stopDownload(isContinue, isSave, false);
    }

    /**
     * 暂停下载
     *
     * @param isContinue 暂停后是否继续下载下一本
     * @param isSave     是否把当前的状态写入数据库
     * @return 暂停是否成功
     */
    public boolean stopDownload(final boolean isContinue, final boolean isSave, boolean isImmediate) {
        // 如果正在下载才停止下载
        if (!downloadEnable && isStartDownload) {
            if (!isImmediate)
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopDownloadImpl(isSave, isContinue);
                    }
                }, SEPARATOR);
            else stopDownloadImpl(isSave, isContinue);
            return true;
        }
        return false;
    }

    private void stopDownloadImpl(boolean isSave, boolean isContinue) {
        downloadPool.shutdownNow();
        try {
            downloadPool.awaitTermination(10000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (isSave) saveDownload(downloadInfo, SQLiteNovel.DOWNLOAD_CONTINUE, false);
        if (isContinue) continueDownload();
        else {
            downloadEnable = true;
            isStartDownload = false;
        }
    }

    @Override
    public synchronized void onDownloadFinish(NovelText novelText, String table, int id) {
        downloadInfo.setFinish(downloadInfo.getFinish() + 1);
        sendDownloadFinishBroadcast();
        if (downloadInfo.getFinish() == downloadInfo.getCount()) {
            saveDownload(downloadInfo, SQLiteNovel.DOWNLOAD_FINISH, false);
            downloadInfo.setFinish(0);
            continueDownload();
        }
        if (novelText == null || novelText.getText().equals("")) return;
        ContentValues values = new ContentValues();
        values.put("text", novelText.getText().replace("\'", "\""));
        sqLiteNovel.getReadableDatabase().update(table, values, "id = ?", new String[]{String.valueOf(id)});
    }

    /**
     * 保存下载的信息
     *
     * @param downloadInfo 要保存的下载信息
     * @param status       下载的状态
     * @param insert       插入 or 更新
     * @return 插入时返回id
     */
    private long saveDownload(NovelDownloadInfo downloadInfo, int status, boolean insert) {
        ContentValues values = new ContentValues();
        values.put("count", downloadInfo.getCount());
        values.put("finish", downloadInfo.getFinish());
        values.put("time", new Date().getTime());
        values.put("status", status);
        values.put("last_id", downloadInfo.getLastId());
        values.put("novel_id", downloadInfo.getNovelId());
        if (insert)
            return sqLiteNovel.getReadableDatabase().insert("download", null, values);
        sqLiteNovel.getReadableDatabase().update("download", values, "id = ?", new String[]{String.valueOf(downloadInfo.getId())});
        return -1;
    }

    /**
     * 继续下载处于等待状态的小说
     */
    private void continueDownload() {
        List<NovelDownloadInfo> downloadInfos = SQLTools.getDownloadInfo(sqLiteNovel, "status = ?",
                new String[]{String.valueOf(SQLiteNovel.DOWNLOAD_WAIT)}, "time");
        if (downloadInfos.size() > 0) {
            final NovelDownloadInfo downloadInfo = downloadInfos.get(0);
            downloadInfo.setStatus(SQLiteNovel.DOWNLOAD_PAUSE);
            ContentValues values = new ContentValues();
            values.put("time", new Date().getTime());
            values.put("status", downloadInfo.getStatus());
            sqLiteNovel.getReadableDatabase().update("download", values, "id = ?", new String[]{String.valueOf(downloadInfo.getId())});
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startDownload(String.valueOf(downloadInfo.getNovelId()), 0, downloadInfo);
                }
            }, SEPARATOR);
        } else {
            downloadEnable = true;
            isStartDownload = false;
        }
    }

    /**
     * 发送下载广播
     */
    private synchronized void sendDownloadFinishBroadcast() {
        Intent intent = new Intent();
        intent.setAction(NovelService.DOWNLOAD_FINISH);
        intent.putExtra("count", downloadInfo.getCount());
        intent.putExtra("finish", downloadInfo.getFinish());
        intent.putExtra("id", downloadInfo.getId());
        sendBroadcast(intent);
    }

    private void onUpdate(int mode, int... values) {
        for (UpdateNovel updateNovel : updateNovel) {
            switch (mode) {
                case ON_UPDATE_START:
                    updateNovel.onUpdateStart();
                    break;
                case ON_UPDATE_FAIL:
                    updateNovel.onUpdateFail();
                    break;
                case ON_UPDATE_FINISH:
                    updateNovel.onUpdateFinish(values[0], values[1], values[2]);
                    break;
            }
        }
    }

    public boolean isContainId(int id) {
        return containId.contains(id);
    }

    public boolean addUpdateListener(UpdateNovel updateNovel) {
        if (!updateEnable) this.updateNovel.add(updateNovel);
        return !updateEnable;
    }

    public void update(int pos, UpdateNovel updateNovel) {
        if (updateEnable) {
            this.updateNovel.clear();
            containId.clear();
            updateEnable = false;
            if (updateNovel != null) {
                this.updateNovel.add(updateNovel);
            }
            onUpdate(ON_UPDATE_START);
            startUpdate(pos);
        } else {
            onUpdate(ON_UPDATE_FAIL);
        }
    }

    /**
     * 更新小说
     *
     * @param pos 更新的位置, {@link NovelService#UPDATE_ALL} 表示更新全部
     */
    private void startUpdate(int pos) {
        List<Map<String, String>> updateList = new ArrayList<>();
        Cursor cursor;

        if (pos == NovelService.UPDATE_ALL) {
            // 从数据库拿所有的小说
            cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"id", "complete", "source",
                    "newChapter"}, null, null, null, null, "time");
        } else {
            cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"id", "complete", "source",
                    "newChapter"}, "id=?", new String[]{String.valueOf(pos)}, null, null, "time");
        }
        updateCount = 0;
        updateFinish = 0;

        if (!cursor.moveToNext()) {
            cursor.close();
            // 数据库里没有小说, 发出更新完成
            if (updateNovel.size() != 0)
                handler.post(new UpdateRunnable(-1, -1, -1));
            updateEnable = true;
            return;
        }

        do {
            int id = cursor.getInt(0);
            int complete = cursor.getInt(1);
            String source = cursor.getString(2);
            String chapter = cursor.getString(3);

            // 如果小说是本地导入就没必要更新了
            if (source == null) continue;

            Cursor cur = sqLiteNovel.getReadableDatabase().query("nc", new String[]{"name", "md5"}, "novel_id=?",
                    new String[]{String.valueOf(id)}, null, null, null);

            cur.moveToNext();

            String URL = cur.getString(0);
            String table = cur.getString(1);

            Cursor c = sqLiteNovel.getReadableDatabase().query(table, new String[]{"id"}, null,
                    null, null, null, "-id", "1");

            boolean exist = c.moveToNext();
            int newId = 0;
            // 如果是刚收藏设id为1
            if (exist) {
                newId = c.getInt(0);
            }
            c.close();
            cur.close();
            // 如果已经完本了且把章节读入数据库就没必要更新了
            if (exist && complete == 1) {
                continue;
            }

            updateCount++;

            // 拿到相应的数据后,放入键值对
            Map<String, String> map = new HashMap<>();

            map.put("source", source);
            map.put("URL", URL);
            map.put("id", String.valueOf(id));
            map.put("newId", String.valueOf(newId));
            updateList.add(map);
            containId.add(id);
        } while (cursor.moveToNext());

        // 最大开4个线程去更新
        if (updateList.size() != 0) {
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(min(updateList.size(), 4));
            for (Map<String, String> map : updateList) {
                Crawler crawler = NovelConfigureManager.getCrawler(map.get("source"));
                crawler.update(map.get("URL"), Integer.parseInt(map.get("id")), Integer.parseInt(map.get("newId")), fixedThreadPool, this);
            }
        }

        // 如果没有小说需要更新
        if (updateCount == 0) {
            onUpdate(ON_UPDATE_FINISH, -1, -1, -1);
            updateEnable = true;
        }

        cursor.close();
    }

    @Override
    public synchronized void onUpdateFinish(int id, List<NovelTextItem> list) {
        // 没有新小说,或发生错误跳过
        if (list == null || list.size() == 0) {
            updateFinish++;
            if (updateNovel.size() != 0) handler.post(new UpdateRunnable(updateFinish, updateCount, id));
            if (updateFinish == updateCount) {
                updateEnable = true;
            }
            return;
        }
        // 更新数据库
        ContentValues contentValues = new ContentValues();
        contentValues.put("newChapter", list.get(list.size() - 1).getChapter());

        sqLiteNovel.getReadableDatabase().update("novel", contentValues, "id=?", new String[]{String.valueOf(id)});

        Cursor cur = sqLiteNovel.getReadableDatabase().query("nc", new String[]{"md5"}, "novel_id=?",
                new String[]{String.valueOf(id)}, null, null, null);

        cur.moveToNext();

        SQLiteDatabase database = sqLiteNovel.getReadableDatabase();
        database.beginTransaction();
        for (NovelTextItem textItem : list) {
            database.execSQL("insert into " + cur.getString(0) +
                    " (url, chapter) values (?, ?)", new String[]{textItem.getUrl(), textItem.getChapter()});
        }
        database.setTransactionSuccessful();
        database.endTransaction();

        cur.close();

        updateFinish++;
        if (updateNovel.size() != 0) handler.post(new UpdateRunnable(updateFinish, updateCount, id));
        if (updateFinish == updateCount) {
            updateEnable = true;
        }
    }

    class UpdateRunnable implements Runnable {
        private int updateFinish;
        private int updateCount;
        private int id;

        UpdateRunnable(int updateFinish, int updateCount, int id) {
            this.updateFinish = updateFinish;
            this.updateCount = updateCount;
            this.id = id;
        }

        @Override
        public void run() {
            onUpdate(ON_UPDATE_FINISH, updateFinish, updateCount, id);
        }
    }

    public interface UpdateNovel {
        /**
         * 小说开始更新时调用
         */
        public abstract void onUpdateStart();

        /**
         * 当更新不可用时调用
         */
        public abstract void onUpdateFail();

        /**
         * 每更新好一本小说时调用
         *
         * @param updateFinish 已经更新的数量
         * @param updateCount  总共要更新的数量
         */
        public abstract void onUpdateFinish(int updateFinish, int updateCount, int id);
    }
}
