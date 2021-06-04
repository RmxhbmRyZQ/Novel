package flandre.cn.novel.net;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.*;

public interface NovelService {
    @Headers({"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36",
            "Connection: Keep-Alive"})
    @GET
    public Observable<ResponseBody> get(@Url String url, @Header("Referer") String referer, @Header("Origin") String origin);

    @Headers({"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36",
            "Connection: Keep-Alive"})
    @POST
    public Observable<ResponseBody> post(@Url String url, @Body String body, @Header("Referer") String referer, @Header("Origin") String origin);
}
