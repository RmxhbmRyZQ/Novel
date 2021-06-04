package flandre.cn.novel.ui.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.chad.library.adapter.base.BaseQuickAdapter;
import flandre.cn.novel.R;
import flandre.cn.novel.adapter.adapter.fragment.SearchResultAdapter;
import flandre.cn.novel.adapter.decoration.Decoration;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.ui.activity.ConfigureSourceActivity;
import flandre.cn.novel.ui.activity.NovelDetailActivity;
import flandre.cn.novel.ui.activity.SearchActivity;
import flandre.cn.novel.net.Crawler;
import flandre.cn.novel.utils.database.SQLiteNovel;
import flandre.cn.novel.bean.data.novel.NovelInfo;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

public class SearchResultFragment extends AttachFragment implements View.OnClickListener {
    private SearchResultAdapter resultAdapter;
    private View search_result;
    private TextView changeSource;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        search_result.setVisibility(isVisibleToUser ? View.VISIBLE : View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        search_result = inflater.inflate(R.layout.search_result, container, false);
        search_result.setTag("result");
        changeSource = search_result.findViewById(R.id.changeSource);
        changeSource.setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        changeSource.setOnClickListener(this);
        resultAdapter = new SearchResultAdapter(R.layout.search_list, null);
        resultAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                List<NovelInfo> data = adapter.getData();
                ImageView imageView = view.findViewById(R.id.image);
                data.get(position).setBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                Intent intent = new Intent(mContext, NovelDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("NovelInfo", data.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        RecyclerView result = search_result.findViewById(R.id.result);
        result.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        result.setAdapter(resultAdapter);
        result.addItemDecoration(new Decoration(mContext));
        search_result.findViewById(R.id.background).setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
        return search_result;
    }

    public void runSearch(String text) {
        SQLiteNovel sqLiteNovel = SQLiteNovel.getSqLiteNovel(mContext.getApplicationContext());
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("search", null,
                "name=?", new String[]{text}, null, null, null);
        if (cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("time", new Date().getTime());
            sqLiteNovel.getReadableDatabase().update("search", values, "name=?", new String[]{text});
        } else {
            ContentValues values = new ContentValues();
            values.put("name", text);
            values.put("time", new Date().getTime());
            sqLiteNovel.getReadableDatabase().insert("search", null, values);
        }
        cursor.close();
        Crawler crawler = NovelConfigureManager.getCrawler();
        crawler.search(text, new Crawler.OnRequestComplete<List<NovelInfo>>() {
            @Override
            public void onSuccess(List<NovelInfo> data) {
                SearchActivity mContext = (SearchActivity) SearchResultFragment.this.mContext;
                // 如果搜索成功显示数据, 如果网络存在问题显示提示页面
                changeSource.setVisibility(View.GONE);
                if (mContext.getRemindFragment().getUserVisibleHint()) {
                    mContext.getRemindFragment().setUserVisibleHint(false);
                    mContext.getResultFragment().setUserVisibleHint(true);
                }
                if (data.size() == 0) {
                    changeSource.setVisibility(View.VISIBLE);
                }
                // 显示到界面
                resultAdapter.setNewData(data);
                mContext.findViewById(R.id.theme).setBackgroundColor(Color.parseColor("#FFFFFF"));
                mContext.getSearchView().setSubmitButtonEnabled(true);
                mContext.getRefreshLayout().setRefreshing(false);
            }

            @Override
            public void onFail(Throwable e) {
                SearchActivity mContext = (SearchActivity) SearchResultFragment.this.mContext;
                if (!mContext.getRemindFragment().getUserVisibleHint()) {
                    mContext.getRemindFragment().setUserVisibleHint(true);
                    mContext.getResultFragment().setUserVisibleHint(false);
                }
                Toast.makeText(mContext, "搜索失败，网络异常", Toast.LENGTH_SHORT).show();
                mContext.getSearchView().setSubmitButtonEnabled(true);
                mContext.getRefreshLayout().setRefreshing(false);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(mContext, ConfigureSourceActivity.class);
        startActivity(intent);
    }
}
