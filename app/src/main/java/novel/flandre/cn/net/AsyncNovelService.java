package novel.flandre.cn.net;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.*;

public interface AsyncNovelService {
    @Headers("Connection: Keep-Alive")
    @GET
    public Observable<ResponseBody> get(@Url String url, @Header("Referer") String referer, @Header("Origin") String origin,
                                        @Header("User-Agent") String userAgent);

    @Headers("Connection: Keep-Alive")
    @POST
    public Observable<ResponseBody> post(@Url String url, @Body String body, @Header("Referer") String referer,
                                         @Header("Origin") String origin, @Header("User-Agent") String userAgent);
}
