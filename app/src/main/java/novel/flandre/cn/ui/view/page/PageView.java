package novel.flandre.cn.ui.view.page;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import novel.flandre.cn.bean.data.novel.NovelText;
import novel.flandre.cn.utils.tools.DisplayUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static novel.flandre.cn.ui.view.page.PageViewTextManager.BufferChapterCount;

/**
 * 翻页的基类, 处理数据
 * 现在后悔了, 其实只要计算下每页的文本位置, 再自定义一个ViewGroup+TextView就好了
 * 最后再重写触摸事件. 现在只能硬着头皮写下去, 怪以前的我
 * 2020.4
 */
public class PageView extends View {
    public static final int REDIRECT = 0;  // 跳转
    public static final int NEXT = 1;  // 下一页
    public static final int LAST = 2;  // 上一页

    private int topLength;  // 头顶通知的高度
    private int marginTop;
    private int marginLeft;

    private Paint textPaint;  // 写text文本的笔
    private Paint descriptionPaint;  //
    private int leftPadding;
    private int watch;  // 观看的位置
    private int length;  // 长度
    private long time;

    ArrayList<ArrayList<String>> textPosition;  // 文本的位置信息
    private PageTurn pageTurn = null;  // 页面变化时的接口
    private Context mContext;

    private boolean load = false;  // view是否在以初始化状态
    boolean pageEnable = false;  // 用户是否可以操控页面

    NovelText[] drawText;  // 当前的文本缓冲
    Integer[] listPosition = new Integer[BufferChapterCount];  // 每个文本的结尾位置
    int now;  // 当前观看的位置
    int position;  // 当前观看的文本位置
    int color = 0xffffffff;  // 背景颜色
    int pageCount;  // 一共有多少行
    private int size;  // 文字大小
    private int rowSpace;  // 行距
    private int paddingTop = 0;
    private int paddingBottom = 0;
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private int lastChapter;  // 最后章节, 在翻页前需要初始化
    int mode;  // pageEnable是false时的跳转模式
    private int width, height;

    int heightRest;

    boolean alwaysNext = false;  // 是否全屏点击下一页
    private PageAnimation pageAnimation;

    public PageView(Context context) {
        this(context, null);
        mContext = context;
        initPaint();
    }

    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initPaint();
    }

    private void initPaint() {
        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(5);  // 画笔的粗细
        textPaint.setTextSize(size);
        textPaint.setTextAlign(Paint.Align.LEFT);

        descriptionPaint = new Paint();
        descriptionPaint.setStyle(Paint.Style.FILL);
        descriptionPaint.setStrokeWidth(2);
        descriptionPaint.setTextSize(DisplayUtil.sp2px(mContext, 14));
        descriptionPaint.setTextAlign(Paint.Align.LEFT);

        topLength = DisplayUtil.dip2px(mContext, 10);
        marginTop = DisplayUtil.dip2px(mContext, 6);
        marginLeft = DisplayUtil.dip2px(mContext, 10);
    }

    @Override
    public int getPaddingRight() {
        return paddingRight;
    }

    public Integer[] getListPosition() {
        return listPosition;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getHeightRest() {
        return heightRest;
    }

    public boolean isPageEnable() {
        return pageEnable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getDefaultSize(600, widthMeasureSpec);
        height = getDefaultSize(1000, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!load) {
            calcText();
            load = true;
            pageAnimation.onLoad(width, height);
        }
    }

    public void setAlwaysNext(boolean alwaysNext) {
        this.alwaysNext = alwaysNext;
    }

    public Paint getPaint() {
        return textPaint;
    }

    public boolean getLoad() {
        return load;
    }

    public void setLastChapter(int lastChapter) {
        this.lastChapter = lastChapter;
    }

    public int getPosition() {
        return position;
    }

    public PageAnimation getPageAnimation() {
        return pageAnimation;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setPageAnimation(PageAnimation pageAnimation) {
        if (this.pageAnimation != null) {
            this.pageAnimation.onCycle();
        }
        this.pageAnimation = pageAnimation;
        if (load) this.pageAnimation.onLoad(width, height);
    }

    /**
     * 设置间距
     */
    public void setPadding(int paddingTop, int paddingRight, int paddingBottom, int paddingLeft) {
        this.paddingTop = DisplayUtil.dip2px(mContext, paddingTop);
        this.paddingRight = DisplayUtil.dip2px(mContext, paddingRight);
        this.paddingBottom = DisplayUtil.dip2px(mContext, paddingBottom);
        this.paddingLeft = DisplayUtil.dip2px(mContext, paddingLeft);
    }

    public void setOnPageTurnListener(PageTurn pageTurnListener) {
        this.pageTurn = pageTurnListener;
    }

    /**
     * 设置文本的大小
     *
     * @param size 文本的大小, 单位sp
     */
    public void setTextSize(int size) {
        this.size = DisplayUtil.sp2px(mContext, size);
        textPaint.setTextSize(this.size);
        this.rowSpace = this.size / 2;
        if (load) calcText();
    }

    /**
     * 翻页的处理
     *
     * @param event 事件
     * @param tx    按下的位置
     */
    protected void action(MotionEvent event, int tx) {
        int x = (int) event.getX();
        if (Math.abs(x - tx) > width / 10) {
            if (event.getX() - tx > width / 10) {
                pageAnimation.lastPage();
            } else if (tx - event.getX() > width / 10) {
                pageAnimation.nextPage();
            }
        } else {
            if (tx > width / 2 + width / 10) {
                pageAnimation.nextPage();
            } else if (tx < width / 2 - width / 10) {
                if (alwaysNext) pageAnimation.nextPage();
                else pageAnimation.lastPage();
            } else {
                pageTurn.onShowAction();
            }
        }
    }

    public PageTurn getPageTurn() {
        return pageTurn;
    }

    /**
     * 设置文字颜色
     */
    public void setTextColor(int color) {
        textPaint.setColor(color);
    }

    /**
     * 设置描述颜色
     */
    public void setDescriptionColor(int color) {
        descriptionPaint.setColor(color);
    }

    /**
     * 翻页后对观看位置, 观看时间的更新
     */
    public void flashWatch() {
        watch = Integer.valueOf(textPosition.get(now).get(0).split(":")[0]);
        long now = new Date().getTime();
        long addTime = now - time;
        time = now;
        pageTurn.onUpdateWatch(addTime, watch);
    }

    public NovelText[] getDrawText() {
        return drawText;
    }

    public int getWatch() {
        return watch;
    }

    /**
     * @return "页面的总数量:当前观看的位置"
     */
    private String calcWatch(int now, int position) {
        if (load) {
            int pageCount, page;
            if (position != 0) {
                pageCount = listPosition[position] - listPosition[position - 1];
                page = now - listPosition[position - 1] + 1;
            } else {
                pageCount = listPosition[position];
                page = now + 1;
            }
            return pageCount + " : " + page;
        } else {
            return "";
        }
    }

    public void setPageEnable(boolean pageEnable) {
        this.pageEnable = pageEnable;
    }

    public int getRowHeight() {
        return size + rowSpace;
    }

    public int getNow() {
        return now;
    }

    public ArrayList<ArrayList<String>> getTextPosition() {
        return textPosition;
    }

    /**
     * 计算文本的相关信息
     */
    private void calcText() {
        if (drawText == null) return;
//        int a = (int) textPaint.measureText("\n");
        pageCount = (height - paddingTop - paddingBottom + rowSpace) / (size + rowSpace);  // 页面的行数
        heightRest = (height - paddingTop - paddingBottom + rowSpace) % (size + rowSpace);  // 剩余的高度
        leftPadding = width;
        int supposeWidth = width - paddingLeft - paddingRight;
        int end, start, middle, i, j, position, width, leave, add, rest;
        // 计算出来的文本位置
        // 里面的每一个list代表每一页的文本的信息, 里面的list存放每一行的信息: "start:end"
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        for (i = 0; i < BufferChapterCount && drawText[i] != null; i++) {
            if (drawText[i].getText().equals("")) drawText[i].setText("空章节");
            String text = drawText[i].getText();
            end = 0;
            start = 0;
            middle = 0;
            for (; end != text.length(); ) {
                ArrayList<String> strings = new ArrayList<>();
                for (j = 0; j < pageCount & end != text.length(); j++) {
                    end += supposeWidth / size;
                    position = text.indexOf('\n', start + 1) + 1;
                    width = 0;  // 拿到出现换行的位置
                    position = position != 0 ? position : text.length();
                    while (true) {
                        // 遇到换行符, 直接换行
                        if (position <= end) {
                            end = position;
                            break;
                        }
                        width += (int) textPaint.measureText(text, middle, end);  // 计算text在屏幕占用的宽度
                        leave = supposeWidth - width;
                        // 当宽度不足时换行
                        add = leave / size;
                        if (add == 0) {
                            if (leftPadding > (rest = leave / 2)) {  // 为了让文字居中, 计算剩余的空间除以2
                                leftPadding = rest;
                            }
                            break;
                        }
                        middle = end;
                        end += add;
                    }
                    strings.add(start + ":" + end);
                    // 计算出当前观看的位置
                    if (i == this.position && start <= watch && watch <= end) now = list.size();
                    start = middle = end;
                }
                list.add(strings);
            }
            listPosition[i] = list.size();
        }
        length = i - 1;
        textPosition = list;
    }

    public void update() {
        calcText();
        if (drawText != null) postInvalidate();
    }

    public void updateText(NovelText[] novelTexts, int position) {
        boolean i = drawText == null;
        drawText = novelTexts.clone();
        this.position = position;
        // 如果view已经初始化了, 就进行计算
        if (load) {
            calcText();
            if (i) postInvalidate();
        }
        if (!pageEnable) {
            pageEnable = true;
            pageAnimation.onUpdateText();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        pageAnimation.onDraw(canvas);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (pageAnimation.onKeyDown(keyCode, event)) return true;
        return super.onKeyDown(keyCode, event);
    }

    public void setWatch(int watch) {
        this.watch = watch;
    }

    void drawText(Canvas canvas, int tp) {
        drawText(canvas, tp, position);
    }

    /**
     * 在canvas上绘制一页的文本
     *
     * @param tp       文本位置
     * @param position 章节位置
     */
    void drawText(Canvas canvas, int tp, int position) {
        if (drawText == null) return;
        String text = drawText[position].getText();
        drawChapter(canvas, tp, position);
        ArrayList<String> strings = textPosition.get(tp);
        for (int i = 1; i <= strings.size(); i++) {
            String[] mark = strings.get(i - 1).split(":");
            canvas.drawText(text, Integer.valueOf(mark[0]), Integer.valueOf(mark[1]), getLeftPadding(), getCowPosition(i), textPaint);
        }
    }

    /**
     * 绘制章节等信息
     */
    void drawChapter(Canvas canvas, int tp, int position) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0, 0, width, paddingTop, paint);
        canvas.drawRect(0, height - paddingBottom, width, height, paint);
        String chapter = drawText[position].getChapter();
        canvas.drawText(chapter, marginLeft, marginTop + topLength, descriptionPaint);
        canvas.drawText(calcWatch(tp, position), marginLeft, height - marginTop, descriptionPaint);

        String s = calcPercent();
        float v = descriptionPaint.measureText(s);
        canvas.drawText(s, width - marginLeft - v, height - marginTop, descriptionPaint);
    }

    private String calcPercent() {
        if (lastChapter == 0) return "0.00%";
        int chapter = pageTurn.getChapter() - 1;
        float c = (float) chapter / lastChapter;
        float step = (float) 1 / lastChapter;
        float pos = listPosition[position];
        float now = this.now + 1;
        if (position == 0) {
            c += step * now / pos;
        } else {
            float ppos = listPosition[position - 1];
            c += step * (now - ppos) / (pos - ppos);
        }
        float v = BigDecimal.valueOf(c * 100).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        return v + "%";
    }

    public int getLeftPadding() {
        return leftPadding + paddingLeft;
    }

    public int getCowPosition(int i) {
        return paddingTop + i * (size + rowSpace) - rowSpace;
    }

    void next() {
        now++;
        boolean t = false;  // 文本位置是否改变
        // 如果当前观看的位置达到了下一个文本, 调整文本的位置
        if (now == listPosition[position]) {
            position++;
            t = true;
        }
        // 当文本位置溢出, 重调文本位置, 设置页面不可移动
        if (position > length) {
            position--;
            mode = NEXT;
            pageEnable = t = false;
        }
        if (pageTurn != null) {
            if (pageTurn.onNextPage(position, t, pageEnable, now >= textPosition.size())) {
                Toast.makeText(mContext, "最后一页啦！", Toast.LENGTH_SHORT).show();
                now = textPosition.size() - 1;
                pageEnable = true;
                return;
            }
            if (now >= textPosition.size()) now = textPosition.size() - 1;
            flashWatch();
        }
    }

    void last() {
        now--;
        boolean t = false;
        if (position != 0 && now == listPosition[position - 1] - 1) {
            position--;
            t = true;
        }
        if (now < 0) {
            mode = LAST;
            pageEnable = t = false;
        }
        if (pageTurn != null) {
            if (pageTurn.onLastPage(position, t, pageEnable, now < 0)) {
                Toast.makeText(mContext, "第一页啦！", Toast.LENGTH_SHORT).show();
                pageEnable = true;
                now = 0;
                return;
            }
            if (now < 0) now = 0;
            flashWatch();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (pageAnimation != null) pageAnimation.dispatchTouch(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return pageAnimation.onTouch(event);
    }

    /**
     * 设置背景颜色
     *
     * @param color 背景颜色
     */
    public void setColor(int color) {
        this.color = color;
        pageAnimation.onPaintChange();
    }

    public interface PageTurn {
        /**
         * 用户点击了向右滑动后调用的函数
         *
         * @param position         当前观看的文本位置
         * @param isPositionChange 当前的文本位置是否发生改变
         * @param isPageEnable     翻页是否能用
         * @param isNowOverflow    now是否溢出
         * @return 是否是第一页
         */
        public boolean onLastPage(int position, boolean isPositionChange, boolean isPageEnable, boolean isNowOverflow);

        public boolean onNextPage(int position, boolean isPositionChange, boolean isPageEnable, boolean isNowOverflow);

        /**
         * 用户点击了中间调用的函数
         */
        public void onShowAction();

        /**
         * 函数用于在翻页过后更新观看位置
         */
        public void onUpdateWatch(long addTime, int watch);

        /**
         * 拿到当前观看章节
         */
        public int getChapter();
    }
}
