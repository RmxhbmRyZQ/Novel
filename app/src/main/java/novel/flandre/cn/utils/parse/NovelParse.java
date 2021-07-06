package novel.flandre.cn.utils.parse;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import novel.flandre.cn.utils.crypt.AES;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.tools.ByteBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static novel.flandre.cn.utils.parse.ShareFile.SHARE_FILE;

public class NovelParse extends FileParse {
    private String source;

    public NovelParse(String path, SQLiteNovel sqLiteNovel, Context context) {
        super(path, sqLiteNovel, context);
    }

    @Override
    protected void prepare(InputStream inputStream) throws Exception {
        byte[] bytes = new byte[1024];
        inputStream.read(bytes, 0, SHARE_FILE.length() + 4);
        int len = new ByteBuilder(bytes).setSeek(SHARE_FILE.length()).readInt();
        inputStream.read(bytes, 0, len + 2);
        ByteBuilder byteBuilder = new ByteBuilder(AES.decrypt(Base64.decode(bytes, 0, len, Base64.DEFAULT)));
        source = byteBuilder.readString(byteBuilder.readInt());
    }

    @Override
    protected void getCode(File file) throws IOException {
        code = Charset.forName("UTF8");
    }

    @Override
    protected void extraNovel(ContentValues values) {
        values.put("source", source);
    }

    @Override
    protected void insertText(String chapter, String text, SQLiteDatabase database) {
        if (text.startsWith("http"))
            database.execSQL("insert into " + table + " (chapter, url) values (?, ?)", new String[]{chapter, text});
        else
            database.execSQL("insert into " + table + " (chapter, text) values (?, ?)", new String[]{chapter, text});
    }
}
