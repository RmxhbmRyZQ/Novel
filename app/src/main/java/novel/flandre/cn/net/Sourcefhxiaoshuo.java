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

import static java.lang.Math.min;
import static novel.flandre.cn.net.Crawler.*;

public class Sourcefhxiaoshuo extends BaseResolve {
    public static String TAG = "https://www.ciyuxs.com/";

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
        return "GBK";
    }

    @Override
    public int getThreadCount() {
        return MIDDLE_THREAD_COUNT;
    }

    @Override
    public Observable<ResponseBody> getSearchObservable(String s) {
        return crawler.crawlerPOST(getDomain() + "modules/article/search.php", "searchkey=" + setUnicode(s));
    }

    @Override
    public List<String> search(Document document, String s) {
        List<String> list = new ArrayList<>();

        if (document.select("#fmimg").size() != 0) {
            list.add(document.select("div.con_top > a").get(1).attr("href"));
        } else {
            Elements elements = document.select("table.grid > tbody > tr");
            elements.remove(0);
//            elements.get(0).remove();
            for (int i = 0; i < min(elements.size(), SEARCH_COUNT); i++) {
                String detailUrl = elements.get(i).select("td.odd > a").attr("href");
                list.add(detailUrl);
            }
        }

        return list;
    }

    @Override
    public void searchData(Document doc, NovelInfo novelInfo) {
        String imgUrl = doc.select("#fmimg > img").get(0).attr("src");

        novelInfo.setName(doc.select("#info > h1").get(0).text());
        novelInfo.setAuthor(doc.select("#info > p").get(0).text().substring(4));
        novelInfo.setChapter(doc.select("#info > font > p > a").get(0).text());
        String introduce = doc.select("#intro > .introtxt").get(0).text().substring(5);
        novelInfo.setIntroduce(introduce.equals("") ? "没有简介" : introduce);
        novelInfo.setUrl(doc.baseUri());
        novelInfo.setComplete(0);
        novelInfo.setSource(Sourcefhxiaoshuo.class.getName());
        novelInfo.setImagePath(imgUrl);
    }

    @Override
    public List<NovelTextItem> list(Document document, String url) {
        List<NovelTextItem> list = new ArrayList<>();

        Elements elements = document.select("#list > dl > dd > a");

        int i;
        for (i = 0; i < elements.size(); i++) {
            if (i % 3 == 0 && i != 0) {
                for (int j = i - 1; j >= i - 3; j--) {
                    NovelTextItem textItem = new NovelTextItem();
                    Element element = elements.get(j);
                    textItem.setChapter(element.text());
                    textItem.setUrl(element.attr("href"));
                    list.add(textItem);
                }
            }
        }
        int leave = i - (i % 3 != 0 ? i % 3 : 3);
        for (int k = i - 1; k >= leave; k--) {
            NovelTextItem textItem = new NovelTextItem();
            Element element = elements.get(k);
            textItem.setUrl(element.attr("href"));
            textItem.setChapter(element.text());
            list.add(textItem);
        }

        return list;
    }

    @Override
    public NovelText text(Document document) {

        document.select("#TXT > div").remove();
        document.select("#TXT > font").remove();
        String text = withBr(document, "#TXT", " ", DOUBLE_LE_RF);
        NovelText novelText = new NovelText();
        novelText.setText(text);
        novelText.setChapter(document.select(".zhangjieming > h1").get(0).text());

        return novelText;
    }

    @Override
    public String getRankUrl(int type) {
        String url = null;
        switch (type) {
            case DAY_RANK:
                url = getDomain() + "weekvisit/1/";
                break;
            case MONTH_RANK:
                url = getDomain() + "monthvisit/1/";
                break;
            case TOTAL_RANK:
                url = getDomain() + "allvisit/1/";
                break;
        }
        return url;
    }

    @Override
    public List<String> rank(Document document, int type) {
        List<String> list = new ArrayList<>();
        Elements elements = document.select("#alist > div");
        for (int i = 0; i < min(elements.size(), RANK_COUNT); i++) {
            String detailUrl = elements.get(i).select("div.info > div.title > h2 > a").attr("href");
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
        for (Element element:elements){
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
}
