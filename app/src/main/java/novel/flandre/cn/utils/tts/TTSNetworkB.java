package novel.flandre.cn.utils.tts;

import okhttp3.OkHttp;

/**
 * 调用百度的API进行TTS
 */
public class TTSNetworkB implements TTSNetwork {
    private OkHttp mCrawler;

    public TTSNetworkB(){

    }

    @Override
    public void tts(String text, int voice, int speed, int tone, int volume, int extra, OnFinish onFinish) {

    }

    @Override
    public String getSupporter() {
        return null;
    }
}
