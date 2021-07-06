package novel.flandre.cn.bean.data.music;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 音乐信息
 */
public class MusicInfo implements Parcelable {
    private long songId = -1;
    private int duration;
    private String data;
    private String name;
    private String singer;
    private String sort;
    private boolean isPlaying = false;

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public MusicInfo(){

    }

    public MusicInfo(Parcel in) {
        songId = in.readLong();
        duration = in.readInt();
        data = in.readString();
        name = in.readString();
        singer = in.readString();
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel in) {
            return new MusicInfo(in);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(songId);
        dest.writeInt(duration);
        dest.writeString(data);
        dest.writeString(name);
        dest.writeString(singer);
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }
}
