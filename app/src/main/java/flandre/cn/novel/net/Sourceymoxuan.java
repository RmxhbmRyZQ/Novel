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
import java.util.ArrayList;
import java.util.List;

import static flandre.cn.novel.net.Crawler.*;
import static java.lang.Math.min;

public class Sourceymoxuan extends BaseResolve {
    public static String TAG = "http://www.yanmoxuan.net/";

    @Override
    public Observable<ResponseBody> getSearchObservable(String s) {
        return crawler.crawlerGET(getDomain() + "search.htm?keyword=" + s);
    }

    @Override
    public List<String> search(Document document, String s) {
        List<String> list = new ArrayList<>();
        Elements elements = document.select("section.lastest > ul > li");
        elements.remove(0);
        elements.remove(elements.size() - 1);
        elements = orderBy(elements, "span.n2 > a", s);
        for (int i = 0; i < min(elements.size(), SEARCH_COUNT); i++) {
            String detailUrl = getProtocol() + elements.get(i).select("span.n2 > a").get(0).attr("href");
            list.add(detailUrl);
        }
        return list;
    }

    @Override
    public void searchData(Document document, NovelInfo novelInfo) throws ParseException {

        Elements elements = document.select("body > section > div.left > article.info");
        String imgUrl = elements.select("div.cover > img").attr("src");

        novelInfo.setName(elements.select("header > h1").get(0).text());
        novelInfo.setAuthor(elements.select("p.detail.pt20 > i:nth-child(1) > a").text());
        novelInfo.setComplete(elements.select("p.detail.pt20 > i:nth-child(3)").text().contains("完本") ? 1 : 0);
        novelInfo.setChapter(elements.select("p").get(2).select("i > a").select("i > a").text());
        novelInfo.setIntroduce(withBr(elements, "p.desc", "", DOUBLE_LE_RF));
        novelInfo.setSource(Sourceymoxuan.class.getName());
        novelInfo.setUrl(getProtocol() + elements.select("footer > a").get(0).attr("href"));
        novelInfo.setImagePath(imgUrl);
    }

    @Override
    public List<NovelTextItem> list(Document document, String url) {
        List<NovelTextItem> list = new ArrayList<>();

        Element data = document.select("body > section > article").get(0);

        Element last = data.select(".col1.volumn").get(0);
        Element next;

        do {
            next = last.nextElementSibling();
            last.remove();
            last = next;
        } while (!next.attr("class").equals("col1 volumn"));

        data.select(".col1.volumn").remove();

        Elements info = data.select("ul > li > a");

        for (Element ele : info) {
            NovelTextItem textItem = new NovelTextItem();

            textItem.setChapter(ele.text());
            textItem.setUrl(getProtocol() + ele.attr("href"));

            list.add(textItem);
        }

        return list;
    }

    @Override
    public NovelText text(Document document) {
        try {
            Thread.sleep((long) (Math.random() * 2000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        document.select("#content script").remove();

        NovelText novelText = new NovelText();
        novelText.setText(withBr(document, "#content", " ", DOUBLE_LE_RF));
        novelText.setChapter(document.select("#a3 > header > h1").get(0).text());

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
                elements = document.select("body > section.container > div.left > section > ul > li > span.n > a");
                break;
            case MONTH_RANK:
                elements = document.select("body > section.container > div.left > article.author.clearfix > div > dl > dt > a");
                break;
            case TOTAL_RANK:
                elements = document.select("body > section.container > div.right > section:nth-child(1) > div > ul > li > a");
                break;
        }
        for (int i = 0; i < Math.min(elements.size(), RANK_COUNT); i++) {
            String detailUrl = getProtocol() + elements.get(i).attr("href");
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
        Elements elements = document.select("body > section.container > div.right > section:nth-child(1) > div > ul > li");
        for (Element element : elements) {
            NovelRemind novelRemind = new NovelRemind();
            novelRemind.setName(element.select("a").get(1).text());
            novelRemind.setChapter(element.select("a").get(0).text());
            list.add(novelRemind);
        }
        return list;
    }

    @Override
    public String getAddress(String addr, NovelInfo novelInfo) {
        addr = addr.replace("/index.html", "");
        addr = getDomain() + "text_" + addr.substring(addr.lastIndexOf("/") + 1) + ".html";
        return addr;
    }

    @Override
    public String getDomain() {
        return TAG;
    }

    @Override
    public String getCharset() {
        return "UTF-8";
    }

    @Override
    public int getThreadCount() {
        return MIN_THREAD_COUNT;
    }
}
