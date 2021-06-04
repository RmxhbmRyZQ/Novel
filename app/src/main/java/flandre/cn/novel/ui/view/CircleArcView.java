package flandre.cn.novel.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import flandre.cn.novel.R;

public class CircleArcView extends View {
    private int color;
    private float thick;
    private float angle;
    private float width;
    private float height;
    private Path path = new Path();
    private Paint paint;

    public CircleArcView(Context context) {
        super(context);
        initPaint();
    }

    public CircleArcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleArcView);
        color = a.getColor(R.styleable.CircleArcView_color, Color.BLACK);
        thick = a.getDimension(R.styleable.CircleArcView_thick, 5);
        angle = a.getFloat(R.styleable.CircleArcView_angle, 0);
        initPaint();
        a.recycle();
    }

    public void initPaint(){
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.reset();
        path.addArc(0f + thick, 0f + thick, width - thick, height - thick, -90, angle);
        paint.setStrokeWidth(thick);
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
        postInvalidate();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        postInvalidate();
    }

    public float getThick() {
        return thick;
    }

    public void setThick(float thick) {
        this.thick = thick;
        postInvalidate();
    }
}
