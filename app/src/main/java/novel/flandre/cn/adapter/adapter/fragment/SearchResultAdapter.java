package novel.flandre.cn.adapter.adapter.fragment;

import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import novel.flandre.cn.R;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.List;

public class SearchResultAdapter extends BaseQuickAdapter<NovelInfo, BaseViewHolder> {
    public SearchResultAdapter(int layoutResId, @Nullable List<NovelInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, NovelInfo item) {
//        helper.setImageBitmap(R.id.image, item.getBitmap());
        ImageView imageView = helper.getView(R.id.image);
        RequestOptions options = new RequestOptions();
        options.placeholder(R.drawable.img_loading)
                .error(R.drawable.not_found)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(mContext)
                .load(item.getImagePath())
                .apply(options)
                .into(imageView);
        helper.setText(R.id.name, item.getName());
        helper.setText(R.id.author, item.getAuthor());
        helper.setText(R.id.chapter, item.getChapter());
        helper.setTextColor(R.id.name, NovelConfigureManager.getConfigure().getNameTheme());
        helper.setTextColor(R.id.author, NovelConfigureManager.getConfigure().getAuthorTheme());
        helper.setTextColor(R.id.chapter, NovelConfigureManager.getConfigure().getIntroduceTheme());
        helper.addOnClickListener(R.id.wrap);
    }
}
