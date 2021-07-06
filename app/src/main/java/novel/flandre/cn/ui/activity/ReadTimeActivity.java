package novel.flandre.cn.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.activity.ReadTimeAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.data.novel.WrapperNovelInfo;
import novel.flandre.cn.utils.database.SQLTools;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.database.SharedTools;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.List;

import static novel.flandre.cn.utils.tools.NovelTools.resolver;

/**
 * 阅读记录
 * 2019.12.9
 */
public class ReadTimeActivity extends BaseActivity {
    private ReadTimeAdapter adapter;
    private SQLiteNovel sqLiteNovel;
    private NovelConfigure configure;
    private boolean isLoadData = false;
    private ImageView loading;
    private ObjectAnimator objectAnimator;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_time);
        setupMusicService();
        configure = NovelConfigureManager.getConfigure(getApplicationContext());
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(getApplicationContext());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("阅读时长");
        actionBar.setBackgroundDrawable(new ColorDrawable(configure.getMainTheme()));
        handler = new Handler(getMainLooper());
        setupLoading();
        setupValue();
        setupData();
    }

    private void setupLoading() {
        loading = findViewById(R.id.loading);
        loading.setImageResource(NovelConfigureManager.getConfigure().getMode() == NovelConfigure.DAY ? R.drawable.loading_day : R.drawable.loading_night);
        objectAnimator = ObjectAnimator.ofFloat(loading, "rotation", 0, 360);
        objectAnimator.setDuration(1000);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLoadData) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    isLoadData = true;
                    loadDate();
                }
            });
        }
    }

    private void loadDate() {
        List<WrapperNovelInfo> list = SQLTools.getWrapperNovelInfo(sqLiteNovel);
        adapter.setNewData(list);
    }

    private void setupData() {
        RecyclerView recyclerView = findViewById(R.id.introduce);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new Decoration(this));
        adapter = new ReadTimeAdapter(null);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isLoadData) return;
                objectAnimator.pause();
                loading.setVisibility(View.GONE);
            }
        });
    }

    private void setupValue() {
        NestedScrollView scrollView = findViewById(R.id.scroll);
        scrollView.setBackgroundColor(configure.getBackgroundTheme());
        findViewById(R.id.sep).setBackgroundColor((~configure.getBackgroundTheme()) & 0x11FFFFFF | 0x11000000);
        ((TextView) findViewById(R.id.read_time)).setTextColor(configure.getNameTheme());
        ((TextView) findViewById(R.id.read_count)).setTextColor(configure.getNameTheme());
        ((TextView) findViewById(R.id.read_finish)).setTextColor(configure.getNameTheme());
        TextView read_time = findViewById(R.id.read_time_data);
        TextView read_count = findViewById(R.id.read_count_data);
        TextView read_finish = findViewById(R.id.read_finish_data);
        read_time.setTextColor(configure.getIntroduceTheme());
        read_count.setTextColor(configure.getIntroduceTheme());
        read_finish.setTextColor(configure.getIntroduceTheme());
        SharedTools sharedTools = SharedTools.getSharedTools();
        long time = sharedTools.getReadTime();
        read_time.setText(resolver(time));
        read_count.setText(sharedTools.getStart() + " 本");
        read_finish.setText(sharedTools.getFinish() + " 本");
        if (configure.getMode() == NovelConfigure.DAY) {
            findViewById(R.id.read_top).setBackground(getResources().getDrawable(R.drawable.read_ex_day));
        } else {
            findViewById(R.id.read_top).setBackground(getResources().getDrawable(R.drawable.read_ex_night));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
