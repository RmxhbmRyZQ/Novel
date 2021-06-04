package flandre.cn.novel.bean.data.music;

/**
 * 音乐保存信息
 */
public class MusicSaveData {
    private String saveList;  // 播放列表
    private long songId;  // 当前歌曲id
    private int playStatus;  // 播放顺序
    private int current;  // 播放位置
    private boolean isShowNotification;  // 是否显示通知

    public MusicSaveData(String saveList, long songId, int playStatus, int current, boolean isShowNotification) {
        this.saveList = saveList;
        this.songId = songId;
        this.playStatus = playStatus;
        this.current = current;
        this.isShowNotification = isShowNotification;
    }

    public boolean isShowNotification() {
        return isShowNotification;
    }

    public int getCurrent(){
        return current;
    }

    public String getSaveList() {
        return saveList;
    }

    public long getSongId() {
        return songId;
    }

    public int getPlayStatus() {
        return playStatus;
    }
}
