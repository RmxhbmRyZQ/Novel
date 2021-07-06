package novel.flandre.cn.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.ActionBar;
import androidx.core.widget.NestedScrollView;
import novel.flandre.cn.R;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.utils.database.SQLTools;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.tools.NovelConfigure;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import static novel.flandre.cn.utils.database.SQLTools.saveInSQLite;

/**
 * 查看详细界面
 * 2019.12.6
 */
public class NovelDetailActivity extends BaseActivity implements View.OnClickListener {
    private NovelInfo novelInfo;
    private SQLiteNovel sqLiteNovel;
    private ImageView image;
    private Button star;
    private Button read;
    private TextView name;
    private TextView author;
    private TextView status;
    private TextView introduce;
    private NovelConfigure configure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_detail);
        setupNovelService();
        configure = NovelConfigureManager.getConfigure(getApplicationContext());
        getValue();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        unpack(savedInstanceState);
        // 判断数据库里是否存在数据
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(getApplicationContext());
        Cursor cursor = sqLiteNovel.getReadableDatabase().query("novel", new String[]{"source"},
                "author=? and name=?", new String[]{novelInfo.getAuthor(), novelInfo.getName()},
                null, null, null);
        try {
            if (cursor.moveToNext()) {
                if (novelInfo.getSource().equals(cursor.getString(0))) {
                    star.setText(getResources().getText(R.string.cancel));
                    read.setText(getResources().getText(R.string.ctn));
                } else {
                    star.setText(getResources().getText(R.string.change));
                    read.setText(getResources().getText(R.string.ctn));
                }
            } else {
                star.setText(getResources().getText(R.string.star));
                read.setText(getResources().getText(R.string.read));
            }
        }catch (NullPointerException e){
            star.setText(getResources().getText(R.string.cancel));
            read.setText(getResources().getText(R.string.ctn));
        }
        cursor.close();
        NestedScrollView scrollView = findViewById(R.id.wrap);
        name.setText(novelInfo.getName());
        author.setText("作者: " + novelInfo.getAuthor());
        status.setText("状态: " + (novelInfo.getComplete() == 0 ? "连载" : "完本"));
        introduce.setText(novelInfo.getIntroduce());
        image.setImageBitmap(novelInfo.getBitmap());

        star.setOnClickListener(starListener);

        read.setOnClickListener(this);
    }

    private void unpack(Bundle savedInstanceState) {
        Bundle bundle;
        // 拿到传参
        if (savedInstanceState == null)
            bundle = getIntent().getExtras();
        else bundle = savedInstanceState;

        novelInfo = (NovelInfo) bundle.getSerializable("NovelInfo");
    }

    private void getValue() {
        // 拿到标签
        name = findViewById(R.id.name);
        author = findViewById(R.id.author);
        status = findViewById(R.id.status);
        introduce = findViewById(R.id.introduce);
        star = findViewById(R.id.star);
        read = findViewById(R.id.read);
        image = findViewById(R.id.image);
        LinearLayout linearLayout = findViewById(R.id.total);
        // 设置主题
        linearLayout.setBackgroundColor(configure.getBackgroundTheme());
        star.setBackgroundColor(configure.getMainTheme());
        read.setBackgroundColor(configure.getMainTheme());
        name.setTextColor(configure.getNameTheme());
        author.setTextColor(configure.getAuthorTheme());
        status.setTextColor(configure.getAuthorTheme());
        introduce.setTextColor(configure.getIntroduceTheme());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(configure.getMainTheme()));
    }

    private void starBook(TextView readView, TextView textView) {
        if (novelInfo.getSource() == null) {
            Toast.makeText(this, "请再次使用本地导入", Toast.LENGTH_SHORT).show();
            return;
        }
        novelInfo.setSource(novelInfo.getSource().replace("crawler", "net"));
        saveInSQLite(novelInfo, sqLiteNovel, this);
        textView.setText(getResources().getText(R.string.cancel));
        readView.setText(getResources().getText(R.string.ctn));
    }

    private void cancelStar(TextView readView, TextView textView) {
        // 删除图片与相关的表与数据
        int id = SQLTools.getNovelId(sqLiteNovel, novelInfo.getName(), novelInfo.getAuthor());
        SQLTools.delete(sqLiteNovel, String.valueOf(id), mService, this);
        novelInfo.setTable(null);
        textView.setText(getResources().getText(R.string.star));
        readView.setText(getResources().getText(R.string.read));
    }

    private void changeSource(TextView readView, TextView textView) {
        // 改变书本来源
        int id = SQLTools.getNovelId(sqLiteNovel, novelInfo.getName(), novelInfo.getAuthor());
        SQLTools.changeSource(sqLiteNovel, novelInfo, String.valueOf(id), mService);
        textView.setText(getResources().getText(R.string.cancel));
        readView.setText(getResources().getText(R.string.ctn));
    }

    private View.OnClickListener starListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView textView = (TextView) view;
            TextView readView = findViewById(R.id.read);
            if (textView.getText().equals(getResources().getText(R.string.star))) {
                starBook(readView, textView);
            } else if (textView.getText().equals(getResources().getText(R.string.change))) {
                changeSource(readView, textView);
            } else {
                cancelStar(readView, textView);
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("NovelInfo", novelInfo);
        super.onSaveInstanceState(outState);
    }

    /**
     * 进入阅读界面
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TextActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", novelInfo.getName());
        bundle.putString("author", novelInfo.getAuthor());
        if (star.getText() == getResources().getText(R.string.star)) {
            bundle.putString("url", novelInfo.getUrl());
            bundle.putString("source", novelInfo.getSource());
        }
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
