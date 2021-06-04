package flandre.cn.novel.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import flandre.cn.novel.R;

/**
 * 右侧的字母选择
 * 2020.5.1
 */
public class SlideBar extends View {
    public static final String ALL_LETTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String ALL_NUMBER = "0123456789";
    public final static String[] LETTER = {"~", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    private Paint mPaint;
    private int mTextPadding;  // 文字间距
    private int mTextHeight;  // 文字完整高度
    private int mTop;  // 顶部位置
    private int mCount = -1;  // 当前点击的字母位置
    private OnTouchLetterListener mTouchLetterListener;
    private TextView mShowLetter;  // 显示当前点击字母的View
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setVisibility(INVISIBLE);
            mCount = -1;
        }
    };

    public SlideBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideBar);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);  // 画笔的粗细
        mPaint.setTextSize(typedArray.getDimension(R.styleable.SlideBar_textSize, 0x20));
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setColor(typedArray.getColor(R.styleable.SlideBar_textColor, Color.WHITE));
        mTextPadding = (int) typedArray.getDimension(R.styleable.SlideBar_textPadding, 5);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 如果宽或高设置为 wrap_content
        if (heightMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.AT_MOST) {
            if (widthMode == MeasureSpec.AT_MOST) {
                // 宽等于padding + 文字的大小
                widthMeasureSpec = getPaddingRight() + (int) mPaint.measureText("W") + widthMode;
            }
            if (heightMode == MeasureSpec.AT_MOST) {
                // 高等于 宽 + (文字高 + 文字间隔) * LETTER.length
                int textHeight = mTextPadding * 2 + (int) mPaint.getTextSize();
                heightMeasureSpec = textHeight * LETTER.length + heightMode + MeasureSpec.getSize(widthMeasureSpec);
            }
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setShowLetter(TextView ShowLetter) {
        this.mShowLetter = ShowLetter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        removeCallbacks(runnable);
        mTextHeight = mTextPadding * 2 + (int) mPaint.getTextSize();
        mTop = mTextHeight + getWidth() / 5 * 2;
        int top = mTop;
        for (String s : LETTER) {
            int left = (int) (getWidth() / 2 - mPaint.measureText(s) / 2);
            canvas.drawText(s, left, top, mPaint);
            top += mTextHeight;
        }
        postDelayed(runnable, 2000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            removeCallbacks(runnable);
            if (mShowLetter != null) mShowLetter.setVisibility(VISIBLE);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            postDelayed(runnable, 2000);
            if (mShowLetter != null) mShowLetter.setVisibility(GONE);
        }
        int y = (int) event.getY();
        int top = mTop;
        int count;
        for (count = 0; count < LETTER.length - 1; ) {
            if (top < y) {
                count++;
                top += mTextHeight;
            } else break;
        }
        if (mCount == count) return true;
        if (mShowLetter != null) {
            mShowLetter.setText(LETTER[count]);
        }
        if (mTouchLetterListener != null) {
            mCount = count;
            mTouchLetterListener.onTouchLetter(LETTER[mCount]);
        }
        return true;
    }

    public void setTouchLetterListener(OnTouchLetterListener touchLetterListener) {
        this.mTouchLetterListener = touchLetterListener;
    }

    public interface OnTouchLetterListener {
        public void onTouchLetter(String letter);
    }
}
