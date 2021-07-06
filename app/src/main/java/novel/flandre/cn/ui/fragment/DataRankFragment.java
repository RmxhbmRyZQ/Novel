package novel.flandre.cn.ui.fragment;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.fragment.RankDataAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.net.Crawler;
import novel.flandre.cn.ui.activity.NovelDetailActivity;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.List;

/**
 * 排行榜
 * 2019.??
 */
public class DataRankFragment extends AttachFragment {
    private RankDataAdapter adapter;
    private int rankType;  // 更新的类型
    private boolean loadEnable;  // 加载排行榜是否可用
    private SwipeRefreshLayout refresh;
    private LinearLayout loading_wrap;
    private ImageView loading_img;
    private TextView loading_text;
    private TextView io_error;

    public RankDataAdapter getAdapter() {
        return adapter;
    }

    public SwipeRefreshLayout getRefresh() {
        return refresh;
    }

    /**
     * 传递创建的类型
     */
    public static DataRankFragment newInstance(int type) {
        DataRankFragment fragment = new DataRankFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * 当用户第一次看见时更新数据
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (loadEnable && isVisibleToUser) {
            loadEnable = false;
            updateData();
        }
    }

    boolean isLoadEnable() {
        return loadEnable;
    }

    void setLoadEnable(boolean loadEnable) {
        this.loadEnable = loadEnable;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadEnable = true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void updateData() {
        Crawler crawler = NovelConfigureManager.getCrawler();
        crawler.rank(rankType, new Crawler.OnRequestComplete<List<NovelInfo>>() {
            @Override
            public void onSuccess(List<NovelInfo> data) {
                // 去掉空值
                while (data.remove(null)) ;
                adapter.setNewData(data);
                if (data.size() == 0) {
                    io_error.setVisibility(View.VISIBLE);
                }else {
                    io_error.setVisibility(View.GONE);
                }
                loading_wrap.setVisibility(View.GONE);
                refresh.setRefreshing(false);
            }

            @Override
            public void onFail(Throwable e) {
                adapter.setNewData(null);
                io_error.setVisibility(View.VISIBLE);
                loading_wrap.setVisibility(View.GONE);
                refresh.setRefreshing(false);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rankType = getArguments().getInt("type");
        SQLiteNovel.getSqLiteNovel(mContext.getApplicationContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rank_detail_layout, container, false);
        loadingUI(view);
        loadRankUI(view);
        io_error = view.findViewById(R.id.io_error);
        io_error.setTextColor(NovelConfigureManager.getConfigure().getAuthorTheme());
        return view;
    }

    private void loadingUI(View view) {
        // 加载加载界面
        loading_wrap = view.findViewById(R.id.load_wrap);
        loading_img = view.findViewById(R.id.load_img);
        loading_img.setBackground(mContext.getResources().getDrawable(NovelConfigureManager.getConfigure().getMode()
                == NovelConfigure.DAY ? R.drawable.loading_day : R.drawable.loading_night));

        loading_text = view.findViewById(R.id.load_txt);
        loading_text.setTextColor(NovelConfigureManager.getConfigure().getNameTheme());
        // 让图片旋转
        RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        loading_img.setAnimation(animation);
        animation.setDuration(1000);

    }

    private void loadRankUI(View view) {
        refresh = view.findViewById(R.id.fresh);
        refresh.setColorSchemeColors(NovelConfigureManager.getConfigure().getMainTheme());

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.data);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayout.VERTICAL);
        recyclerView.setLayoutManager(manager);
        adapter = new RankDataAdapter(R.layout.rank_list, null);
        adapter.bindToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new Decoration(mContext));
        recyclerView.setHasFixedSize(false);
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                // 点击排行榜上的Item时进入查看详细界面
                ImageView imageView = view.findViewById(R.id.image);
                NovelInfo novelInfo = (NovelInfo) adapter.getData().get(position);
                novelInfo.setBitmap(((BitmapDrawable)imageView.getDrawable()).getBitmap());
                Intent intent = new Intent(mContext, NovelDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("NovelInfo", novelInfo.copy());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    void changeTheme() {
        refresh.setColorSchemeColors(NovelConfigureManager.getConfigure().getMainTheme());
        loading_img.setBackground(mContext.getResources().getDrawable(
                NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY ? R.drawable.loading_day : R.drawable.loading_night));
        loading_text.setTextColor(NovelConfigureManager.getConfigure().getNameTheme());
        io_error.setTextColor(NovelConfigureManager.getConfigure().getAuthorTheme());
        adapter.notifyDataSetChanged();
    }
}
