package flandre.cn.novel.net;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseResolve implements Resolve {
    public static final String BR_REPLACEMENT = "0x0a";
    public static final String DOUBLE_LE_RF = "\r\n";
    Crawler crawler;

    String getProtocol(){
        return getDomain().split("/")[0];
    }

    @Override
    public void attachCrawler(Crawler crawler){
        this.crawler = crawler;
    }

    public boolean check(String response) {
        return response.contains("http://1.1.1.2:89/cookie/flash.js") || response.contains("http://10.30.1.30:89/flashredir.html");
    }

    private Document getDocument(String result, String url) {
        return Jsoup.parse(result, url);
    }

    public static String getDOMAIN(String DOMAIN) {
        String http = "http://";
        if (DOMAIN.startsWith(http)) {
            return DOMAIN.substring(http.length(), DOMAIN.length() - 1);
        } else {
            return DOMAIN.substring(http.length() + 1, DOMAIN.length() - 1);
        }
    }

    /**
     * 给小说搜索结果排序
     *
     * @param elements 小说列表
     * @param selector 每个列表获取小说名的解析式
     * @param name     搜索的小说名
     */
    Elements orderBy(Elements elements, String selector, String name) {
        return orderBy(elements, selector, null, name);
    }

    Elements orderBy(Elements elements, String selector, Pattern pattern, String name) {
        try {
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                String text = element.select(selector).text();
                if (pattern != null) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find() && matcher.groupCount() > 0) text = matcher.group(1);
                }
                if (setUnicode(text).equals(name)) {
                    elements.add(0, element.clone());
                    elements.remove(i + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return elements;
    }

    /**
     * 把Br转换成空格
     *
     * @return 转换后的文本
     */
    String withBr(Elements element, String select) {
        return withBr(element, select, "", "");
    }

    String withBr(Elements elements, String select, String extra, String rep) {
        elements.select(select + " br").append(BR_REPLACEMENT);
        elements.select(select + " p").append(BR_REPLACEMENT);
        Matcher matcher = Pattern.compile("[" + BR_REPLACEMENT + extra + "]{" + BR_REPLACEMENT.length() + ",}")
                .matcher(elements.select(select).text());
        return matcher.replaceAll("\r\n" + rep);
    }

    String withBr(Element element, String select) {
        return withBr(element, select, "", "");
    }

    String withBr(Element element, String select, String extra, String rep) {
        element.select(select + " br").append(BR_REPLACEMENT);
        element.select(select + " p").append(BR_REPLACEMENT);
        Matcher matcher = Pattern.compile("[" + BR_REPLACEMENT + extra + "]{" + BR_REPLACEMENT.length() + ",}")
                .matcher(element.select(select).text());
        return matcher.replaceAll("\r\n" + rep);
    }

    String trim(String s, boolean p) {
        int len = s.length();
        int st = 0;
        if (p)
            while ((st < len) && (s.charAt(st) <= ' ')) {
                st++;
            }
        else
            while ((st < len) && (s.charAt(len - 1) <= ' ')) {
                len--;
            }
        return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;
    }

    /**
     * 为线程池设置超时时间
     *
     * @param fixedThreadPool 线程池对象
     * @param timeout         超时的时间
     */
    void checkTimeOut(ExecutorService fixedThreadPool, int timeout) {
        try {
            fixedThreadPool.shutdown();
            if (!fixedThreadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转码, 把输入的字符串转换成 {@link Resolve#getCharset()} 这个编码
     *
     * @param s 要转码的字符串
     */
    String setUnicode(String s) {
        try {
            s = URLEncoder.encode(s, getCharset());
            return s;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }
}
