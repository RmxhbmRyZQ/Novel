package novel.flandre.cn.ui.view.page;

import android.graphics.Canvas;

/**
 * 平移翻页
 *
 * 2020.6.24
 */
public class TranslationSmoothPageAnimation extends CascadeSmoothPageAnimation {
    public TranslationSmoothPageAnimation(PageView view) {
        super(view);
    }

    @Override
    public void onDraw(Canvas canvas) {
        switch (drawMode){
            case NORMAL_DRAW:
                canvas.drawBitmap(getBitmap().get(1), 0, 0, null);
                break;
            case SMOOTH_LAST_DRAW:
                canvas.drawBitmap(getBitmap().get(0), left, 0, null);
                canvas.drawBitmap(getBitmap().get(1), left + width, 0, null);
                break;
            case SMOOTH_NEXT_DRAW:
                canvas.drawBitmap(getBitmap().get(1), left, 0, null);
                canvas.drawBitmap(getBitmap().get(2), left + width, 0, null);
                break;
        }
    }
}
