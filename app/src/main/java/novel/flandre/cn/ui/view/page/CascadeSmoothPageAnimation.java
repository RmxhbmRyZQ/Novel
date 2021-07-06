package novel.flandre.cn.ui.view.page;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;
import novel.flandre.cn.bean.data.novel.NovelText;

import java.util.ArrayList;

/**
 * 层叠翻页
 *
 * 2020.6.24
 */
public class CascadeSmoothPageAnimation extends BasePageAnimation {
    static final int NORMAL_DRAW = 0;  // 普通作画
    static final int SMOOTH_LAST_DRAW = 1;  // 上一个页面作画
    static final int SMOOTH_NEXT_DRAW = 2;  // 下一个页面作画

    private static final long POST_DELAY = 20;  // post延迟时间(毫秒)
    private static final int DISTANCE = 8;  // 每次移动的距离(width/this)

    int drawMode = NORMAL_DRAW;  // 作画的模式

    int left;  // bitmap的x坐标
    private ArrayList<Bitmap> mBitmap = new ArrayList<>();
    private ArrayList<Canvas> mCanvas = new ArrayList<>();
    private int x, y;

    public CascadeSmoothPageAnimation(PageView view) {
        super(view);
    }

    @Override
    public void onCycle() {
        for (Bitmap bitmap:mBitmap){
            bitmap.recycle();
        }
    }

    @Override
    public void onLoad(int width, int height) {
        super.onLoad(width, height);
        for (int i = 0; i < 3; i++) {
            mBitmap.add(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
            mCanvas.add(new Canvas(mBitmap.get(i)));
            mCanvas.get(i).drawColor(mPageView.color);
        }
        // 如果已经设置了drawText就可以直接开始作画
        if (mPageView.drawText != null) {
            drawBitmap();
        }
    }

    /**
     * 给三张bitmap都进行作画
     */
    private void drawBitmap() {
        NovelText[] drawText = mPageView.drawText;
        int now = mPageView.now;
        if (drawText == null) return;
        for (Canvas canvas : mCanvas) canvas.drawColor(mPageView.color);
        if (now > 0)
            mPageView.drawText(mCanvas.get(0), now - 1, getPosition(now - 1));
        mPageView.drawText(mCanvas.get(1), now);
        if (now < mPageView.listPosition[6] - 1)
            mPageView.drawText(mCanvas.get(2), now + 1, getPosition(now + 1));
    }

    /**
     * @param now 页面位置
     * @return 对应的文本位置
     */
    private int getPosition(int now) {
        int position = 0;
        for (int p : mPageView.listPosition) {
            if (now >= p) position++;
            else break;
        }
        return position;
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        if (!mPageView.pageEnable) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                y = (int) event.getY();
            case MotionEvent.ACTION_MOVE:
                // 页面随着手的变化而变化
                int x = (int) event.getX();
                smooth(x);
                mPageView.postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                // 如果时点击时, 设置好数据
                if (Math.abs(event.getX() - this.x) < width / 10) {
                    // 如果使用全屏点击下一页或者点的位置在右边, 使用下一页动画
                    if (mPageView.alwaysNext || this.x > width / 2) {
                        left = 0;
                        drawMode = SMOOTH_NEXT_DRAW;
                    } else {
                        left = -width;
                        drawMode = SMOOTH_LAST_DRAW;
                    }
                }
                mPageView.action(event, this.x);
                break;
        }
        return true;
    }

    /**
     * 仅对一章bitmap进行作画
     *
     * @param mode true是下一页, false是上一页
     */
    private void changeBitmap(boolean mode) {
        int now = mPageView.now;
        if (mode) {
            Bitmap bitmap = mBitmap.get(0);
            Canvas canvas = mCanvas.get(0);
            mBitmap.remove(0);
            mCanvas.remove(0);
            mBitmap.add(bitmap);
            mCanvas.add(canvas);
            canvas.drawColor(mPageView.color);
            if (now < mPageView.listPosition[6] - 1) mPageView.drawText(canvas, now + 1, getPosition(now + 1));
        } else {
            Bitmap bitmap = mBitmap.get(2);
            Canvas canvas = mCanvas.get(2);
            mBitmap.remove(2);
            mCanvas.remove(2);
            mBitmap.add(0, bitmap);
            mCanvas.add(0, canvas);
            canvas.drawColor(mPageView.color);
            if (now > 0) mPageView.drawText(canvas, now - 1, getPosition(now - 1));
        }
    }

    /**
     * 计算x坐标的位置, 并设置页面的移动方式
     *
     * @param x 当前手指的坐标
     */
    private void smooth(int x){
        if (x - this.x > 0) {
            left = Math.abs(x - this.x) - width;
            drawMode = SMOOTH_LAST_DRAW;
        } else {
            left = 0 - Math.abs(x - this.x);
            drawMode = SMOOTH_NEXT_DRAW;
        }
    }

    @Override
    public void onPaintChange() {
        if (mPageView.getLoad()) {
            drawBitmap();
            mPageView.postInvalidate();
        }
    }

    public ArrayList<Bitmap> getBitmap() {
        return mBitmap;
    }

    @Override
    public void onDraw(Canvas canvas) {
        switch (drawMode) {
            case NORMAL_DRAW:
                canvas.drawBitmap(getBitmap().get(1), 0, 0, null);
                break;
            case SMOOTH_LAST_DRAW:
                canvas.drawBitmap(getBitmap().get(1), 0, 0, null);
                canvas.drawBitmap(getBitmap().get(0), left, 0, null);
//                canvas.drawLine(left + width, 0, left + width, height, getPaint());
                break;
            case SMOOTH_NEXT_DRAW:
                canvas.drawBitmap(getBitmap().get(2), 0, 0, null);
                canvas.drawBitmap(getBitmap().get(1), left, 0, null);
//                canvas.drawLine(left + width, 0, left + width, height, getPaint());
                break;
        }
    }

    @Override
    public void onUpdateText() {
        switch (mPageView.mode) {
            case PageView.NEXT:
                left = 0;
                drawMode = SMOOTH_NEXT_DRAW;
                nextPage();
                break;
            case PageView.LAST:
                left = -width;
                drawMode = SMOOTH_LAST_DRAW;
                lastPage();
                break;
            case PageView.REDIRECT:
                drawBitmap();
                mPageView.postInvalidate();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPageView.pageEnable) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    left = -width;
                    drawMode = SMOOTH_LAST_DRAW;
                    lastPage();
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    left = 0;
                    drawMode = SMOOTH_NEXT_DRAW;
                    nextPage();
                    return true;
            }
        }
        return true;
    }

    @Override
    public void nextPage() {
        // 开启翻页动画, 不给用户移动, 直到动画结束
        mPageView.pageEnable = false;
        mPageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveNext();
            }
        }, POST_DELAY);
    }

    private void moveNext() {
        if (mPageView.now < mPageView.listPosition[6] - 1) {
            // 如果不是最后一页, 进行下一页操作
            // 当left<-width时表示页面已经翻走了, 可以结束动画进行收尾工作
            if (left > -width) {
                left -= width / DISTANCE;
                mPageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveNext();
                    }
                }, POST_DELAY);
                // 最后的改变交给NORMAL_DRAW就可以了
                if (left <= -width) return;
            } else {
                drawMode = NORMAL_DRAW;
                finishNext();
            }
        } else {
            // 如果时最后一页, 把页面翻回来
            if (left < 0) {
                left += width / DISTANCE;
                mPageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveNext();
                    }
                }, POST_DELAY);
                if (left >= 0) return;
            } else {
                drawMode = NORMAL_DRAW;
                finishNext();
            }
        }
        mPageView.postInvalidate();
    }

    private void finishNext() {
        // 设置为用户可移动
        mPageView.pageEnable = true;
        // 未进行页面处理时, 是否为最后一页, 如果是最后一页, 就不需要改变bitmap
        boolean change = mPageView.now >= mPageView.listPosition[6] - 1;
        mPageView.next();
        if (!mPageView.pageEnable || change) return;
        changeBitmap(true);
    }

    @Override
    public void lastPage() {
        // 进行翻页动画
        mPageView.pageEnable = false;
        mPageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveLast();
            }
        }, POST_DELAY);
    }

    private void moveLast() {
        // 不是第一页就翻到下一页, 是第一页就退回来
        if (mPageView.now > 0) {
            if (left < 0) {
                left += width / DISTANCE;
                mPageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveLast();
                    }
                }, POST_DELAY);
                if (left >= 0) return;
            } else {
                drawMode = NORMAL_DRAW;
                finishLast();
            }
        } else {
            if (left > -width) {
                left -= width / DISTANCE;
                mPageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveLast();
                    }
                }, POST_DELAY);
                if (left <= -width) return;
            } else {
                drawMode = NORMAL_DRAW;
                finishLast();
            }
        }
        mPageView.postInvalidate();
    }

    private void finishLast() {
        mPageView.pageEnable = true;
        // 未翻页前是否是第一页, 是第一页就没必要changeBitmap
        boolean change = mPageView.now <= 0;
        mPageView.last();
        if (!mPageView.pageEnable || change) return;
        changeBitmap(false);
    }
}
