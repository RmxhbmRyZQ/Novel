package flandre.cn.novel.utils.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteNovel extends SQLiteOpenHelper {
    public static final int DOWNLOAD_PAUSE = 0;  // 正在下载
    public static final int DOWNLOAD_WAIT = 1;  // 等待中
    public static final int DOWNLOAD_CONTINUE = 2;  // 继续下载
    public static final int DOWNLOAD_FINISH = 3;  // 下载完成

    private static SQLiteNovel sqLiteNovel = null;
    public boolean freeStatus = true;  // 数据库是否空闲状态

    private SQLiteNovel(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
    }

    public static SQLiteNovel getSqLiteNovel(Context context) {
        if (sqLiteNovel == null) {
            sqLiteNovel = newInstance(context.getApplicationContext(), "novel.db", 2);
        }
        return sqLiteNovel;
    }

    public static SQLiteNovel getSqLiteNovel(){
        return sqLiteNovel;
    }

    public static SQLiteNovel newInstance(Context context, String name, int version) {
        if (sqLiteNovel == null) {
            sqLiteNovel = new SQLiteNovel(context, name, null, version);
        }
        return sqLiteNovel;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 小说表
        sqLiteDatabase.execSQL(
                "create table if not exists novel  (" +
                        "id INTEGER primary key AUTOINCREMENT," +
                        "name varchar(20)," +  // 小说名
                        "author varchar(20)," +  // 作者
                        "complete tinyint(1)," +  // 是否完本
                        "newChapter varchar(127)," +  // 最新章节
                        "watch varchar(20)," +  // 观看位置
                        "image varchar(255)," +  // 图片路径
                        "source varchar(255)," +  // 来源
                        "introduce varchar(1023)," +  // 介绍
                        "time bigint default 0," +  // 最新阅读时间
                        "start bigint default 0," +  // 开始阅读时间
                        "finish bigint default 0," +  // 完成阅读时间
                        "read bigint default 0)"  // 阅读时长
        );
        // 小说和章节的关系表
        sqLiteDatabase.execSQL(
                "create table if not exists nc (" +
                        "id INTEGER primary key AUTOINCREMENT," +
                        "novel_id INTEGER references novel," +  // novel表的id
                        "name varchar(64)," +  // 章节目录的url(本地导入时, 本地小说路径)
                        "md5 varchar(64))"  // 章节表的表名 FL + md5
        );
        // 搜索提示表
        sqLiteDatabase.execSQL("create table if not exists search(" +
                "id INTEGER primary key AUTOINCREMENT," +
                "name varchar(20)," +  // 历史搜索
                "time int)"  // 搜索时间
        );
        // 下载记录表
        sqLiteDatabase.execSQL("create table if not exists download(" +
                "id INTEGER primary key AUTOINCREMENT, " +
                "novel_id int, " +  // novel的id
                "last_id int, " +  // 下载时最后一章的id
                "count int, " +  // 下载总数
                "finish int, " +  // 下载完成数
                "status int, " +  // 当前状态 0.停止 1.等待 2.完成
                "time bigint)");  // 下载完成时间
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // 下载记录表
        sqLiteDatabase.execSQL("create table if not exists download(" +
                "id INTEGER primary key AUTOINCREMENT, " +
                "novel_id int, " +  // novel的id
                "last_id int, " +  // 下载时最后一章的id
                "count int, " +  // 下载总数
                "finish int, " +  // 下载完成数
                "status int, " +  // 当前状态 0.停止 1.等待 2.完成
                "time bigint)");  // 下载完成时间
    }
}
