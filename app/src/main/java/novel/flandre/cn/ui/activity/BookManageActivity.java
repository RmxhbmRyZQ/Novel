package novel.flandre.cn.ui.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.activity.BookManagerAdapter;
import novel.flandre.cn.adapter.adapter.activity.SpinnerAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.utils.database.SQLTools;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.tools.NovelConfigureManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 书籍管理
 * 2019.12.9
 */
public class BookManageActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private boolean select_all = false;  // 是否选择全部
    private BookManagerAdapter adapter;
    private TextView textView;
    private SQLiteNovel sqLiteNovel;
    private LinearLayout linearLayout;
    private int mode;  // 进行的操作(0 删除选中)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manage);
        setupMusicService();
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(getApplicationContext());
        NovelConfigureManager.getConfigure(getApplicationContext());

        linearLayout = findViewById(R.id.bottom);
        Spinner spinner = findViewById(R.id.select);
        textView = findViewById(R.id.select_all);
        textView.setTextColor(Color.WHITE);
        linearLayout.setBackgroundColor(NovelConfigureManager.getConfigure().getMainTheme());
        textView.setOnClickListener(this);
        spinner.setAdapter(new SpinnerAdapter(new ArrayList<String>() {{
            add("删除");
            add("设为连载");
            add("设为完结");
            add("删除数据");
        }}, this));
        spinner.setPopupBackgroundDrawable(new ColorDrawable(NovelConfigureManager.getConfigure().getMainTheme()));
        spinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        spinner.setOnItemSelectedListener(this);

        setupActionBar();
        setupData();
        loadData();
    }

    private void loadData() {
        List<NovelInfo> list = SQLTools.getNovelData(sqLiteNovel);
        adapter.setNewData(list);
    }

    private void setupData() {
        RecyclerView recyclerView = findViewById(R.id.data);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new BookManagerAdapter(R.layout.manage_list, null);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new Decoration(this));
        recyclerView.setHasFixedSize(false);
        recyclerView.setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundTheme());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 设置菜单
        if (adapter.isManage()) {
            menu.findItem(R.id.cancel).setVisible(false);
            menu.findItem(R.id.manager).setVisible(true);
        } else {
            menu.findItem(R.id.cancel).setVisible(true);
            menu.findItem(R.id.manager).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 在ActionBar上添加菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manager_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel:
                adapter.setManage(true);
                select_all = false;
                textView.setText("全选");
                // 使菜单无效重新生成
                invalidateOptionsMenu();
                adapter.notifyDataSetChanged();
                if (linearLayout != null) linearLayout.setVisibility(View.GONE);
                break;
            case R.id.manager:
                adapter.setManage(false);
                invalidateOptionsMenu();
                adapter.notifyDataSetChanged();
                if (linearLayout != null) linearLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.save:
                doSave();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void doSave() {
        ContentValues values;
        switch (mode) {
            case 0:
                // 删除选中的小说
                for (String i : adapter.getPosition()) {
                    SQLTools.delete(sqLiteNovel, i, mService, this);
                }
                loadData();
                break;
            case 1:
            case 2:
                values = new ContentValues();
                values.put("complete", mode >> 1);
                for (String i : adapter.getPosition())
                    sqLiteNovel.getReadableDatabase().update("novel", values, "id = ?", new String[]{i});
                loadData();
                break;
            case 3:
                values = new ContentValues();
                values.putNull("text");
                for (String i : adapter.getPosition()) {
                    String table = SQLTools.getTableName(sqLiteNovel, i);
                    sqLiteNovel.getReadableDatabase().update(table, values, null, null);
                }
                loadData();
                break;
        }
        adapter.getPosition().clear();
        Intent intent = new Intent();
        intent.setAction(IndexActivity.LOAD_DATA);
        sendBroadcast(intent);
        // 还原界面
        adapter.setManage(true);
        select_all = false;
        linearLayout.setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("书本管理");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(NovelConfigureManager.getConfigure().getMainTheme()));
    }

    @Override
    public void onClick(View v) {
        List<String> position = adapter.getPosition();
        if (select_all) {
            select_all = false;
            ((TextView) v).setText("全选");
            position.clear();
        } else {
            select_all = true;
            ((TextView) v).setText("取消");
            position.clear();
            for (NovelInfo novelInfo : adapter.getData())
                position.add(String.valueOf(novelInfo.getId()));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mode = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
