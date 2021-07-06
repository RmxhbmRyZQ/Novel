package novel.flandre.cn.utils.parse;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.net.Crawler;
import novel.flandre.cn.ui.activity.IndexActivity;
import novel.flandre.cn.utils.crypt.AES;
import novel.flandre.cn.utils.database.SQLTools;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.tools.ByteBuilder;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.io.FileInputStream;
import java.lang.ref.WeakReference;

import static novel.flandre.cn.utils.parse.ShareFile.FL_FILE;

public class GetNovelInfo {
    private WeakReference<Context> mContext;
    private OnFinishParse onFinishParse;

    public GetNovelInfo(Context context) {
        mContext = new WeakReference<>(context);
    }

    public GetNovelInfo setOnFinishParse(OnFinishParse onFinishParse) {
        this.onFinishParse = onFinishParse;
        return this;
    }

    public void execute(String path){

        NovelInfo novelInfo = new NovelInfo();
        try {
            byte[] data = new byte[1024];
            FileInputStream inputStream = new FileInputStream(path);
            int read = inputStream.read(data);
            ByteBuilder byteBuilder = new ByteBuilder(AES.decrypt(Base64.decode(data, FL_FILE.length(), read, Base64.DEFAULT)));
            String source = byteBuilder.readString(byteBuilder.readInt());
            String name = byteBuilder.readString(byteBuilder.readInt());
            String author = byteBuilder.readString(byteBuilder.readInt());
            String address = byteBuilder.readString(byteBuilder.readInt());

            if (SQLTools.getNovelId(SQLiteNovel.getSqLiteNovel(), name, author) == -1) {
                Crawler crawler = NovelConfigureManager.getCrawler(source);
                crawler.getNovelInfo(address, novelInfo, new Crawler.OnRequestComplete<NovelInfo>() {
                    @Override
                    public void onSuccess(NovelInfo data) {
                        SQLTools.saveInSQLite(data, SQLiteNovel.getSqLiteNovel(), mContext.get());
                        onExecute(OnFinishParse.OK);
                    }

                    @Override
                    public void onFail(Throwable e) {
                        onExecute(OnFinishParse.ERROR);
                        e.printStackTrace();
                    }
                });
            } else {
                onExecute(OnFinishParse.ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            onExecute(OnFinishParse.ERROR);
        }
    }

    private void onExecute(Integer integer) {
        switch (integer) {
            case OnFinishParse.ALWAYS:
                Toast.makeText(mContext.get(), "该小说已经存在了！", Toast.LENGTH_SHORT).show();
                break;
            case OnFinishParse.ERROR:
                Toast.makeText(mContext.get(), "加载失败！", Toast.LENGTH_SHORT).show();
                break;
            case OnFinishParse.OK:
                Toast.makeText(mContext.get(), "加载成功！", Toast.LENGTH_SHORT).show();
                ((IndexActivity) mContext.get()).getBookFragment().loadData();
                break;
        }
        if (onFinishParse != null) onFinishParse.onFinishParse(integer);
    }
}
