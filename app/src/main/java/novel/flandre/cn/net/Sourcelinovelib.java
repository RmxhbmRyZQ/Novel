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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;
import static novel.flandre.cn.net.Crawler.*;

public class Sourcelinovelib extends BaseResolve {
    public static String TAG = "https://w.linovelib.com/";

    @Override
    public boolean isPC() {
        return false;
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

    @Override
    public Observable<ResponseBody> getSearchObservable(String s) {
        return crawler.crawlerGET(getDomain() + "s/?searchkey=" + s + "&searchtype=all");
    }

    @Override
    public List<String> search(Document document, String s) {
        List<String> list = new ArrayList<>();
        if (s.endsWith("（漫画）")) return list;
        if (document.select("#bookDetailWrapper").size() > 0) {
            String u = getDomain() + document.select("#btnReadBook").attr("href").substring(1);
            list.add(getAddress(u, null));
        } else {
            Elements elements = document.select("body > div.page.page-finish > div > div.module > ol > li");
            orderBy(elements, "h4.book-title", s);
            List<Integer> integers = new ArrayList<>();
            for (int i = elements.size() - 1; i >= 0; i--)
                if (elements.get(i).select("h4.book-title").text().endsWith("(漫画)"))
                    integers.add(i);
            for (int i : integers) {
                elements.remove(i);
            }
            for (int i = 0; i < min(elements.size(), SEARCH_COUNT); i++) {
                String detailUrl = getDomain() + elements.get(i).select("a").attr("href").substring(1);
                list.add(detailUrl);
            }
        }
        return list;
    }

    @Override
    public void searchData(Document document, NovelInfo info) {

        Elements elements = document.select("#bookDetailWrapper > div > div.book-layout");
        String imgUrl = elements.select("img").attr("src");

        info.setName(elements.select("div > h2").text());
        info.setAuthor(elements.select("div > div > span").text());
        info.setChapter(document.select("#book-friend-list-container > li").get(0).select(" > a > span").get(0).text());
        info.setIntroduce(withBr(document, "#bookSummary > content", " ", DOUBLE_LE_RF));
        info.setUrl(getDomain() + document.select("#btnReadBook").attr("href").substring(1));
        info.setComplete(document.select("#bookDetailWrapper > div > div.book-layout > div > p.book-meta").get(1).text().contains("连载") ? 0 : 1);
        info.setSource(Sourcelinovelib.class.getName());
        info.setImagePath(imgUrl);
    }

    @Override
    public List<NovelTextItem> list(Document document, String u) {
        List<NovelTextItem> list = new ArrayList<>();
        Elements elements = document.select("#volumes > li.chapter-li.jsChapter");
        for (Element element : elements) {
            NovelTextItem textItem = new NovelTextItem();
            textItem.setUrl(getDomain() + element.select("a").attr("href").substring(1));
            textItem.setChapter(element.select("a").text());
            list.add(textItem);
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUrl().contains("avascript:cid(0)")) {  // 通过计算来得出章节的 url
                // https://www.linovelib.com/novel/2013/72087.html -> https://www.linovelib.com/novel/2013/72088.html
                if (i > 0) {
                    String url = list.get(i - 1).getUrl();
                    int s = url.lastIndexOf('/') + 1;
                    int l = url.lastIndexOf('.');
                    url = url.substring(0, s) + (Integer.parseInt(url.substring(s, l)) + 1) + url.substring(l);
                    list.get(i).setUrl(url);
                } else if (list.size() > 1) {
                    String url = list.get(1).getUrl();
                    int s = url.lastIndexOf('/') + 1;
                    int l = url.lastIndexOf('.');
                    url = url.substring(0, s) + (Integer.parseInt(url.substring(s, l)) - 1) + url.substring(l);
                    list.get(i).setUrl(url);
                }
            }
        }
        return list;
    }

    @Override
    public NovelText text(Document document) throws IOException {
        NovelText novelText = new NovelText();
        document.select("#acontent > div.cgo").remove();
        document.select("#acontent > div.divimage").remove();
        Elements elements = document.select("#acontent > p > img.imagecontent");
        for (Element element : elements) {
            element.parent().remove();
        }
        novelText.setChapter(document.select("#atitle").text());
        if (document.select("#acontent > p").size() == 0) {
            novelText.setText("假装有图片");
            return novelText;
        }
        String text = withBr(document, "#acontent", " ", DOUBLE_LE_RF);
        if (document.select("#footlink > a").get(3).text().equals("下一页")){
            String json = document.select("#aread > script").get(0).html();
            json = json.substring(json.indexOf('{'));
            // 用 JSONObject 莫名报错，还是正则吧
            Matcher matcher = Pattern.compile("url_next:\'(.*?)\',").matcher(json);
            matcher.find();
            String url = getDomain() + matcher.group(1).substring(1);

            ResponseBody body = crawler.get(url).execute().body();
            text += text(NovelCrawler.getDocument(body, url, this)).getText();
        } else
            text = text.substring(0, text.length() - DOUBLE_LE_RF.length() * 2);
        novelText.setText(text);
        return novelText;
    }

    @Override
    public String getRankUrl(int type) {
        String url = null;
        switch (type) {
            case DAY_RANK:
                url = getDomain() + "top/monthvisit/1.html";
                break;
            case MONTH_RANK:
                url = getDomain() + "top/monthvote/1.html";
                break;
            case TOTAL_RANK:
                url = getDomain() + "top/goodnum/1.html";
                break;
        }
        return url;
    }

    @Override
    public List<String> rank(Document document, int type) {
        List<String> list = new ArrayList<>();
        Elements elements = document.select("div.module-rank-booklist.active > ol > li > a");
        for (Element element : elements) {
            list.add(getDomain() + element.attr("href").substring(1));
        }
        return list;
    }

    @Override
    public String getRemindUrl() {
        return getDomain() + "top/newhot/1.html";
    }

    @Override
    public List<NovelRemind> remind(Document document) {
        List<NovelRemind> list = new ArrayList<>();
        Elements select = document.select("div.module-rank-booklist.active > ol > li > a");
        for (Element element : select) {
            NovelRemind novelRemind = new NovelRemind();
            novelRemind.setName(element.select("div.book-cell > h4").text());
            novelRemind.setChapter(element.select("div.book-cell > p").text());
            list.add(novelRemind);
        }
        return list;
    }

    @Override
    public String getAddress(String addr, NovelInfo novelInfo) {
        return addr.replace("/catalog", ".html");
    }
}
