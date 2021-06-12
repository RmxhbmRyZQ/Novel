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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static flandre.cn.novel.net.Crawler.*;
import static java.lang.Math.min;

public class Sourceaixiatxt extends BaseResolve {
    public static String TAG = "http://www.ixiatxt.com/";

    @Override
    public Observable<ResponseBody> getSearchObservable(String s) {
        return crawler.crawlerGET(getDomain() + "search.php?s=&searchkey=" + setUnicode(s));
    }

    @Override
    public List<String> search(Document document, String s) {
        List<String> list = new ArrayList<>();
        Elements elements = document.select("body > div:nth-child(4) > div.list > div > ul > li");
        Pattern pattern = Pattern.compile("《(.*?)》");
        elements = orderBy(elements, "a", pattern, s);
        for (int i = 0; i < min(elements.size(), SEARCH_COUNT); i++) {
            String detailUrl = getDomain() + elements.get(i).select("a").get(0).attr("href").substring(1);
            list.add(detailUrl);
        }
        return list;
    }

    @Override
    public void searchData(Document document, NovelInfo novelInfo) {
        Elements elements = document.select("body > div:nth-child(4) > div.show > div:nth-child(1) > div");
        String imgUrl = getDomain() + elements.select("div.detail_pic > img").attr("src").substring(1);
        Matcher matcher = Pattern.compile("《(.*?)》").matcher(elements.select("div.detail_info > div > h1").text());
        if (matcher.find() && matcher.groupCount() > 0)
            novelInfo.setName(matcher.group(1));
        else return;
        novelInfo.setAuthor(elements.select("div.detail_info > div > ul > li:nth-child(6)").text().substring(5));
        novelInfo.setComplete(elements.select("div.detail_info > div > ul > li:nth-child(5)").text().contains("完本") ? 1 : 0);
        novelInfo.setChapter(elements.select("div.detail_info > div > ul > li:nth-child(8) > a").text());
        novelInfo.setIntroduce(document.select("body > div:nth-child(4) > div.show > div:nth-child(2) > div.showInfo > p").text());
        novelInfo.setSource(Sourceaixiatxt.class.getName());
        novelInfo.setUrl(getDomain() + document.select("body > div:nth-child(4) > div.show > div:nth-child(4) " +
                "> div.showDown > ul > li:nth-child(1) > a").attr("href").substring(1));
        novelInfo.setImagePath(imgUrl);
    }

    @Override
    public List<NovelTextItem> list(Document document, String url) {
        List<NovelTextItem> list = new ArrayList<>();
        Element element = document.select("#info").get(2);
        Elements items = element.select("div > ul > li");
        for (Element item : items) {
            NovelTextItem novelTextItem = new NovelTextItem();
            novelTextItem.setChapter(item.select("a").text());
            novelTextItem.setUrl(url + item.select("a").attr("href"));
            list.add(novelTextItem);
        }
        return list;
    }

    @Override
    public NovelText text(Document document) {
        document.select("#center_tip").remove();
        NovelText novelText = new NovelText();
        novelText.setText(withBr(document, "#content1", " ", DOUBLE_LE_RF));
        novelText.setChapter(document.select("#info > div > h1").text());
        return novelText;
    }

    @Override
    public String getRankUrl(int type) {
        return getDomain();
    }

    @Override
    public List<String> rank(Document document, int type) {
        List<String> list = new ArrayList<>();
        Elements elements = null;
        switch (type) {
            case DAY_RANK:
                elements = document.select("body > div:nth-child(3) > div.mpLeft > div:nth-child(2) > div > ul:nth-child(2) > li");
                break;
            case MONTH_RANK:
                elements = document.select("body > div:nth-child(3) > div.mpLeft > div:nth-child(2) > div > ul:nth-child(3) > li");
                break;
            case TOTAL_RANK:
                elements = document.select("body > div:nth-child(3) > div.mpLeft > div:nth-child(2) > div > ul:nth-child(4) > li");
                break;
        }
        for (int i = 0; i < Math.min(elements.size(), RANK_COUNT); i++) {
            String detailUrl = getDomain() + elements.get(i).select("a").attr("href").substring(1);
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
        document.select("body > div:nth-child(3) > div.mpRight > div.fic_type_box > div.fic_type_tabcont > div > ul > li:nth-child(1)").remove();
        Elements elements = document.select("body > div:nth-child(3) > div.mpRight > div.fic_type_box > div.fic_type_tabcont > div > ul > li");
        Pattern pattern = Pattern.compile("《(.*?)》");
        for (Element element : elements) {
            NovelRemind novelRemind = new NovelRemind();
            Matcher matcher = pattern.matcher(element.select("a.name").text());
            if (matcher.find() && matcher.groupCount() > 0)
                novelRemind.setName(matcher.group(1));
            else continue;
            novelRemind.setChapter(element.select("a:nth-child(4)").text());
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
        return MIN_THREAD_COUNT;
    }
}
