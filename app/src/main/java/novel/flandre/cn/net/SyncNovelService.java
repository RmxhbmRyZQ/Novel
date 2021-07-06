package novel.flandre.cn.net;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface SyncNovelService {
    @Headers("Connection: Keep-Alive")
    @GET
    public Call<ResponseBody> get(@Url String url, @Header("Referer") String referer, @Header("Origin") String origin,
                                        @Header("User-Agent") String userAgent);

    @Headers("Connection: Keep-Alive")
    @POST
    public Call<ResponseBody> post(@Url String url, @Body String body, @Header("Referer") String referer,
                                         @Header("Origin") String origin, @Header("User-Agent") String userAgent);
}
