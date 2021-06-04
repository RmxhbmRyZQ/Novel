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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static flandre.cn.novel.net.Crawler.*;
import static java.lang.Math.min;

public class Sourcembtxt extends BaseResolve {
    public static String TAG = "https://www.mbtxt.la/";

    @Override
    public Observable<ResponseBody> getSearchObservable(String s) {
        return crawler.crawlerPOST(getDomain() + "modules/article/search.php", "searchkey=" + setUnicode(s) + "&action=login&submit=");
    }

    @Override
    public List<String> search(Document document, String s) {
        List<String> list = new ArrayList<>();
        if (document.select("dl.chapterlist").size() > 0) {
            list.add(document.select("head > link").get(0).attr("href"));
        } else {
            Elements elements = document.select("#fengtui > div.bookbox");
            elements = orderBy(elements, "div > div.bookinfo > h4 > a", s);
            for (int i = 0; i < min(elements.size(), SEARCH_COUNT); i++) {
                String detailUrl = getDomain() + elements.get(i).select("div > div.bookinfo > h4 > a").attr("href").substring(1);
                list.add(detailUrl);
            }
        }
        return list;
    }

    @Override
    public void searchData(Document doc, NovelInfo novelInfo) {
        Element title = doc.select("body > div.container > div.content > div:nth-child(2) > div.bookinfo").get(0);

        novelInfo.setName(title.select("h1").text());
        novelInfo.setAuthor(title.select("p.booktag > a").text());
        novelInfo.setComplete(title.select("p.booktag > span.red").text().contains("连载") ? 0 : 1);
        novelInfo.setIntroduce(title.select("p.bookintro").text());
        novelInfo.setUrl(doc.baseUri());
        novelInfo.setSource(Sourcembtxt.this.getClass().getName());
        novelInfo.setChapter(title.select("p:nth-child(4) > a").get(0).text());
        String imgUrl = doc.select("body > div.container > div.content > div:nth-child(2) > div.bookcover.hidden-xs > img").attr("src");
        novelInfo.setImagePath(imgUrl);
    }

    @Override
    public List<NovelTextItem> list(Document document, String url) {
        List<NovelTextItem> list = new ArrayList<>();
        Elements elements = document.select("#list-chapterAll > dd");
        for (Element element : elements) {
            NovelTextItem textItem = new NovelTextItem();
            textItem.setChapter(element.select("a").text());
            textItem.setUrl(url + element.select("a").attr("href"));
            list.add(textItem);
        }
        return list;
    }

    @Override
    public NovelText text(Document document) throws IOException {
        document.select("body > div.container > div.content > div.book.read > div.readcontent > div").remove();
        document.select("body > div.container > div.content > div.book.read > div.readcontent > p").remove();
        document.select("body > div.container > div.content > div.book.read > h1 > small").remove();
        String text = withBr(document, "body > div.container > div.content > div.book.read > div.readcontent", " ", DOUBLE_LE_RF);

        if (document.select("#linkNext").text().equals("下一页")) {
            String url = document.select("#linkNext").attr("href");
            TmpService tmpService = NovelCrawler.getRetrofit().create(TmpService.class);
            ResponseBody body = tmpService.get(url).execute().body();
            text += text(NovelCrawler.getDocument(body, url, this)).getText();
        }

        NovelText novelText = new NovelText();
        novelText.setChapter(document.select("body > div.container > div.content > div.book.read > h1").text());
        novelText.setText(text.replace("-->>", ""));
        return novelText;
    }

    @Override
    public String getRankUrl(int type) {
        String url = null;
        switch (type) {
            case DAY_RANK:
                url = getDomain() + "allvote.html";
                break;
            case MONTH_RANK:
                url = getDomain() + "monthvote.html";
                break;
            case TOTAL_RANK:
                url = getDomain() + "weekvisit.html";
                break;
        }
        return url;
    }

    @Override
    public List<String> rank(Document document, int type) {
        List<String> list = new ArrayList<>();
        Elements elements = document.select("#fengtui > div.bookbox");
        for (int i = 0; i < Math.min(elements.size(), RANK_COUNT); i++) {
            String detailUrl = elements.get(i).select("div > div.bookinfo > h4 > a").attr("href");
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
        Elements elements = document.select("#gengxin > ul > li");
        for (Element element : elements) {
            NovelRemind novelRemind = new NovelRemind();
            novelRemind.setChapter(element.select("span.s3 > a").text());
            novelRemind.setName(element.select("span.s2 > a").text());
            list.add(novelRemind);
        }
        return list;
    }

    @Override
    public String getAddress(String addr, NovelInfo novelInfo) {
        return addr;
    }

    @Override
    public String getDomain() {
        return TAG;
    }

    @Override
    public String getCharset() {
        return "GBK";
    }

    @Override
    public int getThreadCount() {
        return MIDDLE_THREAD_COUNT;
    }
}
