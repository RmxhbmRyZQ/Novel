package novel.flandre.cn.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * 圆形纯颜色
 * 2020.5.4
 */
public class CircleView extends View {
    private Paint mPaint;
    private int mLength = 0;
    private int mPadding = 0;
    private boolean isSelected = false;

    public CircleView(Context context) {
        super(context);
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        setMeasuredDimension(mLength + widthMode + 2 * mPadding, mLength + heightMode + 2 * mPadding);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float pos = ((float) mLength) / 2 + mPadding;
        float radius;
        if (!isSelected) radius = ((float) mLength) / 2;
        else radius = ((float) mLength + mPadding) / 2;
        canvas.drawCircle(pos, pos, radius, mPaint);
    }

    public void setLength(int length) {
        this.mLength = length;
    }

    public void setPadding(int padding) {
        this.mPadding = padding;
    }

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }
}
