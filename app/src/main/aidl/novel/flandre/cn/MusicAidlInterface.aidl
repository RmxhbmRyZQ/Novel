// MusicAidlInterface.aidl
package novel.flandre.cn;

// Declare any non-default types here with import statements
import novel.flandre.cn.bean.data.music.MusicInfo;

interface MusicAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, in String aString);

    boolean playQueueIsEmpty();

    MusicInfo getPlayInfo();

    void setPlayInfo(in Map infos);

    void addPlayInfo(long id, in MusicInfo info);

    long[] setPlayQueue(in long[] queue);

    boolean addPlayQueue(long id);

    void deletePlayQueue(long id);

    void deleteAllPlayQueue();

    long[] getPlayQueue();

    int getPlayQueueSize();

    void setPlayOrder(int status);

    int getPlayOrder();

    boolean isPlaying();

    void playTarget(long id);

    void play();

    void pause();

    int getCurrentPosition();

    void setCurrentPosition(int pos);

    int getPlayDuration();

    void nextMusic();

    void lastMusic();

    void saveData();
}
