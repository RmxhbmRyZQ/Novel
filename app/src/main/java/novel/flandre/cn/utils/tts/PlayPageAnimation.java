package novel.flandre.cn.utils.tts;

import android.view.KeyEvent;
import android.view.MotionEvent;
import novel.flandre.cn.ui.view.page.NormalPageAnimation;
import novel.flandre.cn.ui.view.page.PageView;

public class PlayPageAnimation extends NormalPageAnimation {
    private CallBack mCallBack;
    private long mTime;
    private int x, y;

    public PlayPageAnimation(PageView view, CallBack callBack) {
        super(view);
        mCallBack = callBack;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        // 加载时屏幕不能滑动
        // 上下滑动时会改变当前读的为止
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTime = System.currentTimeMillis();
                y = (int) event.getY();
                x = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                int tx = (int) event.getX();
                if (System.currentTimeMillis() - mTime < 500 && Math.abs(x - tx) <
                        getPageView().getWidth() / 20 && Math.abs(y - event.getY()) < getPageView().getWidth() / 20)
                    mCallBack.onClick();
//                else {
//                    if (Math.abs(x - tx) > getPageView().getWidth() / 10) {
//                        if (event.getX() - x > getPageView().getWidth() / 10) {
//                            lastPage();
//                        } else if (x - event.getX() > getPageView().getWidth() / 10) {
//                            nextPage();
//                        }
//                    }
//                }
                break;
        }
        return true;
    }

    public void sNextPage(){
        super.nextPage();
    }

    @Override
    public void nextPage() {
        super.nextPage();
        if (mCallBack != null) mCallBack.next();
    }

    @Override
    public void lastPage() {
        super.lastPage();
        if (mCallBack != null) mCallBack.last();
    }

    public interface CallBack {
        public void onClick();

        public void next();

        public void last();
    }
}
