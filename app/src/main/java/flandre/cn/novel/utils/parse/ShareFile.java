package flandre.cn.novel.utils.parse;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.widget.Toast;
import flandre.cn.novel.BuildConfig;
import flandre.cn.novel.utils.crypt.AES;
import flandre.cn.novel.utils.tools.ByteBuilder;
import flandre.cn.novel.utils.database.SQLiteNovel;
import flandre.cn.novel.bean.data.novel.NovelInfo;

import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static flandre.cn.novel.utils.parse.FileParse.compile;

public class ShareFile {
    public static final String FL_FILE = "FLANDRE";
    public static final String SHARE_FILE = "HHSWIFE";

    private Context mContext;
    private NovelInfo mNovelInfo;

    public ShareFile(NovelInfo novelInfo, Context context) {
        mNovelInfo = novelInfo;
        mContext = context;
    }

    public ShareFile(Context context) {
        mContext = context;
    }

    private File createFile(int mode) throws IOException {
        try {
            String name = mNovelInfo.getName();
            File dir = new File(mContext.getExternalFilesDir(null), "tmp");
            if (!dir.exists()) dir.mkdir();
            File file = new File(dir, name + ".txt");
            FileOutputStream outputStream = new FileOutputStream(file);
            if (mode == 0) {
                // 如果是网上小说, 生成一个fh文件分享过去
                ByteBuilder byteBuilder = new ByteBuilder(1024);
                byteBuilder.writeString(FL_FILE)
                        .writeInt(mNovelInfo.getSource().getBytes().length)
                        .writeString(mNovelInfo.getSource())
                        .writeInt(name.getBytes().length)
                        .writeString(name)
                        .writeInt(mNovelInfo.getAuthor().getBytes().length)
                        .writeString(mNovelInfo.getAuthor())
                        .writeInt(mNovelInfo.getUrl().getBytes().length)
                        .writeString(mNovelInfo.getUrl());
                outputStream.write(Base64.encode(AES.encrypt(byteBuilder.getBytes()), Base64.DEFAULT));
            } else {
                // 从本地数据库生成一个文本文件发过去
                Toast.makeText(mContext, "生成文件中", Toast.LENGTH_SHORT).show();
                ByteBuilder byteBuilder = new ByteBuilder(1024);
                byteBuilder.writeInt(mNovelInfo.getSource().getBytes().length)
                        .writeString(mNovelInfo.getSource());
                byte[] bytes = Base64.encode(AES.encrypt(byteBuilder.getBytes()), Base64.DEFAULT);
                ByteBuilder builder = new ByteBuilder(1024 + mNovelInfo.getIntroduce().length() * 3);
                builder.writeString(SHARE_FILE)
                        .writeInt(bytes.length)
                        .writeBytes(bytes)
                        .writeString("\r\n")
                        .writeString("《" + mNovelInfo.getName() + "》\r\n")
                        .writeString("作者：" + mNovelInfo.getAuthor() + "\r\n")
                        .writeString(mNovelInfo.getIntroduce() + "\r\n");
                outputStream.write(builder.getBytes());
                String table = mNovelInfo.getTable();
                SQLiteNovel sqLiteNovel = SQLiteNovel.getSqLiteNovel(mContext.getApplicationContext());
                Cursor cursor = sqLiteNovel.getReadableDatabase().query(table, new String[]{"chapter", "url", "text", "id"}, null,
                        null, null, null, null);

                if (cursor.moveToNext()) {
                    String text, chapter;
                    do {
                        text = cursor.getString(2);
                        chapter = cursor.getString(0);
                        Pattern pattern = Pattern.compile(compile, Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(chapter);
                        if (!chapter.startsWith("第") || matcher.find(0) && Objects.equals(matcher.group(0), "")) {
                            chapter = "第" + cursor.getInt(3) + "章 " + chapter;
                        }
                        outputStream.write((chapter + "\r\n").getBytes());
                        if (text != null){
                            outputStream.write((FileParse.strip(text, "\r\n", "\r\n") + "\r\n").getBytes());
                        }else {
                            outputStream.write((cursor.getString(1) + "\r\n").getBytes());
                        }
                    } while (cursor.moveToNext());
                } else {
                    return null;
                }

                cursor.close();
            }
            outputStream.flush();
            outputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void shareFile() {
        if (mNovelInfo.getSource() != null) {
            final String[] items = {"分享摘要，仅本软件可用", "分享文本，需要先下载小说"};
            AlertDialog.Builder listDialog =
                    new AlertDialog.Builder(mContext);
            listDialog.setTitle("分享方式");
            listDialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        File file = createFile(which);
                        if (file == null) Toast.makeText(mContext, "分享失败", Toast.LENGTH_SHORT).show();
                        share(file, getMimeType(file.getAbsolutePath()), "分享小说");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).show();
        } else if (mNovelInfo.getUrl() != null) {
            // 如果是本地小说, 把本地小说分享过去
            File file = new File(mNovelInfo.getUrl());
            if (!file.exists()) {
                Toast.makeText(mContext, "要分享的小说的本地文件已被删除！", Toast.LENGTH_SHORT).show();
                return;
            }
            share(file, getMimeType(file.getAbsolutePath()), "分享小说");
        } else {
            Toast.makeText(mContext, "分享不了该小说！", Toast.LENGTH_SHORT).show();
        }
    }

    public void share(File file, String mineType, String title) {
        Intent share = new Intent(Intent.ACTION_SEND);
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentUri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            contentUri = Uri.fromFile(file);
        }
        share.putExtra(Intent.EXTRA_STREAM, contentUri);
        share.setType(mineType);//此处可发送多种文件
        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mContext.startActivity(Intent.createChooser(share, title));
    }

    public String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }
}
