package flandre.cn.novel.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MultiPaintTextView extends View {
    private Paint[] paints;
    private List<Data> text;
    private int[] margin;
    private int paintCount;

    public MultiPaintTextView setPaintCount(int paintCount) {
        this.paintCount = paintCount;
        paints = new Paint[paintCount];
        text = new ArrayList<>();
        margin = new int[paintCount];
        return this;
    }

    public MultiPaintTextView setMargin(int margin, int pos) {
        this.margin[pos] = margin;
        return this;
    }

    public MultiPaintTextView(Context context) {
        super(context);
    }

    public MultiPaintTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiPaintTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MultiPaintTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (paintCount == 0 || text.size() == 0)
            setMeasuredDimension(0, 0);
        else {
            float height = getPaddingBottom() + getPaddingTop() + getBottomPaddingOffset() + getTopPaddingOffset();
            height += paints[text.get(0).getPaintPos()].getTextSize();
            for (int i = 1; i < text.size(); i++) {
                height += margin[text.get(i).getPaintPos()];
                height += paints[text.get(i).getPaintPos()].getTextSize();
            }
            setMeasuredDimension(getDefaultSize(getSuggestedMinimumHeight(), widthMeasureSpec), (int) height);
        }
    }

    public MultiPaintTextView setPaints(int color, int textSize, int pos) {
        paints[pos] = new Paint();
        paints[pos].setColor(color);
        paints[pos].setTextSize(textSize);
        return this;
    }

    public MultiPaintTextView addText(String text, int pos){
        this.text.add(new Data(text, pos));
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (text.size() == 0) return;
        float x = 0, y = paints[text.get(0).getPaintPos()].getTextSize() + getPaddingTop() + getTopPaddingOffset();
        canvas.drawText(text.get(0).getText(), x, y, paints[text.get(0).getPaintPos()]);
        for (int i=1;i<text.size();i++) {
            y += margin[text.get(i).getPaintPos()];
            y += paints[text.get(i).getPaintPos()].getTextSize();
            canvas.drawText(text.get(i).getText(), x, y, paints[text.get(i).getPaintPos()]);
        }
    }

    public static class Data {
        private String text;
        private int paintPos;

        public Data(String text, int paintPos) {
            this.text = text;
            this.paintPos = paintPos;
        }

        public String getText() {
            return text;
        }

        public int getPaintPos() {
            return paintPos;
        }
    }
}
