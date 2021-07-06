package novel.flandre.cn.ui.activity;

import android.content.ContentValues;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.activity.DownloadAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.data.novel.NovelDownloadInfo;
import novel.flandre.cn.service.NovelService;
import novel.flandre.cn.utils.database.SQLTools;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.List;

/**
 * 下载管理
 * 2020.4.2
 */
public class DownloadManagerActivity extends BaseActivity implements DownloadAdapter.OnAllDataDeleted {
    private DownloadAdapter adapter;
    private SQLiteNovel sqLiteNovel;
    private TextView textView;
    private Handler handler;

    public NovelService getService(){
        return mService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);
        setupMusicService();
        NovelConfigureManager.getConfigure(getApplicationContext());
        addDownloadFinishListener(this);
        handler = new Handler(getMainLooper());

        sqLiteNovel = SQLiteNovel.getSqLiteNovel(getApplicationContext());
        setupNovelService();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("下载管理");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(NovelConfigureManager.getConfigure().getMainTheme()));
        textView = findViewById(R.id.none);
        textView.setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        findViewById(R.id.total).setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());

        RecyclerView recyclerView = findViewById(R.id.data);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new Decoration(this));
        adapter = new DownloadAdapter(R.layout.download_list, null);
        adapter.setDataDeleted(this);
//        adapter = new Adapter(null);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);
        loadData();
    }

    /**
     * 加载下载信息
     */
    private void loadData() {
        List<NovelDownloadInfo> downloadInfo = SQLTools.getDownloadInfo(sqLiteNovel);
        if (downloadInfo.size() > 0) {
            adapter.setNewData(downloadInfo);
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeDownloadFinishListener(this);
    }

    @Override
    public void onDownloadFinish(int downloadFinish, int downloadCount, long downloadId) {
        int pos = 0;
        for (; pos < adapter.getData().size(); pos++)
            if (adapter.getData().get(pos).getId() == downloadId)
                break;
        if (pos >= adapter.getData().size()) return;
        NovelDownloadInfo downloadInfo = adapter.getData().get(pos);
        // 如果下载下一个任务, 重新导入
        if (downloadInfo.getStatus() != SQLiteNovel.DOWNLOAD_PAUSE) {
            loadData();
            return;
        }
        downloadInfo.setCount(downloadCount);
        downloadInfo.setFinish(downloadFinish);
        // 下载完成时更新状态
        if (downloadCount == downloadFinish) {
            downloadInfo.setStatus(SQLiteNovel.DOWNLOAD_FINISH);
            ContentValues values = new ContentValues();
            values.put("finish", downloadFinish);
            values.put("count", downloadCount);
            values.put("status", downloadInfo.getStatus());
            sqLiteNovel.getReadableDatabase().update("download", values, "id = ?", new String[]{String.valueOf(downloadId)});
        }
        // 这里有个小bug, 一直更新界面的话会消化不了用户的点击
        // adapter.notifyDataSetChanged();
        // 使用消息队列应该可以解决这个bug, 因为点击事件使用消息队列
        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        // adapter.notifyItemChanged(pos);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAllDataDeleted() {
        textView.setVisibility(View.VISIBLE);
    }
}
