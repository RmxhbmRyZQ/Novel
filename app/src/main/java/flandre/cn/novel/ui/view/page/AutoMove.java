package flandre.cn.novel.ui.view.page;

import android.os.Handler;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.utils.database.SharedTools;

/**
 * 自动阅读
 *
 * 2021-2-8
 */
public class AutoMove {
    // 2.进入网络 IO 延迟时的情况还没有检查
    private PageView mPageView;
    private ScrollPageAnimation mScrollPageAnimation;
    private Handler mHandler;
    private int mCount = 0;
    private Runnable mMove = new Runnable() {
        @Override
        public void run() {
            mScrollPageAnimation.scrollY(-SharedTools.getSharedTools().getMoveSpeed() / 40);
            mHandler.postDelayed(mMove, 20);
        }
    };

    public AutoMove(PageView pageView, Handler handler){
        mPageView = pageView;
        mHandler = handler;
    }

    public void prepare(){
        mScrollPageAnimation = new ScrollPageAnimation(mPageView);
        mPageView.setPageAnimation(mScrollPageAnimation);
    }

    public void move(){
        mCount++;
        if (mCount <= 0) return;
        mHandler.post(mMove);
    }

    public void pause(){
        mCount--;
        if (mCount > 0) return;
        mHandler.removeCallbacks(mMove);
    }

    public void cancel(){
        mPageView.setPageAnimation(NovelConfigureManager.getPageAnimation(mPageView));
        mPageView.postInvalidate();
    }
}
