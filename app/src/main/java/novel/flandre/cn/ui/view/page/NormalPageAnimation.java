package novel.flandre.cn.ui.view.page;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * 无动画翻页
 */
public class NormalPageAnimation extends BasePageAnimation {
    private int x,y;

    public NormalPageAnimation(PageView view) {
        super(view);
    }

    @Override
    public void nextPage() {
        mPageView.next();
        if (!mPageView.pageEnable) return;
        mPageView.postInvalidate();
    }

    @Override
    public void lastPage() {
        mPageView.last();
        if (!mPageView.pageEnable) return;
        mPageView.postInvalidate();
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        if (!mPageView.pageEnable) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                y = (int) event.getY();
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                mPageView.action(event, x);
                break;
        }
        return true;
    }

    @Override
    public void onPaintChange() {

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(mPageView.color);
        if (mPageView.drawText != null)
            mPageView.drawText(canvas, mPageView.now);
    }

    @Override
    public void onUpdateText() {
        switch (mPageView.mode){
            case PageView.NEXT:
                nextPage();
                break;
            case PageView.LAST:
                lastPage();
                break;
            case PageView.REDIRECT:
                mPageView.postInvalidate();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPageView.pageEnable) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    lastPage();
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    nextPage();
                    return true;
            }
        }
        return false;
    }
}
