package flandre.cn.novel.ui.view.page;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.Scroller;
import flandre.cn.novel.bean.data.novel.NovelText;

import java.util.ArrayList;
import java.util.List;

/**
 * 滚动动画
 * 2020.6.24
 */
public class ScrollPageAnimation extends NormalPageAnimation implements Runnable {
    private static final int SEPARATOR = 10;
    private static final int MOVE_PAGE_SEPARATOR = 1;

    private int nowPos = 0;  // 第一行的位置，即使这一行没有文字
    private int offset = 0;  // 第一行的偏移
    private boolean isShowNotice = false;  // 是否显示通知
    private int yp;  // 当前手指的y
    private int pos;
    private int x, y;
    private boolean isFirst = true;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private VelocityTracker velocityTracker;
    private int velocityY = 0;
    private Scroller scroller;
    private int preY;
    private long time;

    public int getOffset() {
        return offset;
    }

    public ScrollPageAnimation(PageView view) {
        super(view);
        scroller = new Scroller(mPageView.getContext(), null, false);
    }

    @Override
    public void onLoad(int width, int height) {
        super.onLoad(width, height);
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    public void onPaintChange() {
        if (mPageView.getLoad()) {
            drawScroll(mCanvas);
            mPageView.postInvalidate();
        }
    }

    @Override
    public void onUpdateText() {
        if (mPageView.mode == PageView.REDIRECT) drawScroll(mCanvas);
        super.onUpdateText();
    }

    // velocityTracker 的使用
    @Override
    public void dispatchTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (velocityTracker == null)
                    velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 计算当前速度， 1000表示每秒像素数等
                velocityTracker.computeCurrentVelocity(1000, 80000);
                // 获取横向速度
                velocityY = (int) velocityTracker.getYVelocity();
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
        }
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        if (!mPageView.pageEnable) return true;
        stopScroll();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                y = (int) event.getY();
                yp = (int) event.getY();
                time = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                // 处理滚动
                int y = (int) event.getY();
                scroll(y - yp);
                this.yp = y;
                drawScroll(mCanvas);
                mPageView.postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                int r = (int) (event.getY() - this.y);
                long now = System.currentTimeMillis();
                // 先处理点击事件
                if (this.y < height / 2 + height / 10 && this.y > height / 2 - height / 10
                        && Math.abs(r) < height / 20 && now - time < 300) {
                    mPageView.getPageTurn().onShowAction();
                    return true;
                }
//                if (Math.abs(r) < height / 20 && Math.abs(x - event.getX()) < width / 20) {
//                    if (this.y > height / 2 + height / 10)
//                        nextPage();
//                    else if (this.y < height / 2 - height / 10)
//                        if (mPageView.alwaysNext) nextPage();
//                        else lastPage();
//                    else mPageView.getPageTurn().onShowAction();
//                    return true;
//                }
                // 不是点击事件时自动滚动
                auto();
                break;
        }
        return true;
    }

    @Override
    public void run() {
        if (!scroller.computeScrollOffset()) {
            return;
        }
        int y = scroller.getCurrY();
        int len = preY - y;
        if (len != 0)
            preY = y;
        if (len == 0) return;
        scroll(-len);
        drawScroll(mCanvas);
        mPageView.postInvalidate();
        mPageView.postDelayed(this, SEPARATOR);
    }

    private void auto() {
        preY = 0;
        scroller.fling(0, 0, 0, velocityY, 0, 0, -5000, 5000);  // 使用摩擦滑动
        startScroll();
    }

    private void startScroll() {
        mPageView.postDelayed(this, SEPARATOR);
    }

    private void stopScroll() {
        mPageView.removeCallbacks(this);
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
    }

    public void scrollY(int addY) {
        if (isShowNotice) return;
        scroll(addY);
        drawScroll(mCanvas);
        mPageView.postInvalidate();
    }

    /**
     * 滚动文本
     *
     * @param addY 文本位置增量
     */
    private void scroll(int addY) {
        offset += addY;
        if (offset > 0) {
            // 向上提一行, 把offset变成负的, 让他自己滚下来
            int t = (offset / mPageView.getRowHeight() + 1);
            offset -= t * mPageView.getRowHeight();
            nowPos -= t;
            if (nowPos < 0) {
                // 第一页的时候不能滚动
                if (mPageView.now == 0) {
                    nowPos = 0;
                    offset = 0;
                    // 防止疯狂发信息
                    if (isShowNotice) {
                        stopScroll();
                        return;
                    }
                    isShowNotice = true;
                    mPageView.last();
                    return;
                } else nowPos = mPageView.pageCount + nowPos;
                mPageView.last();
            }
            isShowNotice = false;
        } else if (mPageView.now + 1 == mPageView.listPosition[6]) {
            // 最后一页时停止滚动
            nowPos = 0;
            offset = 0;
            if (isShowNotice) {
                stopScroll();
                return;
            }
            isShowNotice = true;
            mPageView.next();
        } else if (Math.abs(offset) >= mPageView.getRowHeight()) {
            // 当滚动了一行时, 向下提一行
            nowPos -= offset / mPageView.getRowHeight();
            if (nowPos >= mPageView.pageCount) {
                nowPos -= mPageView.pageCount;
                mPageView.next();
            }
            offset %= mPageView.getRowHeight();
            isShowNotice = false;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isFirst) {
            if (mPageView.drawText != null)
                drawScroll(mCanvas);
            else mCanvas.drawColor(mPageView.color);
            isFirst = false;
        }
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    private void drawScroll(Canvas canvas) {
        int position = mPageView.position;
        NovelText[] drawText = mPageView.drawText;
        ArrayList<ArrayList<String>> textPosition = mPageView.textPosition;
        int now = mPageView.now;
        Integer[] listPosition = mPageView.listPosition;

        canvas.drawColor(mPageView.color);
        // 文字没加载好就直接退出
        if (mPageView.drawText == null) return;
        int i = 1;  // 当前文字的行数
        // 写当前页的内容
        String text = drawText[position].getText();
        List<String> strings = textPosition.get(now);
        // nowPos 居然会出现 -1？懒得看哪里出问题，直接纠正
        if (nowPos < 0) nowPos = 0;
        for (int j = nowPos + 1; j <= mPageView.pageCount; j++) {
            // 当当前行没有文字时，空转完这一页
            if (j > strings.size()) {
                i++;
                continue;
            }
            String[] mark = strings.get(j - 1).split(":");
            canvas.drawText(text, Integer.valueOf(mark[0]), Integer.valueOf(mark[1]), mPageView.getLeftPadding(),
                    mPageView.getCowPosition(i) + offset, mPageView.getPaint());
            i++;
        }
        // 如果不是最后一页, 写下一页的内容
        if (now + 1 != listPosition[position] || position != 6) {
            int pageCount = offset != 0 ? mPageView.pageCount + 1 : mPageView.pageCount;
            pageCount = mPageView.heightRest > mPageView.getRowHeight() / 1.5 ? pageCount + 1 : pageCount;
            if (now + 1 == listPosition[position]) text = drawText[position + 1].getText();
            strings = textPosition.get(now + 1);
            for (int j = 1; i <= pageCount; i++) {
                if (j > strings.size()) continue;
                String[] mark = strings.get(j - 1).split(":");
                canvas.drawText(text, Integer.valueOf(mark[0]), Integer.valueOf(mark[1]), mPageView.getLeftPadding(),
                        mPageView.getCowPosition(i) + offset, mPageView.getPaint());
                j++;
            }
        }
        mPageView.drawChapter(canvas, now, position);
    }

    @Override
    public void nextPage() {
//        for (int i = 1; i <= mPageView.pageCount / MOVE_PAGE_SEPARATOR; i++) {
//            mPageView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    pos++;
//                    scroll(-mPageView.getRowHeight() * MOVE_PAGE_SEPARATOR);
//                    drawScroll(mCanvas);
//                    mPageView.postInvalidate();
//                    if (pos == mPageView.pageCount / MOVE_PAGE_SEPARATOR) {
//                        mPageView.next();
//                    }
//                }
//            }, i * SEPARATOR);
//        }
        int move = mPageView.getRowHeight() * mPageView.pageCount;
        scroll(-move);
        drawScroll(mCanvas);
        mPageView.postInvalidate();
//        mPageView.next();
    }

    @Override
    public void lastPage() {
//        for (int i = 1; i <= mPageView.pageCount / MOVE_PAGE_SEPARATOR; i++) {
//            mPageView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    pos++;
//                    scroll(mPageView.getRowHeight() * MOVE_PAGE_SEPARATOR);
//                    drawScroll(mCanvas);
//                    mPageView.postInvalidate();
//                    if (pos == mPageView.pageCount / MOVE_PAGE_SEPARATOR) {
//                        mPageView.last();
//                    }
//                }
//            }, i * SEPARATOR);
//        }
        int move = mPageView.getRowHeight() * mPageView.pageCount;
        scroll(move);
        drawScroll(mCanvas);
        mPageView.postInvalidate();
//        mPageView.last();
    }

    @Override
    public void onCycle() {
        super.onCycle();
        mBitmap.recycle();
    }
}
