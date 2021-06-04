package flandre.cn.novel.adapter.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import flandre.cn.novel.utils.tools.NovelConfigureManager;

import java.util.List;

public class Decoration extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };
    private Drawable mDivider;
    private Paint paint;
    private List<Integer> list;

    public void setList(List<Integer> list) {
        this.list = list;
    }

    public Decoration(Context context) {
        this(context, null);
    }

    public Decoration(Context context, List<Integer> list) {
        // 拿到ListView的属性
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        paint = new Paint();
        mDivider = a.getDrawable(0);
        a.recycle();
        this.list = list;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        paint.setColor(NovelConfigureManager.getConfigure().getIntroduceTheme() & 0x22FFFFFF | 0x22000000);

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            if (list != null && list.contains(i)) continue;
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            c.drawRect(left, top, right, bottom, paint);
//            mDivider.setBounds(left, top, right, bottom);
//            mDivider.draw(c);
        }

    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, int itemPosition, @NonNull RecyclerView parent) {
        if (list != null && list.contains(itemPosition)) return;
        // 横屏或竖屏
        outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
    }
}
