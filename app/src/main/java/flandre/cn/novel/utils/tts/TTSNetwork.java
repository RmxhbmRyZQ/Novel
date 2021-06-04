package flandre.cn.novel.utils.tts;

public interface TTSNetwork {
    /**
     * 把文本转换成语音
     * @param text 文本
     * @param voice 说话人
     * @param speed 速度
     * @param tone 语调
     * @param volume 音量
     */
    public void tts(String text, int voice, int speed, int tone, int volume, int extra, OnFinish onFinish);

    /**
     * 拿到技术的提供者
     */
    public String getSupporter();

    public interface OnFinish{
        /**
         * 转换成功调用的函数
         * @param url 转换后语音的网址
         */
        public void onFinish(String url, int extra);

        /**
         * 转换失败调用的函数
         */
        public void onFail(String text, int extra);
    }
}
