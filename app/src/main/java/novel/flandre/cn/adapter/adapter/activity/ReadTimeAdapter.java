package novel.flandre.cn.adapter.adapter.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import novel.flandre.cn.R;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.bean.data.novel.WrapperNovelInfo;
import novel.flandre.cn.ui.view.CircularProgressView;
import novel.flandre.cn.ui.view.MultiPaintTextView;
import novel.flandre.cn.utils.tools.DisplayUtil;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;
import novel.flandre.cn.utils.tools.NovelTools;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReadTimeAdapter extends BaseQuickAdapter<WrapperNovelInfo, BaseViewHolder> implements BaseQuickAdapter.OnItemChildClickListener {
    private static final int MIN_DETAIL_INFO = 0;
    private static final int MAX_DETAIL_INFO = 1;

    public ReadTimeAdapter(@Nullable List<WrapperNovelInfo> data) {
        super(data);
        setMultiTypeDelegate(new MultiTypeDelegate<WrapperNovelInfo>() {
            @Override
            protected int getItemType(WrapperNovelInfo wrapperNovelInfo) {
                return wrapperNovelInfo.isShowDetailInfo() ? MAX_DETAIL_INFO : MIN_DETAIL_INFO;
            }
        });


        getMultiTypeDelegate()
                .registerItemType(MIN_DETAIL_INFO, R.layout.read_min_list)
                .registerItemType(MAX_DETAIL_INFO, R.layout.read_list);
        setOnItemChildClickListener(this);
    }

    @Override
    protected void convert(BaseViewHolder helper, WrapperNovelInfo item) {
        setTopItem(helper, item);
        if (item.isShowDetailInfo())
            setBottomItem(helper, item);
        helper.addOnClickListener(R.id.item);
    }

    private void setTopItem(BaseViewHolder helper, WrapperNovelInfo item){
        MultiPaintTextView top_intro = helper.getView(R.id.top_intro);
        TextView percent = helper.getView(R.id.percent);
        ImageView image = helper.getView(R.id.image);
        CircularProgressView progress = helper.getView(R.id.progress);

        NovelInfo info = item.getInfo();
        top_intro.setPaintCount(3)
                .addText(info.getName(), 0)
                .addText(info.getAuthor(), 1)
                .addText(info.getChapter(), 2);
        double dPercent = (double) item.getChapter() / (double) item.getCount() * 100;
        progress.setProgress((int) dPercent);
        percent.setText(new BigDecimal(dPercent).setScale(1, BigDecimal.ROUND_HALF_UP) + "%");
        image.setImageBitmap(NovelInfo.getBitmap(info.getImagePath(), mContext));

        NovelConfigure configure = NovelConfigureManager.getConfigure();
        top_intro.setPaints(configure.getNameTheme(), DisplayUtil.sp2px(mContext, 20), 0)
                .setPaints(configure.getAuthorTheme(), DisplayUtil.sp2px(mContext, 15), 1)
                .setPaints(configure.getIntroduceTheme(), DisplayUtil.sp2px(mContext, 15), 2)
                .setMargin(0, 0).setMargin(DisplayUtil.dip2px(mContext, 6), 1)
                .setMargin(DisplayUtil.dip2px(mContext, 10), 2);
        progress.setBackColor(~configure.getBackgroundTheme() & 0x11FFFFFF | 0x11000000);
        progress.setProgColor(configure.getIntroduceTheme());
        percent.setTextColor(configure.getIntroduceTheme());
    }

    private void setBottomItem(BaseViewHolder helper, WrapperNovelInfo item) {
        TextView start = helper.getView(R.id.start);
        TextView year_left = helper.getView(R.id.year_left);
        TextView date_left = helper.getView(R.id.date_left);
        TextView finish = helper.getView(R.id.finish);
        TextView year_right = helper.getView(R.id.year_right);
        TextView date_right = helper.getView(R.id.date_right);
        MultiPaintTextView bottom_intro = helper.getView(R.id.bottom_intro);
        TextView status = helper.getView(R.id.status);
        View sep_left = helper.getView(R.id.sep_left);
        View sep_right = helper.getView(R.id.sep_right);

        NovelInfo info = item.getInfo();
        year_left.setText(new SimpleDateFormat("yyyy年").format(info.getStart()));
        date_left.setText(new SimpleDateFormat("MM月\ndd日\nHH时\nmm分").format(info.getStart()));
        if (info.getFinish() != 0) {
            finish.setText("完成时间");
            year_right.setText(new SimpleDateFormat("yyyy年").format(info.getFinish()));
            date_right.setText(new SimpleDateFormat("MM月\ndd日\nHH时\nmm分").format(info.getStart()));
        }
        bottom_intro.setPaintCount(2).addText("当前观看章节", 0)
                .addText(item.getNowChapter(), 1)
                .addText("最近观看时间", 0)
                .addText(NovelTools.resolver(new Date().getTime() - info.getTime()) + "前", 1)
                .addText("书本使用来源", 0)
                .addText(info.getSource() != null ? NovelConfigureManager.novelSource.get(info.getSource()) : "本地导入", 1)
                .addText("小说观看时长", 0)
                .addText(NovelTools.resolver(info.getRead()), 1);
        status.setText(info.getComplete() == 1 ? "已完结" : "连载中");

        NovelConfigure configure = NovelConfigureManager.getConfigure();
        start.setTextColor(configure.getNameTheme());
        year_left.setTextColor(configure.getNameTheme());
        date_left.setTextColor(configure.getIntroduceTheme());
        finish.setTextColor(configure.getNameTheme());
        year_right.setTextColor(configure.getNameTheme());
        date_right.setTextColor(configure.getIntroduceTheme());
        sep_left.setBackgroundColor(configure.getNameTheme());
        sep_right.setBackgroundColor(configure.getNameTheme());
        bottom_intro.setPaints(configure.getNameTheme(), DisplayUtil.sp2px(mContext, 20), 0)
                .setPaints(configure.getAuthorTheme(), DisplayUtil.sp2px(mContext, 16), 1)
                .setMargin(DisplayUtil.dip2px(mContext, 16), 0)
                .setMargin(DisplayUtil.dip2px(mContext, 9), 1);
        status.setTextColor(configure.getIntroduceTheme());
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        WrapperNovelInfo wrapperNovelInfo = getData().get(position);
        wrapperNovelInfo.setShowDetailInfo(!wrapperNovelInfo.isShowDetailInfo());
        notifyItemChanged(position);
    }
}
