package flandre.cn.novel.net;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface TmpService {
    @GET
    Call<ResponseBody> get(@Url String url);
}
