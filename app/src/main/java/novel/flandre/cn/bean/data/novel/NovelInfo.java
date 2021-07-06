package novel.flandre.cn.bean.data.novel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import novel.flandre.cn.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;

/**
 * 小说的信息
 */
public class NovelInfo implements Serializable, Cloneable {
    private int id = 0;  // novel的id
    private String name = null;  // 小说书名
    private String author = null;  // 小说作者
    private String watch = null;  // 观看的进度 章节:页数
    private String table = null;  // 文本表的表名
    private String imagePath = null;  // 图片文件路径
    private byte[] image = null;  // 图片
    private String source = null;  // 使用源
    private String introduce = null;  // 介绍
    private long time = 0;  // 最新阅读时间
    private long start = 0;  // 开始时间
    private long finish = 0;  // 完成时间
    private long read = 0;  // 观看时长
    private String chapter = null;  // 最新章节
    private String url = null;  // 章节的url
    private int complete = 0;  // 是否完本

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getWatch() {
        return watch;
    }

    public void setWatch(String watch) {
        this.watch = watch;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getFinish() {
        return finish;
    }

    public void setFinish(long finish) {
        this.finish = finish;
    }

    public long getRead() {
        return read;
    }

    public void setRead(long read) {
        this.read = read;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public NovelInfo copy() {
        NovelInfo novelInfo = new NovelInfo();
        novelInfo.id = id;
        novelInfo.name = name;
        novelInfo.author = author;
        novelInfo.watch = watch;
        novelInfo.table = table;
        novelInfo.imagePath = imagePath;
        novelInfo.image = image;
        novelInfo.source = source;
        novelInfo.introduce = introduce;
        novelInfo.time = time;
        novelInfo.start = start;
        novelInfo.finish = finish;
        novelInfo.read = read;
        novelInfo.chapter = chapter;
        novelInfo.complete = complete;
        novelInfo.url = url;
        return novelInfo;
    }

    public void setBitmap(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        image = baos.toByteArray();
    }

    public void setBitmap(String bitmapPath, Context context){
        setBitmap(getBitmap(bitmapPath, context));
    }

    public static Bitmap getBitmap(String bitmapPath, Context context){
        File file = new File(bitmapPath);
        Bitmap bitmap;
        if (file.exists())
            bitmap = BitmapFactory.decodeFile(bitmapPath);
        else bitmap= BitmapFactory.decodeResource(context.getResources(), R.drawable.not_found);
        return bitmap;
    }

    public Bitmap getBitmap() {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
