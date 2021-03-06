package novel.flandre.cn.net;

import io.reactivex.Observable;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.bean.data.novel.NovelRemind;
import novel.flandre.cn.bean.data.novel.NovelText;
import novel.flandre.cn.bean.data.novel.NovelTextItem;
import okhttp3.ResponseBody;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface Resolve {
    public boolean isPC();

    public String getDomain();

    public String getCharset();

    public int getThreadCount();

    public void attachCrawler(Crawler crawler);

    public Observable<ResponseBody> getSearchObservable(String s);

    public List<String> search(Document document, String s);

    public void searchData(Document document, NovelInfo info) throws ParseException;

    public List<NovelTextItem> list(Document document, String url);

    public NovelText text(Document document) throws IOException;

    public String getRankUrl(int type);

    public List<String> rank(Document document, int type);

    public String getRemindUrl();

    public List<NovelRemind> remind(Document document);

    public String getAddress(String addr, NovelInfo novelInfo);
}
