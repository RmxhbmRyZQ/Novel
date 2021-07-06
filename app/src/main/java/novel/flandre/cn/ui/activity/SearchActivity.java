package novel.flandre.cn.ui.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import novel.flandre.cn.R;
import novel.flandre.cn.net.Crawler;
import novel.flandre.cn.ui.fragment.SearchRemindFragment;
import novel.flandre.cn.ui.fragment.SearchResultFragment;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

/**
 * 搜索小说
 * 2019.12.8
 */
public class SearchActivity extends BaseActivity {
    private SearchView searchView;  // 顶部的搜索框
    private SwipeRefreshLayout refreshLayout;  // 下拉的旋转组件
    private SearchRemindFragment remindFragment;
    private SearchResultFragment resultFragment;
    private SQLiteNovel sqLiteNovel;
    private Crawler crawler;
    private boolean load = true;

    public Crawler getCrawler() {
        if (crawler == null){
            crawler = NovelConfigureManager.getCrawler();
        }
        return crawler;
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    public SQLiteNovel getSqLiteNovel() {
        return sqLiteNovel;
    }

    public SearchRemindFragment getRemindFragment() {
        return remindFragment;
    }

    public SearchResultFragment getResultFragment() {
        return resultFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (load){
            load = false;
            remindFragment.setUserVisibleHint(true);
            resultFragment.setUserVisibleHint(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupMusicService();
        NovelConfigure configure = NovelConfigureManager.getConfigure(getApplicationContext());
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(configure.getMainTheme()));

        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(NovelConfigureManager.getConfigure().getMainTheme());
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉时如果搜索框里有文字, 进行重新搜索, 否则什么都不做
                if (!searchView.getQuery().toString().equals("")) {
                    searchView.setSubmitButtonEnabled(false);
                    resultFragment.runSearch(searchView.getQuery().toString());
                } else
                    refreshLayout.setRefreshing(false);
            }
        });
        findViewById(R.id.total).setBackgroundColor(configure.getBackgroundTheme());
        findViewById(R.id.theme).setBackgroundColor((~configure.getBackgroundTheme()) & 0x11FFFFFF | 0x11000000);
        setupFragment(savedInstanceState);
    }

    private void setupFragment(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            remindFragment = new SearchRemindFragment();
            resultFragment = new SearchResultFragment();
            fragmentTransaction.add(R.id.data, remindFragment, "SearchRemindFragment");
            fragmentTransaction.add(R.id.data, resultFragment, "SearchResultFragment");
            fragmentTransaction.commit();
        }else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            remindFragment = (SearchRemindFragment) getSupportFragmentManager().findFragmentByTag("SearchRemindFragment");
            resultFragment = (SearchResultFragment) getSupportFragmentManager().findFragmentByTag("SearchResultFragment");
            fragmentTransaction.attach(remindFragment);
            fragmentTransaction.attach(resultFragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        final MenuItem item = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 点击时搜索内容并return给handler
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(true);
                    searchView.setSubmitButtonEnabled(false);
                    resultFragment.runSearch(s);
                } else {
                    Toast.makeText(SearchActivity.this, "搜索紧啊，扑街！", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.equals("") && !remindFragment.getUserVisibleHint()) {
                    remindFragment.setUserVisibleHint(true);
                    resultFragment.setUserVisibleHint(false);
                    findViewById(R.id.theme).setBackgroundColor((~NovelConfigureManager.getConfigure().getBackgroundTheme()) & 0x11FFFFFF | 0x11000000);
                }
                return true;
            }
        });
        // 设置搜索为打开状态
        searchView.setIconified(false);
        searchView.setSubmitButtonEnabled(true);
        return true;
    }
}
