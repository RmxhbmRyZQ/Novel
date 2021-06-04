package flandre.cn.novel;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@SuppressWarnings("ALL")
public class ExampleUnitTest {
    interface Service{
        @POST("search")
        @FormUrlEncoded
        public Call<ResponseBody> get();
    }

    @Test
    public void addition_isCorrect() throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.washuge.com/") // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create()) //设置使用Gson解析(记得加入依赖)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 支持RxJava
                .build();
        Service service = retrofit.create(Service.class);
        Call<ResponseBody> responseBodyCall = service.get();
        ResponseBody body = responseBodyCall.execute().body();
        Document document = Jsoup.parse(new String(body.bytes(), "UTF8"));
        int breakPoint = 0;
    }
}


