package novel.flandre.cn.utils.tts;

import android.content.Context;
import android.util.Log;
import novel.flandre.cn.bean.data.novel.NovelText;
import novel.flandre.cn.ui.activity.TextActivity;
import novel.flandre.cn.ui.view.page.PageView;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.ArrayList;

/**
 * TTS的控制台
 */
public class TTSController implements Speaker.CallBack, PlayPageAnimation.CallBack, TTSPopupFragment.CallBack {
    public static final int COUNT = 3;  // 缓存大小

    private PageView mPageView;
    private Speaker mSpeaker;
    private PlayPageAnimation mAnimation;
    private TTSPopupFragment mTtsPopupFragment;
    private Context mContext;
    private String extra;
    private int s = 0, p = 0;

    public TTSController(PageView mPageView, Context context) {
        mContext = context;
        this.mPageView = mPageView;
        mSpeaker = new Speaker(this);
        mTtsPopupFragment = TTSPopupFragment.newInstance(mSpeaker.getSupport(), this);
    }

    private String getText(int i) {
        ArrayList<String> arrayList = mPageView.getTextPosition().get(i);
        NovelText[] drawText = mPageView.getDrawText();
        String text = drawText[getPosition(i)].getText();
        String tmp;
        int s, e;
        s = Integer.parseInt(arrayList.get(0).split(":")[0]);
        e = Integer.parseInt(arrayList.get(arrayList.size() - 1).split(":")[1]);
        tmp = text.substring(s, e);
        return tmp;
    }

    public void start() {
        mAnimation = new PlayPageAnimation(mPageView, this);
        mPageView.setPageAnimation(mAnimation);
        prepareAll();
    }

    public void prepareAll() {
//        extra = null;
//        String tmp = decorationGetText(0);
//        if (tmp == null) mSpeaker.speak();
        for (int i = 0; i < COUNT; i++) {
            if (mPageView.getListPosition()[6] > mPageView.getNow() + i) {
                mSpeaker.prepare(decorationGetText(i));
            }
        }
//        mSpeaker.prepare(tmp);
    }

    private String decorationGetText(int i) {
        String tmp;
        int pos = 0, sp;
        sp = 0;
        tmp = getText(mPageView.getNow() + i);
        if (extra != null) {
            tmp += extra;
            extra = null;
        }
        while (pos < 15 && sp < tmp.length())
            if (!contains("\r\n！。‘’“”？…  ", tmp.charAt(sp++))) pos++;
        if (pos < 15) {
            extra = tmp;
            return null;
        }
        return tmp;
    }

    private boolean contains(String src, char c) {
        for (int i = 0; i < src.length(); i++) {
            if (src.charAt(i) == c) return true;
        }
        return false;
    }

    public void pause() {
        mSpeaker.pause();
        mSpeaker.reset();
    }

    public void stop() {
        mSpeaker.pause();
        mSpeaker.release();
    }

    @Override
    public void last() {
        refresh();
//        if (mPageView.isPageEnable() && mPageView.getNow() > 0) {
//            mSpeaker.prepare(getText(mPageView.getNow() - COUNT + 1));
//        }
    }

    @Override
    public void next() {
        refresh();
    }

    @Override
    public void onComplete() {
        mAnimation.sNextPage();
        if (mPageView.isPageEnable() && mPageView.getListPosition()[6] > mPageView.getNow() + COUNT - 1) {
            mSpeaker.prepare(decorationGetText(COUNT - 1));
        }
    }

    private int getPosition(int now) {
        int position = 0;
        for (int p : mPageView.getListPosition()) {
            if (now >= p) position++;
            else break;
        }
        return position;
    }

    @Override
    public void onPrepareFail() {
//        onComplete();
        Log.e("Fail", "Fail");
    }

    @Override
    public void onClick() {
        mTtsPopupFragment.show(((TextActivity) mContext).getSupportFragmentManager(), "TTSPopupFragment");
    }

    public void refresh() {
        mSpeaker.pause();
        mSpeaker.reset();
        prepareAll();
    }

    @Override
    public void setVoice(int voice) {
        mSpeaker.setVoice(voice);
        refresh();
    }

    @Override
    public void setTone(int tone) {
        mSpeaker.setTone(tone);
        refresh();
    }

    @Override
    public void setSpeed(int speed) {
        mSpeaker.setSpeed(speed);
        refresh();
    }

    @Override
    public void onExit() {
        pause();
        mPageView.setPageAnimation(NovelConfigureManager.getPageAnimation(mPageView));
        mTtsPopupFragment.dismiss();
    }
}
