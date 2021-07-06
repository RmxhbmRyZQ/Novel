package novel.flandre.cn.ui.view.page;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * 最后还是逃不过继承转组合的命运
 *
 * 2020.6.24
 */
public interface PageAnimation {
    /**
     * 控制页面的点击事件
     */
    public void dispatchTouch(MotionEvent event);

    /**
     * 控制页面的点击事件
     */
    public boolean onTouch(MotionEvent event);

    /**
     * 当画笔参数改变时
     */
    public void onPaintChange();

    /**
     * 初始化
     */
    public void onLoad(int width, int height);

    /**
     * 绘制图画
     */
    public void onDraw(Canvas canvas);

    /**
     * 文本更新时的页面操作
     */
    public void onUpdateText();

    /**
     * 物理硬件被按下时的操作
     */
    public boolean onKeyDown(int keyCode, KeyEvent event);

    /**
     * 向下翻页
     */
    public void nextPage();

    /**
     * 向上翻页
     */
    public void lastPage();

    /**
     * 释放对象的资源
     */
    public void onCycle();
}
