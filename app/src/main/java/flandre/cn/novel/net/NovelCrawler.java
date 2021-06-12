package flandre.cn.novel.net;

import android.text.TextUtils;
import android.util.Log;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import flandre.cn.novel.bean.data.novel.*;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.*;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.*;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

/**
 * 爬虫的基类
 * 创建源时继承该类
 * 2019.12.8
 */
public class NovelCrawler implements Crawler {
    private static final String PC = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";
    private static final String PE = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1";

    private Resolve resolve;
    private static Retrofit retrofit;

    static {
        OkHttpClient client = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectionPool(new ConnectionPool(MAX_THREAD_COUNT, TIMEOUT, TimeUnit.MILLISECONDS))
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(new Interceptor() {
                    @NotNull
                    @Override
                    public Response intercept(@NotNull Chain chain) throws IOException {
                        // @Body 有写问题，他会给数据加个""，而且没有Content-Type头
                        // 如果使用Field的话，解决不了Charset问题，设置Charset怎么也要拦截器
                        // 如果把Retrofit不设置为静态，感觉又有点浪费资源
                        Request request = chain.request();
                        String s = bodyToString(request.body());
                        if (!TextUtils.isEmpty(s)) {
                            Request build = request.newBuilder().post(RequestBody.create(s.substring(1, s.length() - 1).getBytes()))
                                    .header("Content-Type", "application/x-www-form-urlencoded").build();
                            return chain.proceed(build);
                        }
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(new RedirectInterceptor()).build();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.flandre.com/") // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create()) //设置使用Gson解析(记得加入依赖)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 支持RxJava
                .client(client)
                .build();
    }

    private static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }


    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public NovelCrawler(Resolve resolve) {
        this.resolve = resolve;
        this.resolve.attachCrawler(this);
    }

    @Override
    public void getNovelInfo(String addr, final NovelInfo novelInfo, final OnRequestComplete<NovelInfo> complete) {
        final String finalAddr = resolve.getAddress(addr, novelInfo);
        Observable<ResponseBody> responseBodyObservable = crawlerGET(addr);
        responseBodyObservable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Function<ResponseBody, NovelInfo>() {
                    @Override
                    public NovelInfo apply(ResponseBody responseBody) throws IOException, ParseException {
                        Document document = getDocument(responseBody, finalAddr, resolve);
                        resolve.searchData(document, novelInfo);
                        return novelInfo;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NovelInfo>() {
                    @Override
                    public void accept(NovelInfo novelInfo) {
                        complete.onSuccess(novelInfo);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        complete.onFail(throwable);
                    }
                });
    }

    @Override
    public void search(final String s, final OnRequestComplete<List<NovelInfo>> complete) {
        Observable<ResponseBody> responseBodyObservable = resolve.getSearchObservable(s);
        responseBodyObservable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(new Consumer<ResponseBody>() {
            private int[] count = new int[]{0};

            @Override
            public void accept(ResponseBody responseBody) throws IOException {
                Document document = getDocument(responseBody, resolve.getDomain() + "search/", resolve);
                final List<NovelInfo> list = new ArrayList<>();
                List<String> url = resolve.search(document, s);
                int end = min(url.size(), SEARCH_COUNT);
                ExecutorService fixedThreadPool = Executors.newFixedThreadPool(resolve.getThreadCount());
                for (String s1 : url) {
                    NovelInfo novelInfo = new NovelInfo();
                    getSearchData(novelInfo, s1, list, count, end, -1, fixedThreadPool, complete);
                }
                if (end == 0)
                    AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                        @Override
                        public void run() {
                            complete.onSuccess(list);
                        }
                    });
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(final Throwable throwable) {
                AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        complete.onFail(throwable);
                    }
                });
            }
        });
    }

    private void getSearchData(final NovelInfo novelInfo, final String detailUrl, final List<NovelInfo> list,
                               final int[] count, final int end, final int rank, ExecutorService fixedThreadPool, final OnRequestComplete<List<NovelInfo>> complete) {
        Observable<ResponseBody> responseBodyObservable = crawlerGET(detailUrl);
        responseBodyObservable.subscribeOn(Schedulers.from(fixedThreadPool))
                .observeOn(Schedulers.from(fixedThreadPool))
                .map(new Function<ResponseBody, NovelInfo>() {
                    @Override
                    public NovelInfo apply(ResponseBody responseBody) {
                        try {
                            Document doc = getDocument(responseBody, detailUrl, resolve);
                            resolve.searchData(doc, novelInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return novelInfo;
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<NovelInfo>() {
            @Override
            public void accept(NovelInfo novelInfo) {
                if (novelInfo != null & list != null) {
                    if (rank != -1) list.set(rank, novelInfo);  // 因为是排行榜, 所以要确保位置不变
                    else list.add(novelInfo);
                }
                count[0]++;
                if (count[0] == end) complete.onSuccess(list);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                count[0]++;
                if (count[0] == end) complete.onSuccess(list);
            }
        });
    }

    public static Document getDocument(ResponseBody responseBody, String url, Resolve resolve) throws IOException {
        try {
            String result = new String(responseBody.bytes(), resolve.getCharset());
            if (check(result)) throw new CookieHackerException();
            return getDocument(result, url);
        } finally {
            if (responseBody != null) responseBody.close();
        }
    }

    @Override
    public void list(final String URL, ExecutorService fixedThreadPool, final OnRequestComplete<List<NovelTextItem>> complete) {
        Observable<ResponseBody> responseBodyObservable = crawlerGET(URL);
        responseBodyObservable.subscribeOn(fixedThreadPool == null ? Schedulers.io() : Schedulers.from(fixedThreadPool))
                .observeOn(fixedThreadPool == null ? Schedulers.io() : Schedulers.from(fixedThreadPool))
                .map(new Function<ResponseBody, List<NovelTextItem>>() {
                    @Override
                    public List<NovelTextItem> apply(ResponseBody responseBody) throws IOException {
                        Document document = getDocument(responseBody, URL, resolve);
                        return resolve.list(document, URL);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<NovelTextItem>>() {
                    @Override
                    public void accept(List<NovelTextItem> novelTextItems) {
                        complete.onSuccess(novelTextItems);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        complete.onFail(throwable);
                    }
                });
    }

    @Override
    public void text(final String URL, final String table, ExecutorService fixedThreadPool, final OnRequestComplete<WrapperNovelText> complete) {
        Observable<ResponseBody> responseBodyObservable = crawlerGET(URL);
        responseBodyObservable
                .subscribeOn(fixedThreadPool != null ? Schedulers.from(fixedThreadPool) : Schedulers.io())
                .observeOn(fixedThreadPool != null ? Schedulers.from(fixedThreadPool) : Schedulers.io())
                .map(new Function<ResponseBody, WrapperNovelText>() {
                    @Override
                    public WrapperNovelText apply(ResponseBody responseBody) throws IOException {
                        Document document = getDocument(responseBody, URL, resolve);
                        NovelText text = resolve.text(document);
                        WrapperNovelText wrapperNovelText = new WrapperNovelText();
                        wrapperNovelText.setNovelText(text);
                        wrapperNovelText.setTable(table);
                        return wrapperNovelText;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WrapperNovelText>() {
                    @Override
                    public void accept(WrapperNovelText novelText) {
                        complete.onSuccess(novelText);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        complete.onFail(throwable);
                    }
                });
    }

    @Override
    public void update(final String URL, final int id, final int newId, ExecutorService fixedThreadPool, final UpdateFinish updateFinish) {
        list(URL, fixedThreadPool, new OnRequestComplete<List<NovelTextItem>>() {
            @Override
            public void onSuccess(List<NovelTextItem> data) {
                if (newId > 0) {
                    data.subList(0, newId).clear();
                }
                updateFinish.onUpdateFinish(id, data);
            }

            @Override
            public void onFail(Throwable e) {
                updateFinish.onUpdateFinish(id, null);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void download(String URL, final int id, final String table, ExecutorService fixedThreadPool, final DownloadFinish downloadFinish) {
        text(URL, table, fixedThreadPool, new OnRequestComplete<WrapperNovelText>() {
            @Override
            public void onSuccess(WrapperNovelText data) {
                downloadFinish.onDownloadFinish(data.getNovelText(), table, id);
            }

            @Override
            public void onFail(Throwable e) {
                downloadFinish.onDownloadFinish(null, null, id);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void rank(final int type, final OnRequestComplete<List<NovelInfo>> complete) {
        final String url = resolve.getRankUrl(type);
        Observable<ResponseBody> responseBodyObservable = crawlerGET(url);
        responseBodyObservable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<ResponseBody>() {
                    private int[] count = new int[]{0};

                    @Override
                    public void accept(ResponseBody responseBody) throws IOException {
                        final List<NovelInfo> list = new ArrayList<>();
                        for (int i = 0; i < RANK_COUNT; i++) list.add(null);
                        Document document = getDocument(responseBody, url, resolve);
                        List<String> url = resolve.rank(document, type);
                        int end = Math.min(url.size(), RANK_COUNT);
                        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(resolve.getThreadCount());
                        for (int i = 0; i < end; i++) {
                            NovelInfo novelInfo = new NovelInfo();
                            String detailUrl = url.get(i);
                            getSearchData(novelInfo, detailUrl, list, count, end, i, fixedThreadPool, complete);
                        }
                        if (end == 0)
                            AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                                @Override
                                public void run() {
                                    complete.onSuccess(list);
                                }
                            });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(final Throwable throwable) {
                        AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                            @Override
                            public void run() {
                                complete.onFail(throwable);
                            }
                        });
                    }
                });
    }

    @Override
    public void remind(final OnRequestComplete<List<NovelRemind>> complete) {
        Observable<ResponseBody> responseBodyObservable = crawlerGET(resolve.getRemindUrl());
        responseBodyObservable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Function<ResponseBody, List<NovelRemind>>() {
                    @Override
                    public List<NovelRemind> apply(ResponseBody responseBody) throws IOException {
                        Document document = getDocument(responseBody, resolve.getRemindUrl(), resolve);
                        return resolve.remind(document);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<NovelRemind>>() {
                    @Override
                    public void accept(List<NovelRemind> novelReminds) {
                        complete.onSuccess(novelReminds);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        complete.onFail(throwable);
                    }
                });
    }

    /**
     * 发送POST请求
     *
     * @return Document
     */
    @Override
    public Observable<ResponseBody> crawlerPOST(String Url, String data) {
        NovelService textService = retrofit.create(NovelService.class);
        return textService.post(Url, data, resolve.getDomain(), resolve.getDomain(), resolve.isPC() ? PC : PE);
    }

    /**
     * 发送GET请求
     *
     * @return Document
     */
    @Override
    public Observable<ResponseBody> crawlerGET(String Url) {
        NovelService textService = retrofit.create(NovelService.class);
        return textService.get(Url, resolve.getDomain(), resolve.getDomain(), resolve.isPC() ? PC : PE);
    }

    @Override
    public Resolve getResolve() {
        return resolve;
    }

    private static boolean check(String response) {
        return response.contains("http://1.1.1.2:89/cookie/flash.js") || response.contains("http://10.30.1.30:89/flashredir.html");
    }

    private static Document getDocument(String result, String url) {
        return Jsoup.parse(result, url);
    }

    public static class CookieHackerException extends RuntimeException {

    }

    static class RedirectInterceptor implements Interceptor {

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            okhttp3.Request request = chain.request();
            Response response = chain.proceed(request);
            int code = response.code();
            if (code == 307 || code == 301 || code == 302) {
                // 获取重定向的地址
                String location = response.headers().get("Location");
                // 重新构建请求
                Request newRequest = request.newBuilder().url(location).build();
                response = chain.proceed(newRequest);
            }
            return response;
        }
    }
}
