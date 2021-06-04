package flandre.cn.novel.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import flandre.cn.novel.utils.crypt.MD5;
import flandre.cn.novel.ui.activity.IndexActivity;
import flandre.cn.novel.bean.data.novel.NovelDownloadInfo;
import flandre.cn.novel.bean.data.novel.NovelInfo;
import flandre.cn.novel.bean.data.novel.WrapperNovelInfo;
import flandre.cn.novel.service.NovelService;

import java.io.*;
import java.util.*;

public class SQLTools {
    /**
     * 设置最新阅读时间
     *
     * @param id          小说id
     * @param sqLiteNovel 数据库文件
     */
    public static void setTime(int id, SQLiteNovel sqLiteNovel) {
        SQLiteDatabase readableDatabase = sqLiteNovel.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("time", new Date().getTime());
        readableDatabase.update("novel", values, "id=?", new String[]{String.valueOf(id)});
    }

    /**
     * 设置小说开始阅读的时间
     *
     * @param id novel的id
     */
    public static void setStartTime(String id, SQLiteNovel sqLiteNovel) {
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"start"}, "id=? and start != 0",
                new String[]{String.valueOf(id)}, null, null, null);
        if (!cursor.moveToNext()) {
            SharedTools.getSharedTools().increaseStart();
            ContentValues values = new ContentValues();
            values.put("start", new Date().getTime());
            sqLiteNovel.getReadableDatabase().update("novel", values, "id=?", new String[]{id});
        }
        cursor.close();
    }

    /**
     * 设置小说阅读完成的时间
     *
     * @param id novel的id
     */
    public static void setFinishTime(String id, SQLiteNovel sqLiteNovel) {
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"finish", "complete"}, "id=? and finish = 0",
                new String[]{id}, null, null, null);
        // 当小说被收藏且已经完本时才设定看完时间
        if (cursor.moveToNext() && cursor.getInt(1) == 1) {
            SharedTools.getSharedTools().increaseFinish();
            ContentValues values = new ContentValues();
            values.put("finish", new Date().getTime());
            sqLiteNovel.getReadableDatabase().update("novel", values, "id=?", new String[]{id});
        }
        cursor.close();
    }

    /**
     * 设置本小说的阅读时间以及阅读的位置
     *
     * @param id      novel的id
     * @param addTime 阅读时间的增量
     * @param chapter 当前观看的章节
     * @param page    当前观看的页
     */
    public static void setRead(String id, SQLiteNovel sqLiteNovel, long addTime, int chapter, int page) {
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"read"}, "id=?",
                new String[]{id}, null, null, null);
        if (cursor.moveToNext()) {
            long time = cursor.getLong(0);
            ContentValues values = new ContentValues();
            values.put("read", time + addTime);
            values.put("watch", chapter + ":" + page);
            sqLiteNovel.getReadableDatabase().update("novel", values, "id=?", new String[]{id});
        }
        cursor.close();
    }

    /**
     * 拿到read time的小说信息
     */
    public static List<WrapperNovelInfo> getWrapperNovelInfo(SQLiteNovel sqLiteNovel) {
        List<WrapperNovelInfo> list = new ArrayList<>();
        List<NovelInfo> infos = SQLTools.getNovelData(sqLiteNovel);
        for (NovelInfo novelInfo : infos) {
            if (novelInfo.getStart() == 0) continue;
            WrapperNovelInfo wrapperNovelInfo = new WrapperNovelInfo();
            wrapperNovelInfo.setInfo(novelInfo);
            String chapter = novelInfo.getWatch().split(":")[0];
            wrapperNovelInfo.setChapter(Integer.parseInt(chapter));
            // 拿到总章节数
            Cursor cursor = sqLiteNovel.getReadableDatabase().query(novelInfo.getTable(), new String[]{"id"}, null,
                    null, null, null, "-id", "1");
            if (!cursor.moveToNext()) {
                cursor.close();
                continue;
            }
            String newId = cursor.getString(0);
            wrapperNovelInfo.setCount(Integer.valueOf(newId));
            cursor.close();
            // 拿当前章节的章节名
            cursor = sqLiteNovel.getReadableDatabase().query(novelInfo.getTable(), new String[]{"chapter"},
                    "id = ?", new String[]{chapter}, null, null, null);
            cursor.moveToNext();
            wrapperNovelInfo.setNowChapter(cursor.getString(0));
            cursor.close();
            list.add(wrapperNovelInfo);
        }
        return list;
    }

    /**
     * @param novel_id novel的id
     * @return 小说text表的表名
     */
    public static String getTableName(SQLiteNovel sqLiteNovel, String novel_id) {
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("nc", new String[]{"md5"}, "novel_id=?",
                new String[]{novel_id}, null, null, null);
        cursor.moveToNext();
        String table = cursor.getString(0);
        cursor.close();
        return table;
    }

    public static void setData(SQLiteNovel sqLiteNovel, String novel_id, NovelInfo novelInfo) {
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("nc", new String[]{"md5", "name"}, "novel_id=?",
                new String[]{novel_id}, null, null, null);
        cursor.moveToNext();
        novelInfo.setTable(cursor.getString(0));
        novelInfo.setUrl(cursor.getString(1));
        cursor.close();
    }

    public static int getNovelId(SQLiteNovel sqLiteNovel, String name, String author) {
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"id"}, "name = ? and author = ?",
                new String[]{name, author}, null, null, null);
        int id;
        if (cursor.moveToNext())
            id = cursor.getInt(0);
        else id = -1;
        cursor.close();
        return id;
    }

    /**
     * 删除小说关联的所有记录
     *
     * @param novel_id novel的id
     */
    public static void delete(SQLiteNovel sqLiteNovel, String novel_id, NovelService service, Context context) {
        // 检查该小说是否在被下载, 有的话先停止下载
        List<NovelDownloadInfo> downloadInfos = getDownloadInfo(sqLiteNovel, "novel_id = ? and status = ?", new String[]{novel_id,
                String.valueOf(SQLiteNovel.DOWNLOAD_PAUSE)}, null);
        if (downloadInfos.size() > 0) {
            service.stopDownload(false, false, true);
        }

        Cursor cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"name", "author", "image",
                "start", "finish", "read"}, "id=?", new String[]{novel_id}, null, null, null);

        cursor.moveToNext();
        String name = cursor.getString(0);
        String author = cursor.getString(1);
        String image = cursor.getString(2);
        // 当观看时长小于半小时时, 会减少观看本数, 已经以及可能减少看完本数
        SharedTools sharedTools = SharedTools.getSharedTools();
        if (cursor.getLong(3) > 0 && cursor.getLong(5) < 1000 * 60 * 30) {
            sharedTools.decreaseStart();
            if (cursor.getLong(4) != 0) {
                sharedTools.decreaseFinish();
            }
        }

        cursor.close();

        File imagePath = new File(image);
        imagePath.delete();

        String table = "FL" + MD5.md5(name + author);
        sqLiteNovel.getReadableDatabase().execSQL("drop table " + table);
        sqLiteNovel.getReadableDatabase().delete("nc", "md5=?", new String[]{table});
        sqLiteNovel.getReadableDatabase().delete("novel", "name=? and author=?", new String[]{name, author});
        sqLiteNovel.getReadableDatabase().delete("download", "novel_id = ?", new String[]{novel_id});
        context.sendBroadcast(new Intent(IndexActivity.LOAD_DATA));
    }

    /**
     * 修改来源
     *
     * @param novelInfo 里面需要有name和author
     * @param novel_id 小说的id
     */
    public static void changeSource(SQLiteNovel sqLiteNovel, NovelInfo novelInfo, String novel_id, NovelService service){
        // 检查该小说是否在被下载, 有的话先停止下载
        List<NovelDownloadInfo> downloadInfos = getDownloadInfo(sqLiteNovel, "novel_id = ? and status = ?", new String[]{novel_id,
                String.valueOf(SQLiteNovel.DOWNLOAD_PAUSE)}, null);
        if (downloadInfos.size() > 0) {
            service.stopDownload(false, false, true);
        }

        String table = "FL" + MD5.md5(novelInfo.getName() + novelInfo.getAuthor());
        sqLiteNovel.getReadableDatabase().execSQL("drop table " + table);
        sqLiteNovel.getReadableDatabase().delete("download", "novel_id = ?", new String[]{novel_id});
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", novelInfo.getUrl());
        sqLiteNovel.getReadableDatabase().update("nc", contentValues, "novel_id = ?", new String[]{novel_id});
        contentValues = new ContentValues();
        contentValues.put("source", novelInfo.getSource());
        sqLiteNovel.getReadableDatabase().update("novel", contentValues, "id = ?", new String[]{novel_id});
        // 小说内容表
        sqLiteNovel.getReadableDatabase().execSQL(
                "create table " + table + "(" +
                        "id INTEGER primary key AUTOINCREMENT," +
                        "chapter varchar(255)," +  // 章节名
                        "url varcahr(255)," +  // 该章文本的URL
                        "text text)"  // 文本
        );
    }

    public static NovelInfo getNovelOneData(SQLiteNovel sqLiteNovel, String name, String author) {
        return getNovelOneData(sqLiteNovel, name, author, null);
    }

    public static NovelInfo getNovelOneData(SQLiteNovel sqLiteNovel, String novelId) {
        return getNovelOneData(sqLiteNovel, null, null, novelId);
    }

    /**
     * 拿一个novel的数据
     *
     * @param name    小说名
     * @param author  小说作者
     * @param novelId 小说id, 如果这个不是null, 那么另外两个无用
     */
    private static NovelInfo getNovelOneData(SQLiteNovel sqLiteNovel, String name, String author, String novelId) {
        NovelInfo novelInfo = new NovelInfo();
        Cursor cursor;
        if (novelId == null)
            cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"name", "image",
                            "newChapter", "author", "watch", "id", "introduce", "source", "complete"},
                    "name = ? and author = ?", new String[]{name, author}, null, null, null);
        else
            cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"name", "image",
                            "newChapter", "author", "watch", "id", "introduce", "source", "complete"},
                    "id = ?", new String[]{novelId}, null, null, null);
        cursor.moveToNext();
        novelInfo.setName(cursor.getString(0));
        novelInfo.setImagePath(cursor.getString(1));
        novelInfo.setAuthor(cursor.getString(3));
        novelInfo.setChapter(cursor.getString(2));
        novelInfo.setWatch(cursor.getString(4));
        novelInfo.setId(cursor.getInt(5));
        setData(sqLiteNovel, String.valueOf(novelInfo.getId()), novelInfo);
        novelInfo.setSource(cursor.getString(7));
        novelInfo.setIntroduce(cursor.getString(6));
        novelInfo.setComplete(cursor.getInt(8));
        cursor.close();
        return novelInfo;
    }

    /**
     * 拿小说信息
     */
    public static List<NovelInfo> getNovelData(SQLiteNovel sqLiteNovel) {

        // 拿到收藏的小说, 填充主界面, 若没有小说, 显示空空如也
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"name", "image",
                        "newChapter", "author", "watch", "id", "introduce", "source", "complete", "start", "finish", "time", "read"},
                null, null, null, null, "-time");
        List<NovelInfo> list = new ArrayList<>();
        if (cursor.moveToNext()) {
            do {
                NovelInfo novelInfo = new NovelInfo();
                novelInfo.setName(cursor.getString(0));
                novelInfo.setImagePath(cursor.getString(1));
                novelInfo.setChapter(cursor.getString(2));
                novelInfo.setAuthor(cursor.getString(3));
                novelInfo.setWatch(cursor.getString(4));
                novelInfo.setId(cursor.getInt(5));
                setData(sqLiteNovel, String.valueOf(novelInfo.getId()), novelInfo);
                novelInfo.setIntroduce(cursor.getString(6));
                novelInfo.setSource(cursor.getString(7));
                novelInfo.setComplete(cursor.getInt(8));
                novelInfo.setStart(cursor.getLong(9));
                novelInfo.setFinish(cursor.getLong(10));
                novelInfo.setTime(cursor.getLong(11));
                novelInfo.setRead(cursor.getLong(12));
                list.add(novelInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static List<NovelDownloadInfo> getDownloadInfo(SQLiteNovel sqLiteNovel) {
        return getDownloadInfo(sqLiteNovel, null, null, "status, -id");
    }

    /**
     * 拿一个download的数据
     *
     * @param selection     查找条件
     * @param selectionArgs 查找条件的变量
     * @param orderBy       排序方式
     */
    public static List<NovelDownloadInfo> getDownloadInfo(SQLiteNovel sqLiteNovel, String selection, String[] selectionArgs, String orderBy) {
        List<NovelDownloadInfo> downloadInfos = new ArrayList<>();
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("download", new String[]{"novel_id", "last_id",
                        "finish", "count", "status", "time", "id"}, selection, selectionArgs, null,
                null, orderBy, "20");
        while (cursor.moveToNext()) {
            NovelDownloadInfo downloadInfo = new NovelDownloadInfo();
            downloadInfo.setNovelId(cursor.getInt(0));
            downloadInfo.setLastId(cursor.getInt(1));
            downloadInfo.setFinish(cursor.getInt(2));
            downloadInfo.setCount(cursor.getInt(3));
            downloadInfo.setStatus(cursor.getInt(4));
            downloadInfo.setTime(cursor.getLong(5));
            downloadInfo.setId(cursor.getInt(6));
            Cursor cur = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"name"}, "id = ?",
                    new String[]{String.valueOf(downloadInfo.getNovelId())}, null, null, null);
            cur.moveToNext();
            downloadInfo.setTable(cur.getString(0));
            cur.close();
            downloadInfos.add(downloadInfo);
        }
        cursor.close();
        return downloadInfos;
    }

    /**
     * 插入一本小说
     *
     * @param novelInfo 小说的数据
     * @return 小说的id
     */
    public static long insertNovel(SQLiteNovel sqLiteNovel, NovelInfo novelInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", novelInfo.getName());
        contentValues.put("author", novelInfo.getAuthor());
        contentValues.put("watch", novelInfo.getWatch());
        contentValues.put("complete", novelInfo.getComplete());
        contentValues.put("source", novelInfo.getSource());
        contentValues.put("image", novelInfo.getImagePath());
        contentValues.put("newChapter", novelInfo.getChapter());
        contentValues.put("introduce", novelInfo.getIntroduce());
        contentValues.put("time", novelInfo.getTime());
        return sqLiteNovel.getReadableDatabase().insert("novel", null, contentValues);
    }

    /**
     * 把一个小说的信息加载到数据库
     * @param novelInfo 小说的信息
     * @param sqLiteNovel 数据库对象
     * @param context 上下文
     */
    public static void saveInSQLite(NovelInfo novelInfo, SQLiteNovel sqLiteNovel, Context context){
        // 把数据保存起来
        Bitmap bitmap = novelInfo.getBitmap();
        File imagePath = getImagePath(context, novelInfo);
        // 保存图片到本地
        try {
            OutputStream stream = new FileOutputStream(imagePath);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] data = outputStream.toByteArray();
            stream.write(data);
            stream.flush();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 在novel里面添加收藏的记录
        novelInfo.setTime(new Date().getTime());
        novelInfo.setWatch("1:1");
        novelInfo.setImagePath(imagePath.getAbsolutePath());
        long novel_id = SQLTools.insertNovel(sqLiteNovel, novelInfo);
        novelInfo.setId((int) novel_id);
        // 在nc里面记录表名
        ContentValues values = new ContentValues();
        String table = "FL" + MD5.md5(novelInfo.getName() + novelInfo.getAuthor());
        values.put("novel_id", novel_id);
        values.put("name", novelInfo.getUrl());
        values.put("md5", table);
        novelInfo.setTable(table);
        long NC_id = sqLiteNovel.getReadableDatabase().insert("nc", null, values);
        // 创建存文本的表
        sqLiteNovel.getReadableDatabase().execSQL(
                "create table " + table + "(" +
                        "id INTEGER primary key AUTOINCREMENT," +
                        "chapter varchar(255)," +
                        "url varcahr(255)," +
                        "text text)"
        );
        context.sendBroadcast(new Intent(IndexActivity.LOAD_DATA));
    }

    public static File getImagePath(Context context, NovelInfo novelInfo) {
        String image = new File(novelInfo.getImagePath()).getName();

        File file = new File(context.getExternalFilesDir(null), "img");
        if (!file.exists()) file.mkdir();
        return new File(file, image);
    }
}
