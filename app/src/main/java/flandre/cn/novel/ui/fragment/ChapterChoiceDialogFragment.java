package flandre.cn.novel.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import flandre.cn.novel.R;
import flandre.cn.novel.adapter.adapter.fragment.ChoiceChapterAdapter;
import flandre.cn.novel.utils.tools.NovelConfigureManager;
import flandre.cn.novel.ui.activity.TextActivity;
import flandre.cn.novel.utils.database.SQLiteNovel;
import flandre.cn.novel.bean.data.novel.NovelChapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节选着的Dialog
 * 2020.5.5
 */
public class ChapterChoiceDialogFragment extends AttachDialogFragment implements AdapterView.OnItemClickListener {
    private int mChapter;
    private List<? extends NovelChapter> mList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AlarmDialog);
        SQLiteNovel sqLiteNovel = SQLiteNovel.getSqLiteNovel();
        TextActivity activity = (TextActivity) mContext;
        mChapter = activity.getChapter();
        if (activity.getTable() == null) {
            mList = activity.getList();
        } else {
            List<NovelChapter> list = new ArrayList<>();
            Cursor cursor = sqLiteNovel.getReadableDatabase().query(activity.getTable(), new String[]{"chapter"},
                    null, null, null, null, null);

            cursor.moveToNext();
            do {
                NovelChapter novelChapter = new NovelChapter();
                novelChapter.setChapter(cursor.getString(0));
                list.add(novelChapter);
            } while (cursor.moveToNext());
            mList = list;
            cursor.close();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chapter_choice_fragment, container, false);
        view.findViewById(R.id.layout).setBackgroundColor(NovelConfigureManager.getConfigure().getBackgroundColor());

        ListView listView = view.findViewById(R.id.list);
        int show = 10;
        int all = mList.size();
        // 显示的位置
        int position = mChapter - show / 2;
        position = position > -1 ? position : 0;
        position = position < all - show ? position : all - show;
        listView.setAdapter(new ChoiceChapterAdapter((TextActivity) mContext, mList));
        listView.setSelection(position);
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((TextActivity) mContext).choiceChapter(position + 1);
        dismiss();
    }
}
