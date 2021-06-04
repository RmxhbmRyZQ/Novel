package flandre.cn.novel.ui.activity;

public interface MusicListener {
    /**
     * 播放音乐时调用
     */
    public void onPlayMusic();

    /**
     * 音乐停止播放时调用
     */
    public void onPauseMusic();

    /**
     * 播放下一首音乐时调用
     */
    public void onNextSong();

    /**
     * 播放上一首音乐时调用
     */
    public void onLastSong();

    /**
     * 用户拖动音乐进度条时调用
     */
    public void onProgressChange();

    /**
     * 用户清空播放列表时调用
     */
    public void onClearPlayList();
}
