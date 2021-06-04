package flandre.cn.novel.net;

import flandre.cn.novel.bean.data.novel.*;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface Resolve {
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
