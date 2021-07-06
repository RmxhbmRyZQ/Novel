package novel.flandre.cn.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.fragment.SearchRecordAdapter;
import novel.flandre.cn.adapter.adapter.fragment.SearchRemindAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.data.novel.NovelRemind;
import novel.flandre.cn.net.Crawler;
import novel.flandre.cn.ui.activity.SearchActivity;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.ArrayList;
import java.util.List;

;

public class SearchRemindFragment extends AttachFragment {

    private SearchRemindAdapter remindAdapter;
    private SearchRecordAdapter recordAdapter;
    private View search_remind;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        search_remind.setVisibility(isVisibleToUser ? View.VISIBLE : View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        search_remind = inflater.inflate(R.layout.search_remind, container, false);
        search_remind.setTag("remind");

        ((TextView) search_remind.findViewById(R.id.remind_txt)).setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        ((TextView) search_remind.findViewById(R.id.record_txt)).setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());

        recordAdapter = new SearchRecordAdapter(R.layout.record_list, null);
        recordAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ((SearchActivity) mContext).getSearchView().setQuery(recordAdapter.getData().get(position),
                        ((SearchActivity) mContext).getSearchView().isSubmitButtonEnabled());
            }
        });
        RecyclerView record = search_remind.findViewById(R.id.record);
        record.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        record.setAdapter(recordAdapter);

        List<NovelRemind> list = new ArrayList<>();
        NovelRemind novelRemind = new NovelRemind();
        novelRemind.setName("正在搜索");
        novelRemind.setChapter("......");
        list.add(novelRemind);
        remindAdapter = new SearchRemindAdapter(R.layout.remind_list, list);
        remindAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                List<NovelRemind> data = adapter.getData();
                if (data.size() == 1) {
                    if (data.get(0).getName().equals("网络异常")) {
                        getData();
                        adapter.getViewByPosition(R.id.name, position);
                        ((TextView) view.findViewById(R.id.name)).setText("正在重试");
                        ((TextView) view.findViewById(R.id.chapter)).setText("......");
                    }
                } else {
                    if (!((SearchActivity) mContext).getRefreshLayout().isRefreshing())
                        ((SearchActivity) mContext).getSearchView().setQuery(data.get(position).getName(),
                                ((SearchActivity) mContext).getSearchView().isSubmitButtonEnabled());
                    else
                        Toast.makeText(mContext, "搜索紧啊，扑街！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        RecyclerView remind = search_remind.findViewById(R.id.remind);
        remind.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        remind.setAdapter(remindAdapter);
        remind.addItemDecoration(new Decoration(mContext));
        remindAdapter.bindToRecyclerView(remind);

        record.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        remind.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        remind.setNestedScrollingEnabled(false);
        remind.setFocusable(false);
        loadData();
        return search_remind;
    }

    /**
     * 给提示页面加载数据
     */
    private void loadData() {
        List<String> list = new ArrayList<>();
        Cursor cursor = SQLiteNovel.getSqLiteNovel(mContext.getApplicationContext()).getReadableDatabase().query("search",
                new String[]{"name"}, null, null, null, null, "-time", "8");
        if (cursor.moveToNext()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        recordAdapter.setNewData(list);
        getData();
    }

    private void getData() {
        Crawler crawler = ((SearchActivity) mContext).getCrawler();
        crawler.remind(new Crawler.OnRequestComplete<List<NovelRemind>>() {
            @Override
            public void onSuccess(List<NovelRemind> data) {
                if (data.size() == 0) {
                    NovelRemind novelRemind = new NovelRemind();
                    novelRemind.setName("网络异常");
                    novelRemind.setChapter("点击重试");
                    data.add(novelRemind);
                }
                remindAdapter.setNewData(data);
            }

            @Override
            public void onFail(Throwable e) {
                e.printStackTrace();
                List<NovelRemind> data = remindAdapter.getData();
                data.clear();
                NovelRemind novelRemind = new NovelRemind();
                novelRemind.setName("网络异常");
                novelRemind.setChapter("点击重试");
                data.add(novelRemind);
                remindAdapter.notifyDataSetChanged();
            }
        });
    }
}
