package novel.flandre.cn.utils.parse;

import android.content.Context;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import novel.flandre.cn.utils.database.SQLiteNovel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static novel.flandre.cn.utils.parse.ShareFile.FL_FILE;
import static novel.flandre.cn.utils.parse.ShareFile.SHARE_FILE;

public class ShareParse {
    private String mPath;
    private OnFinishParse mOnfinishParse;
    private Context mContext;

    public ShareParse(String path, Context context) {
        mPath = path;
        mContext = context;
    }

    public ShareParse setOnfinishParse(OnFinishParse onfinishParse) {
        this.mOnfinishParse = onfinishParse;
        return this;
    }

    public void parseFile(SwipeRefreshLayout refresh) {
        if (refresh != null) refresh.setRefreshing(true);
        Toast.makeText(mContext, "加载小说中...", Toast.LENGTH_SHORT).show();
        File file = new File(mPath);
        if (!file.exists()) {
            Toast.makeText(mContext, "文件不存在！", Toast.LENGTH_SHORT).show();
            if (refresh != null) refresh.setRefreshing(false);
            return;
        }
        try {
            byte[] bytes = new byte[FL_FILE.length()];
            FileInputStream inputStream = new FileInputStream(mPath);
            inputStream.read(bytes);
            inputStream.close();
            String type = new String(bytes);
            if (type.equals(FL_FILE)) {
                new GetNovelInfo(mContext).setOnFinishParse(mOnfinishParse).execute(mPath);
            } else if (type.equals(SHARE_FILE)) {
                new NovelParse(mPath, SQLiteNovel.getSqLiteNovel(), mContext).setOnfinishParse(mOnfinishParse).parseFile();
            } else {
                new FileParse(mPath, SQLiteNovel.getSqLiteNovel(), mContext).setOnfinishParse(mOnfinishParse).parseFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (refresh != null) refresh.setRefreshing(false);
        }
    }
}
