package novel.flandre.cn.net;

import io.reactivex.Observable;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.bean.data.novel.NovelRemind;
import novel.flandre.cn.bean.data.novel.NovelText;
import novel.flandre.cn.bean.data.novel.NovelTextItem;
import okhttp3.ResponseBody;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.min;
import static novel.flandre.cn.net.Crawler.*;

public class Sourcewangshugu extends BaseResolve {
    public static String TAG = "https://www.wangshugu.com/";

    @Override
    public Observable<ResponseBody> getSearchObservable(String s) {
        return crawler.crawlerPOST(getDomain() + "search/", "searchkey=" + setUnicode(s));
    }

    @Override
    public List<String> search(Document document, String s) {
        List<String> list = new ArrayList<>();
        document.select("#content > dd > table > tbody > tr:nth-child(1)").remove();
        Elements elements = document.select("#content > dd > table > tbody > tr");
        elements = orderBy(elements, "td:nth-child(1) > a", s);
        int end = min(elements.size(), SEARCH_COUNT);
        for (int i = 0; i < end; i++) {
            Element element = elements.get(i);
            String url = element.select("td:nth-child(1) > a").attr("href");
            url = url.substring(0, url.length() - 1);
            url = getDomain() + "books/book" + url.substring(url.lastIndexOf("/") + 1) + ".html";
            list.add(url);
        }
        return list;
    }

    @Override
    public void searchData(Document document, NovelInfo novelInfo) {
        novelInfo.setName(document.select("#content > dd:nth-child(2) > h1").get(0).text().split(" ")[0]);
        novelInfo.setAuthor(document.select("#at > tbody > tr:nth-child(1) > td:nth-child(4)").get(0).text());
        novelInfo.setComplete(document.select("#at > tbody > tr:nth-child(1) > td:nth-child(6)").get(0).text().contains("连载") ? 0 : 1);
        novelInfo.setIntroduce(document.select("#content > dd:nth-child(7) > p:nth-child(3)").get(0).text());
        novelInfo.setUrl(document.select("#content > dd:nth-child(3) > div:nth-child(2) > p.btnlinks > a.read").attr("href"));
        novelInfo.setSource(Sourcewangshugu.class.getName());
        novelInfo.setChapter(document.select("#content > dd:nth-child(7) > p:nth-child(7) > a").get(0).text());
        novelInfo.setImagePath(document.select("#content > dd:nth-child(3) > div:nth-child(1) > a > img").attr("src"));
    }

    @Override
    public List<NovelTextItem> list(Document document, String url) {
        List<NovelTextItem> list = new ArrayList<>();
        Elements elements = document.select("#at > tbody > tr");
        for (Element element : elements) {
            Elements items = element.select("td > a");
            for (Element item : items) {
                NovelTextItem textItem = new NovelTextItem();
                textItem.setChapter(item.text());
                textItem.setUrl(url + item.attr("href"));
                list.add(textItem);
            }
        }
        return list;
    }

    @Override
    public NovelText text(Document document) {
        NovelText novelText = new NovelText();
        novelText.setChapter(document.select("#amain > dl > dd:nth-child(2) > h1").get(0).text());
        novelText.setText(withBr(document, "#contents", " ", DOUBLE_LE_RF));
        return novelText;
    }

    @Override
    public String getRankUrl(int type) {
        String url = null;
        switch (type) {
            case DAY_RANK:
                url = getDomain() + "books/toplist/weekvote-1.html";
                break;
            case MONTH_RANK:
                url = getDomain() + "books/toplist/monthvote-1.html";
                break;
            case TOTAL_RANK:
                url = getDomain() + "books/toplist/allvote-1.html";
                break;
        }
        return url;
    }

    @Override
    public List<String> rank(Document document, int type) {
        List<String> list = new ArrayList<>();
        document.select("#content > dd > table > tbody > tr:nth-child(1)").remove();
        Elements elements = document.select("#content > dd > table > tbody > tr");
        int end = Math.min(elements.size(), RANK_COUNT);
        for (int i = 0; i < end; i++) {
            list.add(elements.get(i).select("td:nth-child(1) > a").attr("href"));
        }
        return list;
    }

    @Override
    public String getRemindUrl() {
        return getDomain();
    }

    @Override
    public List<NovelRemind> remind(Document document) {
        List<NovelRemind> list = new ArrayList<>();
        document.select("#centeri > div > div.blockcontent > ul > li.more").remove();
        Elements elements = document.select("#centeri > div > div.blockcontent > ul > li");
        for (Element element : elements) {
            NovelRemind novelRemind = new NovelRemind();
            novelRemind.setName(element.select("p.ul1 > a.poptext").text());
            novelRemind.setChapter(element.select("p.ul2 > a").text());
            list.add(novelRemind);
        }
        return list;
    }

    @Override
    public String getAddress(String addr, NovelInfo novelInfo) {
        addr = addr.substring(0, addr.length() - 1);
        int last = addr.lastIndexOf("/");
        String num = addr.substring(last + 1);
        addr = addr.substring(0, last);
        addr = addr.substring(0, addr.length() - 1);
        addr = addr.substring(0, addr.lastIndexOf("/")) + "/book" + num + ".html";
        return addr;
    }

    @Override
    public boolean isPC() {
        return true;
    }

    @Override
    public String getDomain() {
        return TAG;
    }

    @Override
    public String getCharset() {
        return "UTF8";
    }

    @Override
    public int getThreadCount() {
        return MIN_THREAD_COUNT;
    }
}
