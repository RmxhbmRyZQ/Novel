package novel.flandre.cn.ui.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.activity.ConfigureSourceAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.serializable.SourceItem;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 配置源的Activity
 * 2019.12.7
 */
public class ConfigureSourceActivity extends BaseActivity {
    private NovelConfigure configure;
    private ConfigureSourceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_source);
        setupMusicService();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("设置来源");

        RecyclerView recyclerView = findViewById(R.id.rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new ConfigureSourceAdapter(R.layout.source_list, NovelConfigureManager.getSource());
        adapter.bindToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new Decoration(this));

        configure = NovelConfigureManager.getConfigure(getApplicationContext());

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(configure.getMainTheme()));
        recyclerView.setBackgroundColor(configure.getBackgroundTheme());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.set_sourcec_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.save:
                try {
                    // 设置当前使用源为选择源
                    SourceItem map = NovelConfigureManager.getSource().get(adapter.getCurrent());
                    configure.setNowSourceValue(map.getSource());
                    configure.setNowSourceKey(map.getName());
                    NovelConfigureManager.setConstructor(map.getSource());
                    // 保存配置文件并结束界面
                    NovelConfigureManager.saveConfigure(configure, this);
                    finish();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }
}
