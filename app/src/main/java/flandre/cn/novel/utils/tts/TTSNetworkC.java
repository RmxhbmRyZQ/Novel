package flandre.cn.novel.utils.tts;

import android.support.annotation.NonNull;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static flandre.cn.novel.net.Crawler.MIN_THREAD_COUNT;
import static flandre.cn.novel.net.Crawler.TIMEOUT;

/**
 * 调用https://www.coder.work/text2audio的API进行TTS
 * robots.txt看上去没有现在这个API的调用
 */
public class TTSNetworkC implements TTSNetwork {
    private static final String DOMAIN = "https://www.coder.work/text2audio";

    private OkHttpClient mCrawler;

    public TTSNetworkC(){
        mCrawler = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectionPool(new ConnectionPool(MIN_THREAD_COUNT, TIMEOUT, TimeUnit.MILLISECONDS))
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS).build();
    }

    @Override
    public void tts(final String text, int voice, int speed, int tone, int volume, final int extra, @NonNull final OnFinish onFinish) {
        byte[] bytes = ("{\"tex\":\""  + text + "\",\"spd\":" + speed + ",\"pit\":" +
                tone + ",\"vol\":" + volume + ",\"per\":" + voice + "}: ")
                .replace("\r", " ").replace("\n", " ").getBytes();
        RequestBody requestBody = RequestBody.create(bytes);
        Request.Builder builder = new Request.Builder().url(DOMAIN + "/getresult").post(requestBody);
        builder.addHeader("Content-Length", String.valueOf(bytes.length));

        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
        builder.addHeader("Connection", "close");
        builder.addHeader("Referer", DOMAIN);
        builder.addHeader("Origin:", DOMAIN);
        builder.addHeader("connection", "Keep-Alive");
        mCrawler.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onFinish.onFail(text, extra);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (body != null) {
                    String result = body.string();
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getInt("errcode") == 0) {
                            onFinish.onFinish(jsonObject.getString("mp3url"), extra);
                        }else onFinish.onFail(text, extra);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onFinish.onFail(text, extra);
                    }
                    body.close();
                }else onFinish.onFail(text, extra);
            }
        });

    }

    @Override
    public String getSupporter() {
        return "www.coder.work";
    }
}
