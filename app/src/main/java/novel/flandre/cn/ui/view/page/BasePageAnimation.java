package novel.flandre.cn.ui.view.page;

import android.view.MotionEvent;

public abstract class BasePageAnimation implements PageAnimation {
    int width;
    int height;
    PageView mPageView;

    public BasePageAnimation(PageView view){
        mPageView = view;
    }

    public PageView getPageView() {
        return mPageView;
    }

    @Override
    public void onLoad(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void dispatchTouch(MotionEvent event) {

    }

    @Override
    public void onCycle() {

    }
}
