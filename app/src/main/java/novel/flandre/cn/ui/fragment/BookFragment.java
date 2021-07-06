package novel.flandre.cn.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;
import novel.flandre.cn.R;
import novel.flandre.cn.adapter.adapter.fragment.BookAdapter;
import novel.flandre.cn.adapter.decoration.Decoration;
import novel.flandre.cn.bean.data.novel.NovelInfo;
import novel.flandre.cn.service.NovelService;
import novel.flandre.cn.ui.activity.IndexActivity;
import novel.flandre.cn.ui.activity.TextActivity;
import novel.flandre.cn.utils.database.SQLTools;
import novel.flandre.cn.utils.database.SQLiteNovel;
import novel.flandre.cn.utils.parse.OnFinishParse;
import novel.flandre.cn.utils.parse.ShareParse;
import novel.flandre.cn.utils.tools.NovelConfigureManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 小说书架
 * 2019.??
 */
public class BookFragment extends AttachFragment implements SwipeRefreshLayout.OnRefreshListener,
        NovelService.UpdateNovel, OnFinishParse {
    public static final String TAG = "BookFragment";
    private BookAdapter bookAdapter;
    private SwipeRefreshLayout refresh;
    private SQLiteNovel sqLiteNovel;
    private List<Integer> hasDelete;

    private TextView empty;

    public SwipeRefreshLayout getRefresh() {
        return refresh;
    }

    @Override
    public void onResume() {
        super.onResume();
        handleData();
    }

    private void handleData() {
        Bundle bundle = getArguments();
        if (bundle != null && bundle.get("path") != null) {
            refresh.setRefreshing(true);
            String path = (String) bundle.get("path");
            setArguments(null);
            new ShareParse(path, mContext).setOnfinishParse(this).parseFile(refresh);
        }
    }

    public void loadData() {
        if (bookAdapter == null) return;
        // 拿到收藏的小说, 填充主界面, 若没有小说, 显示空空如也
        List<NovelInfo> list = SQLTools.getNovelData(sqLiteNovel);
        hasDelete = null;
        if (list.size() > 0) {
            empty.setVisibility(View.GONE);
            bookAdapter.setNewData(list);
        } else {
            bookAdapter.setNewData(null);
            empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqLiteNovel = SQLiteNovel.getSqLiteNovel(mContext.getApplicationContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_fragment_layout, container, false);
        empty = view.findViewById(R.id.empty);
        empty.setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        refresh = view.findViewById(R.id.refresh);
        refresh.setColorSchemeColors(NovelConfigureManager.getConfigure().getMainTheme());
        refresh.setOnRefreshListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.book_main);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayout.VERTICAL);
        recyclerView.setLayoutManager(manager);
        bookAdapter = new BookAdapter(R.layout.index_list, null);
        recyclerView.addItemDecoration(new Decoration(mContext));
        recyclerView.setAdapter(bookAdapter);
        recyclerView.setHasFixedSize(false);

        bookAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                // 点击开始阅读
                NovelInfo novelInfo = (NovelInfo) adapter.getData().get(position);
                Intent intent = new Intent(mContext, TextActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", novelInfo.getName());
                bundle.putString("author", novelInfo.getAuthor());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        bookAdapter.setOnItemChildLongClickListener(new BaseQuickAdapter.OnItemChildLongClickListener() {
            @Override
            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
                // 长按底部弹出菜单
                int base = position;
                if (hasDelete != null) for (Integer p : hasDelete) if (p < base) position--;
                NovelInfo novelInfo = (NovelInfo) adapter.getData().get(position);
                IndexDialogFragment fragment = IndexDialogFragment.newInstance(novelInfo, base);
                fragment.show(getChildFragmentManager(), "dialog");
                return true;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    @Override
    public void onRefresh() {
        ((IndexActivity) mContext).getService().update(NovelService.UPDATE_ALL, this);
    }

    @Override
    public void onUpdateStart() {
        refresh.setRefreshing(true);
        Toast.makeText(mContext, "更新小说中", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateFail() {
        Toast.makeText(mContext, "更新紧啊，扑街！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateFinish(int updateFinish, int updateCount, int id) {
        if (updateFinish == updateCount) {
            refresh.setRefreshing(false);
            Toast.makeText(mContext, "更新完毕", Toast.LENGTH_SHORT).show();
            if (updateFinish != -1)
                loadData();
        }
    }

    /**
     * 删除小说后更新界面
     *
     * @param message obj 数据在列表中的位置
     */
    public void deleteBook(Message message) {
        int position, base;
        if (hasDelete == null) hasDelete = new ArrayList<>();
        base = position = (int) message.obj;
        hasDelete.add(position);
        for (Integer p : hasDelete) if (p < base) position--;
        bookAdapter.getData().remove(position);
        bookAdapter.notifyItemRemoved(position);
        if (bookAdapter.getData().size() == 0) empty.setVisibility(View.VISIBLE);
    }

    public void changeTheme() {
        if (empty == null) return;
        refresh.setColorSchemeColors(NovelConfigureManager.getConfigure().getMainTheme());
        empty.setTextColor(NovelConfigureManager.getConfigure().getIntroduceTheme());
        bookAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFinishParse(int mode) {
        refresh.setRefreshing(false);
        if (mode == OnFinishParse.OK) {
            loadData();
        }
    }
}
