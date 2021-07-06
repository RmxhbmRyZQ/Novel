package novel.flandre.cn.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import novel.flandre.cn.R;

public class CheckBoxView extends View {
    private int centerX;
    private int centerY;
    private int radius;  // 半径
    private int big;  // 膨胀的大小
    private int progress = 1000;
    private boolean isCheck;
    private boolean isMoving = false;
    private int color = 0x333333, stroke = 10, unCheckColor = 0x33333333;
    private Paint paint;
    private Paint backgroundPaint;
    private Paint checkPaint;
    private Paint backgroundCheckPaint;

    public CheckBoxView(Context context) {
        super(context);
        initPaint();
    }

    public CheckBoxView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckBoxView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CheckBoxView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxView);
        color = ta.getColor(R.styleable.CheckBoxView_colorC, 0xffff00);
        unCheckColor = ta.getColor(R.styleable.CheckBoxView_unCheckColor, 0x333333);
        stroke = (int) ta.getDimension(R.styleable.CheckBoxView_stroke, 0x10);
        big = (int) ta.getDimension(R.styleable.CheckBoxView_change, 0x10);
        ta.recycle();
        initPaint();
    }

    private void initPaint() {
        paint = new Paint();
        setPaintStyle(color, stroke, paint);
        checkPaint = new Paint();
        setPaintStyle(0xffffffff, stroke, checkPaint);

        backgroundPaint = new Paint();
        setPaintStyle(unCheckColor, stroke, backgroundPaint);
        backgroundCheckPaint = new Paint();
        setPaintStyle(unCheckColor, stroke, backgroundCheckPaint);
    }

    private void setPaintStyle(int color, int stroke, Paint paint) {
        paint.setColor(color);
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
        postInvalidate();
    }

    public void setBig(int big) {
        radius += this.big - big;
        this.big = big;
        postInvalidate();
    }

    public void setUnCheckColor(int unCheckColor) {
        this.unCheckColor = unCheckColor;
        backgroundCheckPaint.setColor(unCheckColor);
        backgroundPaint.setColor(unCheckColor);
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        radius = (centerX < centerY ? centerX : centerY) - stroke / 2;
        radius -= big;
    }

    public void setProgress(int p) {
        progress = p;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int halfWidth;
        int paintWidth;
        if (progress < 30) {
            // 画圆弧
            paint.setStyle(Paint.Style.STROKE);
            halfWidth = stroke / 2;
            canvas.drawArc(centerX - radius + halfWidth, centerY - radius + halfWidth, centerX + radius - halfWidth,
                    centerY + radius - halfWidth, 90, progress * 12 + 12, false, paint);
            return;
        }

        if (progress < 60) {
            // 画圆环
            paint.setStyle(Paint.Style.STROKE);
            paintWidth = stroke + (progress - 30) * ((radius - stroke) / 30);
            halfWidth = paintWidth / 2;
            paint.setStrokeWidth(paintWidth);
            canvas.drawArc(centerX - radius + halfWidth, centerY - radius + halfWidth, centerX + radius - halfWidth,
                    centerY + radius - halfWidth, 90, 360, false, paint);
            paint.setStrokeWidth(stroke);
            return;
        }

        if (progress < 80) {
            // 画圆变大
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(centerX, centerY, radius + (int) ((double) (progress - 60) / 20 * big), paint);
            int c = (int) (0xff * (((double) progress - 60) / 20));
            checkPaint.setColor((c << 24) + 0xffffff);
            drawCheck(checkPaint, canvas);
            return;
        }

        if (progress <= 100) {
            // 画圆缩小
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(centerX, centerY, radius + (int) ((double) Math.abs(progress - 100) / 20 * big) + stroke / 2, paint);
            drawCheck(checkPaint, canvas);
            if (progress == 100) isMoving = false;
            return;
        }

        if (progress <= 200) {
            // 画虚线圆环
            halfWidth = stroke / 2;
            canvas.drawArc(centerX - radius + halfWidth, centerY - radius + halfWidth, centerX + radius - halfWidth,
                    centerY + radius - halfWidth, 90, (int) ((progress - 100) * 3.6), false, backgroundPaint);
            int c = (int) ((unCheckColor >>> 28) * ((double) (progress - 100) / 100));
            backgroundCheckPaint.setColor((c << 28) + (unCheckColor & 0xffffff));
            drawCheck(backgroundCheckPaint, canvas);
            if (progress == 200) isMoving = false;
            return;
        }

        // 未选择的画面
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
        drawCheck(backgroundCheckPaint, canvas);
    }

    private void drawCheck(Paint paint, Canvas canvas) {
        float m = (float) radius / 3;
        Path path = new Path();
        path.moveTo(centerX - m, centerY);
        path.lineTo(centerX, centerY + m);
        path.lineTo(centerX + m, centerY - m);
        canvas.drawPath(path, paint);
    }

    public void setUnUICheck(boolean check) {
        isCheck = check;
        if (isCheck) progress = 100;
        else progress = 201;
        postInvalidate();
    }

    public void setCheck(boolean check) {
        if (isMoving) return;
        if (isCheck == check) return;
        isCheck = check;
        isMoving = true;
        if (check) {
            ObjectAnimator animator = ObjectAnimator.ofInt(this, "Progress", 0, 100);
            animator.setDuration(1000);
            animator.start();
            return;
        }

        // 取消选择

        ObjectAnimator animator = ObjectAnimator.ofInt(this, "Progress", 101, 200);
        animator.setDuration(1000);
        animator.start();
    }

    public boolean isCheck() {
        return isCheck;
    }
}
