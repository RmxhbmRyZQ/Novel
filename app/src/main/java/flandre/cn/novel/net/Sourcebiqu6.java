package flandre.cn.novel.net;

import flandre.cn.novel.bean.data.novel.NovelInfo;
import flandre.cn.novel.bean.data.novel.NovelRemind;
import flandre.cn.novel.bean.data.novel.NovelText;
import flandre.cn.novel.bean.data.novel.NovelTextItem;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static flandre.cn.novel.net.Crawler.*;
import static java.lang.Math.min;

public class Sourcebiqu6 extends BaseResolve {
    public static String TAG = "http://www.biqu6.com/";

    @Override
    public Observable<ResponseBody> getSearchObservable(String s) {
        return crawler.crawlerPOST(getDomain() + "search", "searchkey=" + setUnicode(s) + "&searchtype=articlename");
    }

    @Override
    public List<String> search(Document document, String s) {
        List<String> list = new ArrayList<>();
        Elements elements = document.select("#content > table > tbody > tr");
        elements.remove(0);
        elements = orderBy(elements, "td.even > a", s);
        for (int i = 0; i < min(elements.size(), SEARCH_COUNT); i++) {
            String detailUrl = getDomain() + elements.get(i).select("td.even > a").get(0).attr("href").substring(1);
            list.add(detailUrl);
        }
        return list;
    }

    @Override
    public void searchData(Document doc, NovelInfo novelInfo) throws ParseException {
        String imgUrl = doc.select("#fmimg > img").attr("src");
        novelInfo.setName(doc.select("#info > h1").get(0).text());
        novelInfo.setAuthor(doc.select("#info > p").get(0).text().substring(4));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = doc.select("#info > p").get(2).text().substring(5);
        long ts = simpleDateFormat.parse(time).getTime();
        // 14 天没更新视为完结
        novelInfo.setComplete(new Date().getTime() - ts > 14 * 60 * 60 * 1000 * 24 ? 1 : 0);
        novelInfo.setChapter(doc.select("#list > dl > dd").get(0).select("a").text());
        novelInfo.setIntroduce(doc.select("#intro > p").text());
        novelInfo.setSource(Sourcebiqu6.class.getName());
        novelInfo.setUrl(doc.baseUri());
        novelInfo.setImagePath(imgUrl);
    }

    @Override
    public List<NovelTextItem> list(Document document, String url) {
        List<NovelTextItem> list = new ArrayList<>();
        Elements elements = document.select("#list > dl > dd");
        do {
            elements.remove(0);
        }while(!elements.get(0).nextElementSibling().nodeName().equals("dt"));
        elements.remove(0);
        for (Element item : elements) {
            NovelTextItem novelTextItem = new NovelTextItem();
            novelTextItem.setChapter(item.select("a").text());
            novelTextItem.setUrl(getDomain() + item.select("a").attr("href").substring(1));
            list.add(novelTextItem);
        }
        return list;
    }

    @Override
    public NovelText text(Document document) {
        NovelText novelText = new NovelText();
        novelText.setText(withBr(document, "#content", " ", DOUBLE_LE_RF));
        novelText.setChapter(document.select("#wrapper > div.content_read > div > div.bookname > h1").text());
        return novelText;
    }

    @Override
    public String getRankUrl(int type) {
        return getDomain() + "paihangbang/";
    }

    @Override
    public List<String> rank(Document document, int type) {
        List<String> list = new ArrayList<>();
        Elements elements = document.select("#main > div.b4").get(1).select("ul");
        switch (type) {
            case DAY_RANK:
                elements = elements.get(1).select("li");
                break;
            case MONTH_RANK:
                elements = elements.get(2).select("li");
                break;
            case TOTAL_RANK:
                elements = elements.get(0).select("li");
                break;
        }
        elements.remove(0);
        for (int i = 0; i < Math.min(elements.size(), RANK_COUNT); i++) {
            String detailUrl = elements.get(i).select("a").attr("href");
            list.add(detailUrl);
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
        Elements elements = document.select("#newscontent > div.l > ul > li");
        for (Element element : elements) {
            NovelRemind novelRemind = new NovelRemind();
            novelRemind.setName(element.select("span.s2 > a").text());
            novelRemind.setChapter(element.select("span.s3 > a").text());
            list.add(novelRemind);
        }
        return list;
    }

    @Override
    public String getAddress(String addr, NovelInfo novelInfo) {
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
        return MIDDLE_THREAD_COUNT;
    }
}
