package novel.flandre.cn.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.activity.ConfigureThemeAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.data.activity.ConfigureThemeData;
import novel.flandre.cn.bean.serializable.PageViewItem;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 开放给用户的页面主题等数据的配置页面
 * 2019.12.7
 */
public class ConfigureThemeActivity extends BaseActivity {
    private NovelConfigure configure;

    private int themeStart;  // 主题颜色的第一个索引
    private int multiStart;  // CheckBox的第一个索引
    private ConfigureThemeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        setupMusicService();
        configure = NovelConfigureManager.getConfigure(this.getApplicationContext());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(configure.getMainTheme()));

        NestedScrollView total = findViewById(R.id.total);
        total.setBackgroundColor(configure.getBackgroundTheme());

        setupRecycleView();
    }

    private void setupRecycleView() {
        adapter = new ConfigureThemeAdapter(getData());
        RecyclerView recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(getDecoration());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);
        recyclerView.setBackground(new ColorDrawable((~configure.getBackgroundTheme()) & 0x11FFFFFF | 0x11000000));
        adapter.bindToRecyclerView(recyclerView);
    }

    private RecyclerView.ItemDecoration getDecoration() {
        Decoration decoration = new Decoration(this);
        List<ConfigureThemeData> data = adapter.getData();
        List<Integer> list = new ArrayList<>();
        list.add(0);
        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).getItemType() == ConfigureThemeData.HEAD_TYPE) {
                list.add(i - 1);
                list.add(i);
            }
        }
        decoration.setList(list);
        return decoration;
    }

    private List<ConfigureThemeData> getData() {
        List<ConfigureThemeData> data = new ArrayList<>();
        data.add(new ConfigureThemeData(new ConfigureThemeData.Head("阅读界面")));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("文字颜色：", configure.getBaseTextColor())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("文字大小：", String.valueOf(configure.getTextSize()))));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("背景颜色：", configure.getBaseBackgroundColor())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("章节颜色：", configure.getBaseChapterColor())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Head("主题颜色")));
        themeStart = data.size();
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("标题背景：", configure.getBaseMainTheme())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("背景颜色：", configure.getBaseBackgroundTheme())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("书名颜色：", configure.getBaseNameTheme())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("作者颜色：", configure.getBaseAuthorTheme())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Edit("介绍颜色：", configure.getBaseIntroduceTheme())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Head("翻页动画")));
        for (PageViewItem item : NovelConfigureManager.getPageView())
            data.add(new ConfigureThemeData(new ConfigureThemeData.OneChoice(item.getDescription(),
                    item.getSource(), item.getSource().equals(configure.getNowPageView()))));
        data.add(new ConfigureThemeData(new ConfigureThemeData.Head("其它")));
        multiStart = data.size();
        data.add(new ConfigureThemeData(new ConfigureThemeData.MultiChoice("全屏点击翻下页", configure.isAlwaysNext())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.MultiChoice("小说闹钟强制休息", configure.isAlarmForce())));
        data.add(new ConfigureThemeData(new ConfigureThemeData.MultiChoice("循环闹钟", configure.isConstantAlarm())));
        return data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 在ActionBar上添加菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.configure_theme_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.save:
                doSave();
                break;
        }
        return true;
    }

    private void doSave() {
        try {
            // 把数据保存到配置类里, 然后把类写入文件
            List<ConfigureThemeData> data = adapter.getData();
            configure.setTextColor(data.get(1).getEdit().getEditString());
            configure.setTextSize(Integer.parseInt(data.get(2).getEdit().getEditString()));
            configure.setBackgroundColor(data.get(3).getEdit().getEditString());
            configure.setChapterColor(data.get(4).getEdit().getEditString());

            configure.setMainTheme(data.get(themeStart).getEdit().getEditString());
            configure.setBackgroundTheme(data.get(themeStart + 1).getEdit().getEditString());
            configure.setNameTheme(data.get(themeStart + 2).getEdit().getEditString());
            configure.setAuthorTheme(data.get(themeStart + 3).getEdit().getEditString());
            configure.setIntroduceTheme(data.get(themeStart + 4).getEdit().getEditString());

            configure.setNowPageView(data.get(adapter.getCurrent()).getOneChoice().getSource());
            configure.setAlwaysNext(data.get(multiStart).getMultiChoice().isSelected());
            configure.setAlarmForce(data.get(multiStart + 1).getMultiChoice().isSelected());
            configure.setConstantAlarm(data.get(multiStart + 2).getMultiChoice().isSelected());

            NovelConfigureManager.saveConfigure(this.configure, this);
            Intent intent = new Intent();
            intent.setAction(IndexActivity.CHANGE_THEME);
            sendBroadcast(intent);
            setResult(0);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "你写的数据有问题", Toast.LENGTH_SHORT).show();
        }
    }
}
