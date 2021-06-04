package flandre.cn.novel.utils.tts;

import android.media.MediaPlayer;
import flandre.cn.novel.utils.database.SharedTools;

import java.io.IOException;

import static flandre.cn.novel.utils.tts.TTSController.COUNT;

/**
 * 播放语音的类
 */
public class Speaker implements TTSNetwork.OnFinish {

    private int mVoice;  // 说话的声音
    private int mSpeed;  // 说话的速度
    private int mTone;  // 说话的音调
    private int mVolume;  // 说话的音量
    private TTSNetwork mTtsNetwork;  // 文本转语音的实现
    private TextMediaPlayer mMediaPlayer;
    private int[] mTry = new int[COUNT];  // 记录重连次数
    private String[] mUrls = new String[COUNT];
    private Status[] mAvailable = new Status[COUNT];  // 是否可用
    private int mNow = -1;
    private CallBack mCallBack;

    public String getSupport() {
        return mTtsNetwork.getSupporter();
    }

    public void release() {
        mMediaPlayer.release();
    }

    private enum Status {
        UNAVAILABLE, URL_OK, PREPARE_FAIL, PREPARE_OK, PLAY_NEXT
    }

    public Speaker(CallBack callBack) {
        mCallBack = callBack;
        mTtsNetwork = new TTSNetworkC();
        mMediaPlayer = new TextMediaPlayer();
        SharedTools tools = SharedTools.getSharedTools();
        mVoice = tools.getAttributes("Voice", 0);
        mSpeed = tools.getAttributes("Speed", 5);
        mTone = tools.getAttributes("Tone", 5);
        mVolume = tools.getAttributes("Volume", 5);
    }

    /**
     * 装载文本
     */
    public synchronized void prepare(String text) {
        mNow = ++mNow % COUNT;
        if (text != null) prepare(text, mNow);
        else mAvailable[mNow] = Status.PLAY_NEXT;
    }

    private void prepare(String text, int extra) {
        if (mNow == -1) return;
        mTry[mNow]++;
        mTtsNetwork.tts(text, mVoice, mSpeed, mTone, mVolume, extra, this);
    }

    /**
     * 消除状态
     */
    public void reset() {
        mMediaPlayer.mPosition = 0;
        mNow = -1;
        for (int i = 0; i < COUNT; i++) {
            mAvailable[i] = Status.UNAVAILABLE;
        }
    }

    /**
     * 播放下一句话
     */
    public void next() {
        mMediaPlayer.next();
    }

    /**
     * 开始播放
     */
    public void speak() {
        mMediaPlayer.play();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        mMediaPlayer.pause();
    }

    /**
     * 停止播放
     */
    public void stop() {
        mMediaPlayer.pause();
        reset();
    }

    @Override
    public void onFinish(String url, int extra) {
        mTry[extra] = 0;
        mUrls[extra] = url;
        mAvailable[extra] = Status.URL_OK;
        for (int i = 0; i < 3; i++) {
            try {
                mMediaPlayer.prepare(extra);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mAvailable[extra] = Status.PREPARE_FAIL;
    }

    @Override
    public void onFail(String text, int extra) {
        if (mTry[extra] > 4) return;
        prepare(text, extra);
    }

    public void setVoice(int mVoice) {
        this.mVoice = mVoice;
        SharedTools.getSharedTools().setAttributes("Voice", mVoice);
    }

    public void setSpeed(int mSpeed) {
        this.mSpeed = mSpeed;
        SharedTools.getSharedTools().setAttributes("Speed", mSpeed);
    }

    public void setTone(int mTone) {
        this.mTone = mTone;
        SharedTools.getSharedTools().setAttributes("Tone", mTone);
    }

    public void setVolume(int mVolume) {
        this.mVolume = mVolume;
        SharedTools.getSharedTools().setAttributes("Volume", mVolume);
    }

    class TextMediaPlayer implements MediaPlayer.OnCompletionListener {
        private int mPosition = 0;
        MediaPlayer[] mPlayer = new MediaPlayer[COUNT];

        TextMediaPlayer() {

        }

        private void play() {
            switch (mAvailable[mPosition]){
                case PREPARE_OK:
                    mPlayer[mPosition].start();
                    break;
                case PREPARE_FAIL:
                    if (mCallBack != null) mCallBack.onPrepareFail();
                    break;
                case PLAY_NEXT:
                    onCompletion(null);
                    break;
            }
        }

        private void next() {
            pause();
            mPosition = ++mPosition % COUNT;
            play();
        }

        private void pause() {
            if (mPlayer[mPosition] != null && mPlayer[mPosition].isPlaying())
                mPlayer[mPosition].pause();
        }

        private void prepare(final int position) throws IOException {
            if (mAvailable[position] == Status.URL_OK) {
                if (mPlayer[position] == null)
                    mPlayer[position] = new MediaPlayer();
                MediaPlayer player = mPlayer[position];
                player.reset();
                player.setOnCompletionListener(this);
                player.setDataSource(mUrls[position]);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mAvailable[position] = Status.PREPARE_OK;
                        if (position == mPosition) {
                            mp.start();
                        }else if (mAvailable[mPosition] == Status.PLAY_NEXT){
                            onCompletion(null);
                        }
                    }
                });
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mCallBack != null) mCallBack.onComplete();
            mAvailable[mPosition] = Status.UNAVAILABLE;
            mPosition = ++mPosition % COUNT;
            play();
        }

        public void release() {
            for (MediaPlayer player : mPlayer) {
                if (player == null) continue;
                if (player.isPlaying()) player.stop();
                player.reset();
                player.release();
            }
        }
    }

    public interface CallBack {
        /**
         * 当当前的语音播放完时回调
         */
        public void onComplete();

        public void onPrepareFail();
    }
}
