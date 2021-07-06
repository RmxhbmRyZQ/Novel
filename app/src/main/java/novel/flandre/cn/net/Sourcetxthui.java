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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static novel.flandre.cn.net.Crawler.*;

public class Sourcetxthui extends BaseResolve {
    public static String TAG = "https://www.txthui.com/";

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

    @Override
    public Observable<ResponseBody> getSearchObservable(String s) {
        return crawler.crawlerPOST(getDomain() + "case.php?m=search", "key=" + setUnicode(s));
    }

    @Override
    public List<String> search(Document document, String s) {
        List<String> list = new ArrayList<>();
        Elements elements = document.select("#newscontent > div.l > ul > li");
        elements = orderBy(elements, "span.s2 > a", s);

        for (int i = 0; i < min(elements.size(), SEARCH_COUNT); i++) {
            String detailUrl = getDomain() + elements.get(i).select("span.s2 > a").get(0).attr("href").substring(1);
            list.add(detailUrl);
        }
        return list;
    }

    @Override
    public void searchData(Document doc, NovelInfo novelInfo) {

        String imgUrl = getDomain() + doc.select("#fmimg > img").attr("src").substring(1);
        novelInfo.setName(doc.select("#info > h1").get(0).text());
        novelInfo.setAuthor(doc.select("#info > p").get(0).text().substring(4));
        novelInfo.setComplete(doc.select("#fmimg > span.a").size() > 0 ? 1 : 0);
        novelInfo.setChapter(doc.select("#info > p").get(3).select("a").get(0).text());
        String s = withBr(doc, "#intro", "", DOUBLE_LE_RF);
        novelInfo.setIntroduce(!s.equals("") ? s : doc.select("#intro").text());
        novelInfo.setSource(Sourcetxthui.class.getName());
        novelInfo.setUrl(doc.baseUri());
        novelInfo.setImagePath(imgUrl);
    }

    @Override
    public List<NovelTextItem> list(Document document, String url) {
        List<NovelTextItem> list = new ArrayList<>();
        Elements elements = document.select("#list > dl").get(0).children();
        int i = 1;
        for (; i < elements.size(); i++) {
            if (elements.get(i).nodeName().equals("dt")) break;
        }
        for (++i; i < elements.size(); i++) {
            NovelTextItem textItem = new NovelTextItem();
            textItem.setUrl(getDomain() + elements.get(i).select("a").get(0).attr("href").substring(1));
            textItem.setChapter(elements.get(i).select("a").get(0).text());
            list.add(textItem);
        }

        return list;
    }

    @Override
    public NovelText text(Document document) throws IOException {
        Elements elements = document.select("#content > p > a");
        String nextUrl = null;
        if (elements.size() > 0) {
            nextUrl = getDomain() + elements.get(0).attr("href").substring(1);
            elements.remove();
        }
        NovelText novelText = new NovelText();
        String text = withBr(document, "#content", " 　 ", DOUBLE_LE_RF + "    ").replace("#", "");
        text = "    " + trim(text, true);
        novelText.setChapter(document.select("#wrapper > div.content_read > div > div.bookname > h1").get(0).text());

        if (nextUrl != null) {
            ResponseBody body = crawler.get(nextUrl).execute().body();
            text = text.substring(0, text.lastIndexOf("... ..."));
            text = text + trim(text(NovelCrawler.getDocument(body, nextUrl, this)).getText(), true);
        } else text = trim(text, false);
        novelText.setText(text);
        return novelText;
    }

    @Override
    public String getRankUrl(int type) {
        String url = null;
        switch (type) {
            case DAY_RANK:
                url = getDomain() + "wanben.html";
                break;
            case MONTH_RANK:
                url = getDomain() + "wanben.html";
                break;
            case TOTAL_RANK:
                url = getDomain() + "dushi.html";
                break;
        }
        return url;
    }

    @Override
    public List<String> rank(Document document, int type) {
        List<String> list = new ArrayList<>();
        Elements elements = null;
        switch (type) {
            case DAY_RANK:
                elements = document.select("#newscontent > div.l > ul > li");
                break;
            case MONTH_RANK:
                elements = document.select("#newscontent > div.r > ul > li");
                break;
            case TOTAL_RANK:
                elements = document.select("#newscontent > div.r > ul > li");
                break;
        }
        for (int i = 0; i < min(elements.size(), RANK_COUNT); i++) {
            String detailUrl = getDomain() + elements.get(i).select("span.s2 > a").attr("href").substring(1);
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
            novelRemind.setName(element.select("span.s2 > a").get(0).text());
            novelRemind.setChapter(element.select("span.s3 > a").text());
            list.add(novelRemind);

        }
        return list;
    }

    @Override
    public String getAddress(String addr, NovelInfo novelInfo) {
        return addr;
    }
}
